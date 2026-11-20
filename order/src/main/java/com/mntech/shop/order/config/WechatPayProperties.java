package com.mntech.shop.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
@RefreshScope // 加上这个注解，支持动态刷新
public class WechatPayProperties {
    private String merchantId;
    private String merchantSerialNumber;
    private String privateKeyPath;
    private String apiV3Key;
    private String publicKeyPath;
    private String publicKeyId;
    private String appId;
    private String notifyUrl;
}
