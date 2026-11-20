package com.mntech.shop.pojo.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItemModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("order_no")
    private String orderNo;

    @TableField("product_id")
    private String productId;

    @TableField("sku_id")
    private String skuId;
    @TableField("user_id")
    private String userId;
    @TableField("product_name")
    private String productName;

    @TableField("sku_name")
    private String skuName;

    @TableField("product_image")
    private String productImage;

    @TableField("quantity")
    private Integer quantity;

    @TableField("price")
    private BigDecimal price;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
