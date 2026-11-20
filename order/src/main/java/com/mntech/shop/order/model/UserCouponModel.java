package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupon")
public class UserCouponModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("coupon_id")
    private String couponId;

    @TableField("coupon_code")
    private String couponCode;

    @TableField("status")
    private String status;  // UNUSED-未使用, USED-已使用, EXPIRED-已过期

    @TableField("used_time")
    private LocalDateTime usedTime;

    @TableField("order_no")
    private String orderNo;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 非数据库字段 - 关联优惠券信息
    @TableField(exist = false)
    private CouponModel coupon;
}
