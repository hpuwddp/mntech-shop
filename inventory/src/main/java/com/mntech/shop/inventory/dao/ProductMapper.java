package com.mntech.shop.inventory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.model.ProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<ProductModel> {

    /**
     * 扣减库存（下单/支付成功后调用）
     * 减少总库存，增加已售库存
     */
    @Update("UPDATE product SET total_stock = total_stock - #{quantity}, " +
            "sold_stock = sold_stock + #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{productId} " +
            "AND total_stock >= #{quantity} " +
            "AND version = #{version} " +
            "AND deleted = 0")
    int deductStock(@Param("productId") String productId,
                    @Param("quantity") Integer quantity,
                    @Param("version") Integer version);

    /**
     * 回滚库存（订单取消/退款时调用）
     * 增加总库存，减少已售库存
     */
    @Update("UPDATE product SET total_stock = total_stock + #{quantity}, " +
            "sold_stock = sold_stock - #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{productId} " +
            "AND sold_stock >= #{quantity} " +
            "AND version = #{version} " +
            "AND deleted = 0")
    int rollbackStock(@Param("productId") String productId,
                      @Param("quantity") Integer quantity,
                      @Param("version") Integer version);
}