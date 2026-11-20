package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WechatPayResponse {
    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付参数（小程序调起支付用）
     */
    private String timeStamp;
    private BigDecimal totalAmount;

    private String nonceStr;
    private String packageStr;
    private String signType;
    private String paySign;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String message;
}
