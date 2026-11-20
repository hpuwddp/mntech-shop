package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOrderResponse {
    private String orderId;
    private String orderNo;
    private String teamId;
    private String groupNo;
    private String activityId;
    private String groupBuyProductId;
    private String productName;
    private String productImage;
    private BigDecimal groupPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    /** 优惠金额（团长9折 = 原合计 * 0.1，参团为0） */
    private BigDecimal discountAmount;
    /** 实付金额（团长9折后 = totalAmount - discountAmount；未支付则等于下单时应付） */
    private BigDecimal payAmount;
    /** 退款金额（未成团退款 = payAmount） */
    private BigDecimal refundAmount;
    /** 退款时间 */
    private LocalDateTime refundTime;
    private String status;
    private String statusDesc;
    private LocalDateTime createdAt;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Long remainingPayTime;
    // 所属团信息
    private Integer teamCurrentNum;
    private Integer teamTargetNum;
    private String teamStatus;
    private String teamStatusDesc;
    /** 当前订单用户是否为该团团长（用于前端展示团长取消整团入口） */
    private Boolean isLeader;
}