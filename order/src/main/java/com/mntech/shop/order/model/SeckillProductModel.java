package com.mntech.shop.order.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")
public class SeckillProductModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("activity_id")
    private String activityId;

    @TableField("product_id")
    private String productId;

    @TableField("sku_id")
    private String skuId;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("remaining_stock")
    private Integer remainingStock;

    @TableField("per_user_limit")
    private Integer perUserLimit;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    /**
     * 锁定库存（下单锁定，支付扣减，超时释放）
     */
    @TableField("locked_stock")
    private Integer lockedStock;

    /**
     * 已售库存
     */
    @TableField("sold_stock")
    private Integer soldStock;
    @TableField("sales_count")
    private Integer salesCount;
    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
    // ========== 非表字段（关联查询） ==========

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private BigDecimal originalPrice;

    @TableField(exist = false)
    private String skuName;

    @TableField(exist = false)
    private Boolean isSoldOut;

    public Boolean getIsSoldOut() {
        return remainingStock == null || remainingStock <= 0;
    }

    public String getSeckillPriceDisplay() {
        return seckillPrice != null ? "¥" + seckillPrice.setScale(2, BigDecimal.ROUND_HALF_UP) : "¥0.00";
    }

    public String getDiscountDisplay() {
        if (originalPrice == null || seckillPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        BigDecimal discount = seckillPrice.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.TEN);
        return discount.setScale(0, BigDecimal.ROUND_HALF_UP) + "折";
    }

    /**
     * 可售库存 = remaining_stock - locked_stock
     */
    public Integer getAvailableStock() {
        return (remainingStock != null ? remainingStock : 0) - (lockedStock != null ? lockedStock : 0);
    }
}