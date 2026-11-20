package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.request.SeckillBuyRequest;
import com.mntech.shop.order.dto.response.OrderResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.dto.response.SeckillActivityResponse;
import com.mntech.shop.order.dto.response.SeckillOrderResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ISeckillService {
    // ==================== 用户端 ====================

    /**
     * 获取进行中的秒杀活动列表
     */
    List<SeckillActivityResponse> getOngoingActivities();

    /**
     * 获取秒杀活动详情（用户端）
     */
    SeckillActivityResponse getActivityDetailForUser(String activityId);

    /**
     * 秒杀下单
     */
    SeckillOrderResponse seckillBuy(SeckillBuyRequest request);

    /**
     * 获取剩余库存
     */
    Integer getRemainingStock(String seckillProductId);

    /**
     * 获取用户秒杀订单列表
     */
    List<SeckillOrderResponse> getUserSeckillOrders(String userId);

    /**
     * 查询用户订单列表
     */
    PageResponse<SeckillOrderResponse> getUserSeckillOrders(OrderQueryRequest pageRequest);

    /**
     * 取消秒杀订单
     */
    void cancelSeckillOrder(String orderId);

    /**
     * 支付秒杀订单
     */
    SeckillOrderResponse paySeckillOrder(String orderId);

    // ==================== 定时任务 ====================



    /**
     * 获取秒杀订单详情
     */
    SeckillOrderResponse getSeckillOrderDetail(String orderId);

    /**
     * 创建订单
     * 扣减库存
     * @param userId
     * @param productId
     * @param num
     */
    void createOrderAndDecreaseStock(String userId, String productId, int num, String activityId, BigDecimal seckillPrice);
}
