package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.SkuModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapper<SkuModel> {
    /**
     * 根据产品ID查询SKU列表
     */
    @Select("SELECT * FROM product_sku WHERE product_id = #{productId} AND deleted = 0")
    List<SkuModel> selectByProductId(@Param("productId") String productId);

    /**
     * 根据产品ID查询有效SKU列表
     */
    @Select("SELECT * FROM product_sku WHERE product_id = #{productId} AND status = 'ACTIVE' AND deleted = 0")
    List<SkuModel> selectActiveByProductId(@Param("productId") String productId);


    /**
     * 锁定SKU库存（乐观锁）
     * ✅ WHERE 条件中加上 version
     */
    @Update("UPDATE product_sku SET " +
            "available_stock = available_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP, " +
            "version = version + 1 " +
            "WHERE id = #{id} " +
            "AND available_stock >= #{quantity} " +
            "AND version = #{version} " +
            "AND deleted = 0")
    int reduceStock(@Param("id") String id,
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);


    /**
     * 释放锁定SKU库存（乐观锁）
     */
    @Update("UPDATE product_sku SET " +
            "available_stock = available_stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP, " +
            "version = version + 1 " +
            "WHERE id = #{skuId} " +
            "AND locked_stock >= #{quantity} " +
            "AND version = #{version} " +
            "AND deleted = 0")
    int increaseStock(@Param("skuId") String skuId,
                      @Param("quantity") Integer quantity,
                      @Param("version") Integer version);

    /**
     * 确认扣减SKU库存（乐观锁）
     */
    @Update("UPDATE product_sku SET " +
            "sold_stock = sold_stock + #{quantity}, " +
            "sales_count = sales_count + #{quantity}, " +
            "updated_at = CURRENT_TIMESTAMP, " +
            "version = version + 1 " +
            "WHERE id = #{skuId} " +
            "AND version = #{version} " +
            "AND deleted = 0")
    int confirmStock(@Param("skuId") String skuId,
                     @Param("quantity") Integer quantity,
                     @Param("version") Integer version);

    /**
     * ✅ 根据SKU ID列表批量查询
     */
    @Select("<script>" +
            "SELECT * FROM product_sku WHERE id IN " +
            "<foreach collection='skuIds' item='id' open='(' separator=',' close=')'>" +
            "   #{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    List<SkuModel> selectByIds(@Param("skuIds") List<String> skuIds);

    /**
     * 批量扣减库存（一条 SQL 更新多个 SKU）
     * @param list SKU 扣减列表
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE product_sku SET " +
            "available_stock = available_stock - " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", locked_stock = locked_stock + " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", version = version + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "   #{item.skuId}" +
            "</foreach> " +
            "AND deleted = 0 " +
            "AND (" +
            "<foreach collection='list' item='item' separator=' OR '>" +
            "   (id = #{item.skuId} AND available_stock >= #{item.quantity} AND version = #{item.version})" +
            "</foreach>" +
            ")" +
            "</script>")
    int batchReduceStock(@Param("list") List<SkuStockUpdate> list);
}
