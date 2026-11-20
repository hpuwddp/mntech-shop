package com.mntech.shop.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 跨域配置
// */
//@Configuration
//public class CorsConfig  implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")  // 所有接口
//                .allowedOriginPatterns("*")  // 允许所有域名（生产环境请指定具体域名）
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的方法
//                .allowedHeaders("*")  // 允许的请求头
//                .allowCredentials(true)  // 允许携带凭证（Cookie）
//                .maxAge(3600);  // 预检请求缓存时间（秒）
//    }
//}

/**
 * 跨域配置
 */
public class CorsConfig {


}
