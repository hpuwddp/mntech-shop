package com.mntech.shop.consumer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.model.OrderItemModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper  extends BaseMapper<OrderItemModel> {
    /**
     * 根据订单号查询订单明细
     */
    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo} AND deleted = 0")
    List<OrderItemModel> selectByOrderNo(@Param("orderNo") String orderNo);


    /**
     * 根据订单号和SKU ID查询
     */
    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo} AND sku_id = #{skuId} AND deleted = 0")
    OrderItemModel selectByOrderNoAndSkuId(@Param("orderNo") String orderNo, @Param("skuId") String skuId);

    /**
     * 根据用户ID查询订单明细
     */
    @Select("SELECT * FROM order_item WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderItemModel> selectByUserId(@Param("userId") String userId);

    /**
     * 批量插入订单明细
     */
    int batchInsert(@Param("list") List<OrderItemModel> list);
}
