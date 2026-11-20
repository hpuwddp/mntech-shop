package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_buy_product")
public class GroupBuyProductModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("activity_id")
    private String activityId;

    @TableField("product_id")
    private String productId;

    @TableField("sku_id")
    private String skuId;

    @TableField("group_price")
    private BigDecimal groupPrice;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("remaining_stock")
    private Integer remainingStock;

    @TableField("per_user_limit")
    private Integer perUserLimit;

    @TableField("min_people")
    private Integer minPeople;

    @TableField("group_hours")
    private Integer groupHours;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("sales_count")
    private Integer salesCount;

    @TableField("locked_stock")
    private Integer lockedStock;

    @TableField("sold_stock")
    private Integer soldStock;

    @TableField("version")
    private Integer version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // ========== 非表字段（关联查询） ==========

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private Boolean isSoldOut;

    public Boolean getIsSoldOut() {
        return remainingStock == null || remainingStock <= 0;
    }

    public String getGroupPriceDisplay() {
        return groupPrice != null ? "¥" + groupPrice.setScale(2, BigDecimal.ROUND_HALF_UP) : "¥0.00";
    }

    public String getDiscountDisplay() {
        if (originalPrice == null || groupPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        BigDecimal discount = groupPrice.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.TEN);
        return discount.setScale(0, BigDecimal.ROUND_HALF_UP) + "折";
    }

    public Integer getAvailableStock() {
        return (remainingStock != null ? remainingStock : 0) - (lockedStock != null ? lockedStock : 0);
    }
}