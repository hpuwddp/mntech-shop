package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateNicknameRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
