package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.CartModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<CartModel> {

    /**
     * 查询用户购物车
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<CartModel> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户选中的购物车项
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND selected = TRUE AND deleted = 0")
    List<CartModel> selectSelectedByUserId(@Param("userId") String userId);

    /**
     * 清空用户购物车
     */
    @Update("UPDATE cart SET deleted = 1, updated_at = NOW() WHERE user_id = #{userId}")
    int clearCart(@Param("userId") String userId);

    /**
     * 清空用户选中的购物车项
     */
    @Update("UPDATE cart SET deleted = 1, updated_at = NOW() WHERE user_id = #{userId} AND selected = TRUE")
    int clearSelected(@Param("userId") String userId);

    /**
     * 根据用户ID和商品ID查询购物车
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId} AND sku_id = #{skuId} AND deleted = 0")
    CartModel selectByUserAndProduct(@Param("userId") String userId, @Param("productId") String productId, @Param("skuId") String skuId);

    /**
     * 根据用户ID和SKU ID列表批量删除购物车商品（逻辑删除）
     */
    @Update({
            "<script>",
            "UPDATE cart SET deleted = 1, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0 AND sku_id IN ",
            "<foreach collection='skuIds' item='skuId' open='(' separator=',' close=')'>",
            "#{skuId}",
            "</foreach>",
            "</script>"
    })
    int batchDeleteByUserIdAndSkuIds(@Param("userId") String userId, @Param("skuIds") List<String> skuIds);

}
