package com.mntech.shop.consumer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.consumer.model.SeckillOrderModel;
import com.mntech.shop.pojo.model.OrderModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper  extends BaseMapper<OrderModel> {
    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND deleted = 0")
    OrderModel selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据订单号更新订单
     */
    @Update("UPDATE orders SET status = #{order.status}, " +
            "updated_at = #{order.updatedAt} " +
            "WHERE order_no = #{order.orderNo} AND status = 'PENDING' AND deleted = 0")
    int updateByOrderNo(@Param("order") OrderModel order);

    /**
     * 取消订单（直接更新状态）
     */
    @Update("UPDATE orders SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = 'PENDING' AND deleted = 0")
    int cancelOrder(@Param("orderNo") String orderNo);
}
