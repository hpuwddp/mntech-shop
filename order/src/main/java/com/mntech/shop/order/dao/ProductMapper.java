package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.model.ProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<ProductModel> {
    /**
     * 查询上架产品
     */
    @Select("SELECT * FROM product WHERE status = 'ON_SHELF' AND available_stock > 0 AND deleted = 0")
    List<ProductModel> selectOnShelfProducts();

    /**
     * 查询促销产品
     */
    @Select("SELECT * FROM product WHERE promotion_price IS NOT NULL " +
            "AND promotion_start <= CURRENT_TIMESTAMP " +
            "AND promotion_end >= CURRENT_TIMESTAMP " +
            "AND status = 'ON_SHELF' " +
            "AND deleted = 0")
    List<ProductModel> selectPromotionProducts();

    /**
     * 更新产品状态
     */
    @Select("UPDATE product SET status = #{status}, updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
            "WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    /**
     * 扣减库存
     */
    @Select("UPDATE product SET available_stock = available_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
            "WHERE id = #{id} AND available_stock >= #{quantity} AND deleted = 0")
    int reduceStock(@Param("id") String id, @Param("quantity") Integer quantity);

    /**
     * 增加库存
     */
    @Select("UPDATE product SET total_stock = total_stock + #{quantity}, " +
            "available_stock = available_stock + #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP, version = version + 1 " +
            "WHERE id = #{id} AND deleted = 0")
    int increaseStock(@Param("id") String id, @Param("quantity") Integer quantity);

    /**
     * 根据分类ID查询产品（包含子分类）
     */
    @Select("SELECT DISTINCT p.* FROM product p " +
            "WHERE p.category_id IN (" +
            "  WITH RECURSIVE category_tree AS (" +
            "    SELECT id FROM category WHERE id = #{categoryId} AND deleted = 0 " +
            "    UNION ALL " +
            "    SELECT c.id FROM category c " +
            "    INNER JOIN category_tree ct ON c.parent_id = ct.id " +
            "    WHERE c.deleted = 0" +
            "  ) SELECT id FROM category_tree" +
            ") " +
            "AND p.deleted = 0 " +
            "AND p.status IN ('ON_SHELF', 'OFF_SHELF', 'APPROVED') " +
            "ORDER BY p.sales_count DESC, p.created_at DESC")
    List<ProductModel> selectByCategoryId(@Param("categoryId") String categoryId);

}
