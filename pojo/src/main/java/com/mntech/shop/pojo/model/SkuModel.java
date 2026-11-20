package com.mntech.shop.pojo.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU实体
 */
@Data
@TableName("product_sku")
public class SkuModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("product_id")
    private String productId;

    @TableField("sku_code")
    private String skuCode;

    @TableField("sku_name")
    private String skuName;

    @TableField("attributes")
    private String attributes; // JSON字符串

    @TableField("attribute_hash")
    private String attributeHash;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("selling_price")
    private BigDecimal sellingPrice;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("promotion_price")
    private BigDecimal promotionPrice;

    @TableField("promotion_start")
    private LocalDateTime promotionStart;

    @TableField("promotion_end")
    private LocalDateTime promotionEnd;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("available_stock")
    private Integer availableStock;

    @TableField("locked_stock")
    private Integer lockedStock;

    @TableField("sold_stock")
    private Integer soldStock;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("volume")
    private BigDecimal volume;

    @TableField("image_url")
    private String imageUrl;

    @TableField("status")
    private String status;

    @TableField("sales_count")
    private Integer salesCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    @TableField("version")
    private Integer version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}