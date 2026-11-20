package com.mntech.shop.order.Interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mntech.shop.order.config.WebConfig;
import com.mntech.shop.pojo.dto.response.Result;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class GatewayInnerInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String secret = request.getHeader("X-Gateway-Secret");
        if (!StrUtil.equals(secret, WebConfig.GATEWAY_INNER_SECRET)) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Result<?> error = Result.error("禁止直接访问后端服务，请通过网关访问");
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return false;
        }
        return true;
    }
}