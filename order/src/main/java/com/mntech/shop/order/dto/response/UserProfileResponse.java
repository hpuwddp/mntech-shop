package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private String id;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private String createTime;
}