package com.mntech.shop.order.dto.response;

import lombok.Data;

@Data
public class WechatSessionResponse {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}
