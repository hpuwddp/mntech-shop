package com.mntech.shop.order.dto.request;

import lombok.Data;

@Data
public class WechatLoginRequest {
    private String code;           // 微信登录code
    private String encryptedData;  // 加密数据
    private String iv;             // 加密向量
}
