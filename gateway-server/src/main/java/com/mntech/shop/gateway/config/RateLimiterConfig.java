package com.mntech.shop.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {


    /**
     * 【生产可用】IP+userId组合限流
     * 优先取Nginx透传 X‑Forwarded‑For 获取真实客户端IP；取不到降级使用RemoteAddress；兜底unknown
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String ip = "unknown";

            // 1.优先从 X‑Forwarded‑For 获取真实客户端IP（Nginx代理场景）
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            System.out.println("xForwardedFor---"+xForwardedFor);
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // X‑Forwarded‑For格式：客户端ip,代理1ip,代理2ip，第一个就是真实客户端
                String[] ips = xForwardedFor.split(",");
                if (ips.length > 0) {
                    ip = ips[0].trim();
                }
            } else {
                // 2.没有代理头，降级取 RemoteAddress
                InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                if (remoteAddress != null) {
                    ip = remoteAddress.getAddress().getHostAddress();
                }
            }

            // 获取登录用户ID header X‑User‑Id
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            System.out.println("userId---"+userId);
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId + ":ip:" + ip);
            }
            // 未登录用户，按IP限流
            return Mono.just("ip:" + ip);
        };
    }


    /**
     * 仅IP限流（备用）
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String ip = "unknown";
            String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                String[] ips = xForwardedFor.split(",");
                if (ips.length > 0) {
                    ip = ips[0].trim();
                }
            } else {
                InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                if (remoteAddress != null) {
                    ip = remoteAddress.getAddress().getHostAddress();
                }
            }
            return Mono.just(ip);
        };
    }

    /**
     * 按路径限流（备用）
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getURI().getPath());
    }
}
