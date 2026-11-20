package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReceivePrizeRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotNull(message = "地址信息不能为空")
    @Valid
    private AddressInfo address;

    @Data
    public static class AddressInfo {
        @NotBlank(message = "收货人姓名不能为空")
        private String receiverName;

        @NotBlank(message = "手机号不能为空")
        private String receiverPhone;

        @NotBlank(message = "省份不能为空")
        private String province;

        @NotBlank(message = "城市不能为空")
        private String city;

        private String district;

        @NotBlank(message = "详细地址不能为空")
        private String detailAddress;
    }
}