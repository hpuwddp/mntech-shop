package com.mntech.shop.order.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

//@Data
//@Configuration
//@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {
    /**
     * 商户号
     */
    private String merchantId;

    /**
     * 商户API证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 商户API证书私钥路径
     */
    private String privateKeyPath;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * 小程序AppId
     */
    private String appId;

    /**
     * 回调地址
     */
    private String notifyUrl;

}
