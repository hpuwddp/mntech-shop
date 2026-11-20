package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateGenderRequest {

    @NotBlank(message = "性别不能为空")
    private String gender;
}