package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.dto.SeckillOrderStats;
import com.mntech.shop.order.model.SeckillOrderModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrderModel> {

    /**
     * 获取用户秒杀订单列表
     */
    @Select("SELECT so.*, p.name as product_name, p.main_image as image_url " +
            "FROM seckill_order so " +
            "LEFT JOIN seckill_product sp ON so.seckill_product_id = sp.id " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "WHERE so.user_id = #{userId} " +
            "AND so.deleted = 0 " +
            "ORDER BY so.created_at DESC")
    List<SeckillOrderModel> selectByUserId(@Param("userId") String userId);

    /**
     * 获取用户秒杀订单列表（按状态）
     */
    @Select("SELECT so.*, p.name as product_name, p.main_image as image_url " +
            "FROM seckill_order so " +
            "LEFT JOIN seckill_product sp ON so.seckill_product_id = sp.id " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "WHERE so.user_id = #{userId} " +
            "AND so.deleted = 0 AND so.status = #{status} " +
            "ORDER BY so.created_at DESC")
    List<SeckillOrderModel> selectByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);


    /**
     * 获取用户对某个商品的购买数量（用于限购校验）
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM seckill_order " +
            "WHERE user_id = #{userId} " +
            "AND seckill_product_id = #{seckillProductId} " +
            "AND status IN ('PENDING', 'PAID') " +
            "AND deleted = 0")
    Integer countUserBuy(@Param("userId") String userId, @Param("seckillProductId") String seckillProductId);

    /**
     * 检查用户是否已购买（限购校验）
     */
    @Select("SELECT COUNT(*) FROM seckill_order " +
            "WHERE user_id = #{userId} " +
            "AND seckill_product_id = #{seckillProductId} " +
            "AND status IN ('PENDING', 'PAID') " +
            "AND deleted = 0")
    int countUserBuyCount(@Param("userId") String userId, @Param("seckillProductId") String seckillProductId);

    /**
     * 支付订单
     */
    @Update("UPDATE seckill_order SET status = 'PAID', updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'PENDING' ")
    int payOrder(@Param("id") String id);

    /**
     * 取消订单
     */
    @Update("UPDATE seckill_order SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'PENDING' ")
    int cancelOrder(@Param("id") String id);

    /**
     * 更新订单状态为已支付
     */
    @Update("UPDATE seckill_order SET status = 'PAID', pay_time =#{payTime}, transaction_id = #{transactionId} " +
            "WHERE id = #{id} AND status = 'PENDING'")
    int updatePaySuccess(@Param("id") String id, @Param("transactionId") String transactionId, @Param("payTime") LocalDateTime payTime);


    /**
     * 获取待支付订单（超时取消，15分钟，PostgreSQL 语法）
     */
    @Select("SELECT * FROM seckill_order " +
            "WHERE status = 'PENDING' " +
            "AND deleted = 0 " +
            "AND created_at < NOW() - INTERVAL '15 minutes'")
    List<SeckillOrderModel> selectPendingTimeoutOrders();

    /**
     * 获取待支付订单（超时取消，PostgreSQL版本）
     */
    @Select("SELECT * FROM seckill_order " +
            "WHERE status = 'PENDING' " +
            "AND deleted = 0 " +
            "AND created_at < NOW() - INTERVAL '15 minutes'")
    List<SeckillOrderModel> selectPendingTimeoutOrdersPg();

    /**
     * 获取活动订单统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_orders, " +
            "SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_orders, " +
            "SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) as paid_orders, " +
            "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_orders, " +
            "SUM(CASE WHEN status = 'PAID' THEN quantity ELSE 0 END) as total_sales " +
            "FROM seckill_order " +
            "WHERE activity_id = #{activityId} " +
            "AND deleted = 0")
    SeckillOrderStats selectOrderStats(@Param("activityId") String activityId);

    /**
     * 获取用户秒杀订单数量
     */
    @Select("SELECT COUNT(*) FROM seckill_order WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") String userId);

    /**
     * 获取用户秒杀订单数量（按状态）
     */
    @Select("SELECT COUNT(*) FROM seckill_order " +
            "WHERE user_id = #{userId} " +
            "AND status = #{status} " +
            "AND deleted = 0")
    int countByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
}