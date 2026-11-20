package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.CategoryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryModel> {
    /**
     * 根据父级ID查询子分类
     */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<CategoryModel> selectByParentId(@Param("parentId") String parentId);

    /**
     * 查询所有一级分类（parent_id为null）
     */
    @Select("SELECT * FROM category WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<CategoryModel> selectRootCategories();

    /**
     * 查询所有启用的分类
     */
    @Select("SELECT * FROM category WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<CategoryModel> selectActiveCategories();

    /**
     * 根据层级查询分类
     */
    @Select("SELECT * FROM category WHERE level = #{level} AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<CategoryModel> selectByLevel(@Param("level") Integer level);

    /**
     * 查询分类及其所有子分类（递归查询）
     */
    @Select("WITH RECURSIVE category_tree AS (" +
            "  SELECT * FROM category WHERE id = #{categoryId} AND deleted = 0" +
            "  UNION ALL" +
            "  SELECT c.* FROM category c" +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.id" +
            "  WHERE c.deleted = 0" +
            ") SELECT * FROM category_tree")
    List<CategoryModel> selectCategoryTree(@Param("categoryId") String categoryId);

    /**
     * 检查分类编码是否存在
     */
    @Select("SELECT COUNT(*) FROM category WHERE code = #{code} AND deleted = 0")
    int countByCode(@Param("code") String code);

    /**
     * 检查分类名称是否在兄弟节点中重复
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId} AND name = #{name} AND deleted = 0")
    int countByParentIdAndName(@Param("parentId") String parentId, @Param("name") String name);
}
