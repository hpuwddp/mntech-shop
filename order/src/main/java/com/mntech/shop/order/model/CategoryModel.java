package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体
 * 用于商品的多级分类管理
 */
@Data
@TableName("category")
public class CategoryModel {

    /**
     * 分类ID，使用UUID
     * 主键，自动生成
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 父级分类ID
     * 用于构建树形结构，如果为null则表示一级分类
     * 例如：服装 -> 男装 -> 羽绒服
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 分类名称
     * 例如：电子产品、服装、手机、笔记本电脑
     */
    @TableField("name")
    private String name;

    /**
     * 分类编码
     * 系统唯一标识，例如：ELECTRONICS、PHONE、LAPTOP
     */
    @TableField("code")
    private String code;

    /**
     * 分类层级
     * 1-一级分类，2-二级分类，3-三级分类
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序顺序
     * 数值越小越靠前
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 分类图标
     * 图标地址或CSS类名
     */
    @TableField("icon")
    private String icon;

    /**
     * 分类图片
     * 展示用图片地址
     */
    @TableField("image")
    private String image;

    /**
     * 分类描述
     * 详细说明
     */
    @TableField("description")
    private String description;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     * 自动填充
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 自动填充
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}