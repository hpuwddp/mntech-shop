package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销佣金比例配置表
 */
@Data
@TableName("distribution_config")
public class DistributionConfigModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 分销层级：1/2/3 */
    @TableField("level")
    private Integer level;

    /** 佣金比例（如 0.10 表示 10%） */
    @TableField("rate")
    private BigDecimal rate;

    /** 是否启用：1 启用 / 0 停用 */
    @TableField("enabled")
    private Integer enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}