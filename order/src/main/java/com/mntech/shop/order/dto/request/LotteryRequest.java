package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 抽奖请求
 */
@Data
public class LotteryRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
