package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class CouponModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("name")
    private String name;

@TableField("description")
    private String description;

    @TableField("type")
    private String type;  // cash-满减, discount-折扣

    @TableField("value")
    private BigDecimal value;

    @TableField("discount")
    private BigDecimal discount;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @TableField("applicable_scope")
    private String applicableScope;  // all-全场, category-指定分类, product-指定商品

    @TableField("applicable_ids")
    private String applicableIds;

    @TableField("total_quantity")
    private Integer totalQuantity;

    @TableField("used_quantity")
    private Integer usedQuantity;

    @TableField("remaining_quantity")
    private Integer remainingQuantity;

    @TableField("user_limit")
    private Integer userLimit;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}