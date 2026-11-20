package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 品牌实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("brand")
public class BrandModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌ID，使用UUID
     * 主键，自动生成UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 品牌名称
     * 例如：Apple、Samsung、Xiaomi
     */
    @TableField("name")
    private String name;

    /**
     * 品牌编码
     * 例如：APPLE、SAMSUNG、XIAOMI
     * 用于系统内部标识
     */
    @TableField("code")
    private String code;

    /**
     * 品牌Logo URL
     * 品牌图标的图片地址
     */
    @TableField("logo")
    private String logo;

    /**
     * 品牌描述
     * 品牌的详细介绍信息
     */
    @TableField("description")
    private String description;

    /**
     * 排序顺序
     * 数值越小越靠前，用于前台展示排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     * 控制品牌是否在前台显示
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     * 记录创建时间，自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 记录最后更新时间，自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0-未删除，1-已删除
     * 软删除标记，删除时不会真正从数据库移除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}