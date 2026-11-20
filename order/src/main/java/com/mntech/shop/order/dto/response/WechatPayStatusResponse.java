package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WechatPayStatusResponse {
    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 微信支付交易号
     */
    private String transactionId;

    /**
     * 交易状态：SUCCESS-支付成功, REFUND-转入退款, NOTPAY-未支付, CLOSED-已关闭, REVOKED-已撤销, USERPAYING-用户支付中, PAYERROR-支付失败
     */
    private String tradeState;

    /**
     * 交易状态描述
     */
    private String tradeStateDesc;

    /**
     * 用户支付金额（分）
     */
    private Integer payerTotal;

    /**
     * 支付完成时间
     */
    private String successTime;

    /**
     * 订单本地状态
     */
    private String localStatus;

    /**
     * 是否支付成功
     */
    private Boolean isPaid;
}
