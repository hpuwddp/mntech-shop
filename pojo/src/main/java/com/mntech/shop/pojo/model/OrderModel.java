package com.mntech.shop.pojo.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class OrderModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private String userId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

@TableField("pay_amount")
    private BigDecimal payAmount;

    /** 优惠券抵扣金额 */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /** 使用的用户优惠券ID（user_coupon.id） */
    @TableField("coupon_id")
    private String couponId;

    /** 优惠券名称 */
    @TableField("coupon_name")
    private String couponName;

    @TableField("status")
    private String status;  // PENDING-待支付, PAID-已支付, CANCELLED-已取消, REFUND-已退款

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    @TableField("receiver_address")
    private String receiverAddress;

    @TableField("pay_type")
    private String payType;  // wechat-微信, alipay-支付宝

    @TableField("remark")
    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    @TableField("wl")
    private String wl;
    @TableField("wl_code")
    private String wlCode;
    // ✅ 非数据库字段，用于关联查询
    @TableField(exist = false)
    private List<OrderItemModel> items;
}