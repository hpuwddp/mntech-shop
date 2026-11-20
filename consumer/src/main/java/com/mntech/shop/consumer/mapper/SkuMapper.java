package com.mntech.shop.consumer.mapper;

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
     * 批量扣减库存
     */
    @Update("<script>" +
            "UPDATE product_sku SET " +
            "available_stock = available_stock - " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", locked_stock = locked_stock - " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", sold_stock = sold_stock + " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "   #{item.skuId}" +
            "</foreach> " +
            "AND deleted = 0 " +
            "AND (" +
            "<foreach collection='list' item='item' separator=' OR '>" +
            "   (id = #{item.skuId} AND available_stock >= #{item.quantity})" +
            "</foreach>" +
            ")" +
            "</script>")
    int batchReduceStock(@Param("list") List<SkuStockUpdate> list);


    /**
     * 批量回滚库存（带乐观锁）
     */
    @Update("<script>" +
            "UPDATE product_sku SET " +
            "available_stock = available_stock + " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> " +
            ", locked_stock = locked_stock - " +
            "<foreach collection='list' item='item' separator=' ' open='CASE id' close='END'>" +
            "   WHEN #{item.skuId} THEN #{item.quantity}" +
            "</foreach> "  +

            ", version = version + 1, updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "   #{item.skuId}" +
            "</foreach> " +
            "AND deleted = 0 " +
            "AND (" +
            "<foreach collection='list' item='item' separator=' OR '>" +
            "   (id = #{item.skuId} AND version = #{item.version})" +  // ✅ 乐观锁条件
            "</foreach>" +
            ")" +
            "</script>")
    int batchRollbackStock(@Param("list") List<SkuStockUpdate> list);


    /**
     * 根据 SKU ID 列表批量查询（带排序）
     */
    @Select("<script>" +
            "SELECT * FROM product_sku " +
            "WHERE id IN " +
            "<foreach collection='skuIds' item='skuId' open='(' separator=',' close=')'>" +
            "   #{skuId}" +
            "</foreach> " +
            "AND deleted = 0 " +
            "ORDER BY FIELD(id, " +
            "<foreach collection='skuIds' item='skuId' separator=','>#{skuId}</foreach>" +
            ")" +
            "</script>")
    List<SkuModel> selectByIdsOrdered(@Param("skuIds") List<String> skuIds);
}
