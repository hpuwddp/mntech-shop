package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WechatLoginResponse {
    private String userId;
    private String token;
    private Long expireTime;
    private String nickname;
    private String avatar;
    private Boolean isNewUser;
}
