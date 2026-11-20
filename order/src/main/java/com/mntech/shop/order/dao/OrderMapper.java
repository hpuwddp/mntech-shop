package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.pojo.model.OrderModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<OrderModel> {
    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND deleted = 0")
    OrderModel selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单状态为已支付
     */
    @Update("UPDATE orders SET status = '${@com.mntech.shop.common.enums.OrderStatus@PAID.name()}' , pay_time =#{payTime}, transaction_id = #{transactionId} " +
            "WHERE order_no = #{orderNo} AND status = '${@com.mntech.shop.common.enums.OrderStatus@PENDING.name()}' ")
    int updatePaySuccess(@Param("orderNo") String orderNo, @Param("transactionId") String transactionId, @Param("payTime") LocalDateTime payTime);

    /**
     * 根据用户ID查询所有订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderModel> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和状态查询订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderModel> selectByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

/**
     * 取消订单
     */
    @Update("UPDATE orders SET status = '${@com.mntech.shop.common.enums.OrderStatus@CANCELLED.name()}', updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = '${@com.mntech.shop.common.enums.OrderStatus@PENDING.name()}' ")
    int cancelOrder(@Param("orderNo") String orderNo);

    /**
     * 清除订单优惠券信息（显式置 NULL，避免 MyBatis-Plus 非空更新策略不写 NULL 导致旧券残留）
     */
    @Update("UPDATE orders SET coupon_id = NULL, coupon_name = NULL, discount_amount = 0, pay_amount = total_amount, updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND deleted = 0")
    int clearCoupon(@Param("orderNo") String orderNo);

    /**
     * 确认收货
     */
    @Update("UPDATE orders SET status = '${@com.mntech.shop.common.enums.OrderStatus@COMPLETED.name()}', updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = '${@com.mntech.shop.common.enums.OrderStatus@PAID.name()}' ")
    int confirmReceipt(@Param("orderNo") String orderNo);


    // ==================== ✅ 订单统计 ====================

    /**
     * 统计用户所有订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND deleted = 0")
    int countAll(@Param("userId") String userId);

    /**
     * 统计用户待付款订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = '${@com.mntech.shop.common.enums.OrderStatus@PENDING.name()}' AND deleted = 0")
    int countPending(@Param("userId") String userId);

    /**
     * 统计用户待发货订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = '${@com.mntech.shop.common.enums.OrderStatus@PAID.name()}' AND deleted = 0")
    int countPaid(@Param("userId") String userId);

    /**
     * 统计用户待收货订单数量（已发货/配送中）
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = 'SHIPPED' AND deleted = 0")
    int countShipped(@Param("userId") String userId);

    /**
     * 统计用户待评价订单数量（已完成未评价）
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = '${@com.mntech.shop.common.enums.OrderStatus@COMPLETED.name()}' AND deleted = 0")
    int countCompleted(@Param("userId") String userId);

}
