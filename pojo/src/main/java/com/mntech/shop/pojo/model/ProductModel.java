package com.mntech.shop.pojo.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product")
public class ProductModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("spu_code")
    private String spuCode;

    @TableField("name")
    private String name;

    @TableField("subtitle")
    private String subtitle;

    @TableField("description")
    private String description;

    @TableField("brand_id")
    private String brandId;

    @TableField("category_id")
    private String categoryId;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("selling_price")
    private BigDecimal sellingPrice;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("member_price")
    private BigDecimal memberPrice;

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

    @TableField("status")
    private String status;

    @TableField("audit_status")
    private String auditStatus;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("auditor_id")
    private String auditorId;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("has_sku")
    private Boolean hasSku;

    @TableField("sku_template")
    private String skuTemplate; // JSON字符串

    @TableField("weight")
    private BigDecimal weight;

    @TableField("volume")
    private BigDecimal volume;

//    @TableField("shipping_template_id")
//    private String shippingTemplateId;

    @TableField("delivery_time")
    private String deliveryTime;

    @TableField("main_image")
    private String mainImage;

    @TableField("images")
    private String images; // JSON数组字符串

    @TableField("video_url")
    private String videoUrl;

    @TableField("tags")
    private String tags; // JSON数组字符串

    @TableField("sales_count")
    private Integer salesCount;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField("review_count")
    private Integer reviewCount;

    @TableField("rating")
    private BigDecimal rating;

    @TableField("created_by")
    private String createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Version
    @TableField("version")
    private Integer version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
