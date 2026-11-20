package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.request.CreateOrderRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.*;

import java.util.List;

public interface IPayService {
    /**
     * 创建订单并发起微信支付
     */
    WechatPayResponse createOrderAndPay(CreateOrderRequest request);

    OrderResponse createOrderOnly(CreateOrderRequest request);

    WechatPayResponse payOrder(String userId, String orderNo);

    void updateAddress(String orderNo, AddressRequest request);

    /**
     * 微信支付回调处理
     */
    void handleWechatNotify(String body, String signature, String serial, String timestamp, String nonce);

    void testpaySuccess(String orderNo);

    /**
     * 查询订单状态
     */
    String getOrderStatus(String orderNo);

    /**
     * 查询订单详情（含明细）
     */
    OrderResponse getOrderDetail(String orderNo);

    /**
     * 查询用户订单列表
     */
    PageResponse<OrderResponse> getUserOrders(OrderQueryRequest pageRequest);

    /**
     * 根据用户ID和状态查询订单
     */
    PageResponse<OrderResponse> getUserOrdersByStatus(OrderQueryRequest pageRequest);

/**
     * 取消订单
     */
    boolean cancelOrder(String orderNo);

    /**
     * 给待支付订单应用/更换/清除优惠券（userCouponId 为空表示不使用优惠券）
     */
    OrderResponse applyCouponToOrder(String orderNo, String userCouponId);

    /**
     * 确认收货
     */
    boolean confirmReceipt(String orderNo);

    /**
     * 获取用户订单统计
     */
    OrderStatisticsResponse getOrderStatistics(String userId);

    /**
     * 查询微信支付订单状态
     */
    WechatPayStatusResponse queryWechatPayStatus(String orderNo);
}
