package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.model.OrderItemModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemModel> {
    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo} AND deleted = 0")
    List<OrderItemModel> selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM order_item WHERE product_id = #{productId} AND deleted = 0")
    List<OrderItemModel> selectByProductId(@Param("productId") String productId);
}
