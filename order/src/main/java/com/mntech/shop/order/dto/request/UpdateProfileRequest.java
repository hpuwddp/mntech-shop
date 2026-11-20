package com.mntech.shop.order.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;
    private String gender;
}