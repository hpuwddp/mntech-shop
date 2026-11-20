package com.mntech.shop.gateway.config;

import com.alibaba.fastjson2.JSON;
import com.mntech.shop.gateway.dto.Result;
import io.jsonwebtoken.*;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    // 🔥 确保这个密钥与生成Token时使用的完全一致
    private static final String JWT_SECRET = "mntech-shop-2026-jwt-secret-key-32bytes";

    private static final String INNER_GATEWAY_SECRET = "mntech-shop-2026-internal-gateway-secret";
    private static final int STAT_WINDOW_SEC = 60;
    private static final int BLACK_THRESHOLD = 120;
    private static final int BLACK_EXPIRE_SEC = 300;

    private final List<String> whiteList = Arrays.asList(
            "/order/api/user/login/wechat",
            "/order/api/user/register",
            "/order/api/categories/query",
            "/order/api/products/**",
            "/order/api/cart/*",
            "/order/api/pay/notify",
            "/order/api/seckill/user/buy"
    );

    public AuthGlobalFilter(ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer stringSer = StringRedisSerializer.UTF_8;
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(stringSer)
                .build();
        this.reactiveRedisTemplate = new ReactiveRedisTemplate<>(factory, context);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethodValue();
        // 放行跨域OPTIONS预检
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        // 白名单：免登录
        if (isWhiteList(path)) {
            System.out.println("【白名单请求】" + path);
            ServerHttpRequest newReq = request.mutate()
                    .headers(h -> {
                        h.remove("X-Gateway-Secret");
                        h.remove("X-User-Id");
                    })
                    .header("X-Gateway-Secret", INNER_GATEWAY_SECRET)
                    .build();
            return chain.filter(exchange.mutate().request(newReq).build());
        }

        String clientIp = getRealIp(request);

        // 提取Token
        String token = extractToken(request);
        System.out.println("【提取到的Token】" + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null"));

        if (!StringUtils.hasText(token)) {
            System.out.println("【Token缺失】返回401");
            return writeUnauthorized(exchange.getResponse(), "token缺失，请登录");
        }

        String userId;
        try {
            // 🔥 打印JWT_SECRET长度用于调试
            System.out.println("【JWT_SECRET长度】" + JWT_SECRET.getBytes(StandardCharsets.UTF_8).length);

            // 解析JWT
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);

            // 打印所有claims
            System.out.println("【Claims内容】" + jws.getBody().entrySet());

            userId = jws.getBody().get("userId").toString();
            System.out.println("【Token解析成功】userId: " + userId);

        } catch (ExpiredJwtException e) {
            System.out.println("【Token已过期】" + e.getMessage());
            return writeUnauthorized(exchange.getResponse(), "token已过期，请重新登录");
        } catch (MalformedJwtException e) {
            System.out.println("【Token格式错误】" + e.getMessage());
            return writeUnauthorized(exchange.getResponse(), "token格式错误");
        } catch (SignatureException e) {
            System.out.println("【Token签名验证失败】" + e.getMessage());
            System.out.println("【可能原因】JWT_SECRET与生成Token时使用的密钥不一致！");
            return writeUnauthorized(exchange.getResponse(), "token签名验证失败");
        } catch (Exception e) {
            System.out.println("【Token解析失败】" + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return writeUnauthorized(exchange.getResponse(), "token非法: " + e.getMessage());
        }

        // 限流和黑名单检查
        String countKey = "access_cnt:" + userId + ":" + clientIp;
        String blackKey = "black:" + userId + ":" + clientIp;
        String finalUserId = userId;

        return reactiveRedisTemplate.hasKey(blackKey)
                .flatMap(isBlack -> {
                    if (Boolean.TRUE.equals(isBlack)) {
                        return writeForbidden(exchange.getResponse(), "您访问过于频繁，已被临时限制，请稍后重试");
                    }
                    return reactiveRedisTemplate.opsForValue().increment(countKey)
                            .flatMap(cntVal -> {
                                Mono<Long> afterExpireMono;
                                if (cntVal == 1) {
                                    afterExpireMono = reactiveRedisTemplate.expire(countKey, Duration.ofSeconds(STAT_WINDOW_SEC))
                                            .thenReturn(cntVal);
                                } else {
                                    afterExpireMono = Mono.just(cntVal);
                                }
                                return afterExpireMono.flatMap(currentCnt -> {
                                    if (currentCnt >= BLACK_THRESHOLD) {
                                        return reactiveRedisTemplate.opsForValue()
                                                .set(blackKey, "1", Duration.ofSeconds(BLACK_EXPIRE_SEC))
                                                .flatMap(unused -> writeForbidden(exchange.getResponse(), "访问过于频繁，已临时拉黑"));
                                    }
                                    return doAuth(exchange, chain, finalUserId);
                                });
                            });
                });
    }

    /**
     * 从多个来源提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. 从 Header 中获取 x-access-token
        String token = request.getHeaders().getFirst("x-access-token");
        if (StringUtils.hasText(token)) {
            System.out.println("【Token来源】Header: x-access-token");
            return token;
        }

        // 2. 从 URL 参数中获取 x_access_token（小程序主要使用）
        token = request.getQueryParams().getFirst("x_access_token");
        if (StringUtils.hasText(token)) {
            System.out.println("【Token来源】URL参数: x_access_token");
            return token;
        }

        // 3. 从 URL 参数中获取 access_token（兼容其他命名）
        token = request.getQueryParams().getFirst("access_token");
        if (StringUtils.hasText(token)) {
            System.out.println("【Token来源】URL参数: access_token");
            return token;
        }

        // 4. 兼容标准 Authorization: Bearer xxx
        String authHeader = request.getHeaders().getFirst("x-access-token");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("【Token来源】Header: Authorization Bearer");
            return token;
        }

        // 5. 兼容旧的 token 头
        token = request.getHeaders().getFirst("token");
        if (StringUtils.hasText(token)) {
            System.out.println("【Token来源】Header: token");
            return token;
        }

        return null;
    }

    private Mono<Void> doAuth(ServerWebExchange exchange, GatewayFilterChain chain, String userId) {
        System.out.println("【认证通过】userId: " + userId);
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest newReq = request.mutate()
                .headers(h -> {
                    h.remove("X-User-Id");
                    h.remove("X-Gateway-Secret");
                })
                .header("X-User-Id", userId)
                .header("X-Gateway-Secret", INNER_GATEWAY_SECRET)
                .build();
        return chain.filter(exchange.mutate().request(newReq).build());
    }

    private String getRealIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private boolean isWhiteList(String path) {
        for (String p : whiteList) {
            if (antPathMatcher.match(p, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeForbidden(ServerHttpResponse response, String msg) {
        System.out.println("【403】" + msg);
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<?> res = Result.error(msg);
        byte[] bytes = JSON.toJSONString(res).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }

    private Mono<Void> writeUnauthorized(ServerHttpResponse response, String msg) {
        System.out.println("【401】" + msg);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<?> res = Result.error(msg);
        byte[] bytes = JSON.toJSONString(res).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}