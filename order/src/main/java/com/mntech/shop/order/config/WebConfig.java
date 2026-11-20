package com.mntech.shop.order.config;

import com.mntech.shop.order.Interceptor.GatewayInnerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String GATEWAY_INNER_SECRET = "mntech-shop-2026-internal-gateway-secret";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GatewayInnerInterceptor())
                .addPathPatterns("/**")
                // 微信回调接口，第三方微信服务器调用，不需要网关头，放行
                .excludePathPatterns("/api/pay/notify");
    }
}