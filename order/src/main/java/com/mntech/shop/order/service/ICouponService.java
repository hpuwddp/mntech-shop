package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.CouponReceiveRequest;
import com.mntech.shop.order.dto.request.CouponUseRequest;
import com.mntech.shop.order.dto.request.CreateOrderRequest;
import com.mntech.shop.order.dto.response.CouponCalcResult;
import com.mntech.shop.order.dto.response.CouponResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ICouponService {
    /**
     * 领取优惠券
     */
    CouponResponse receiveCoupon(CouponReceiveRequest request);

    /**
     * 获取用户优惠券列表
     */
    List<CouponResponse> getUserCoupons(String userId);

    /**
     * 获取用户可用优惠券
     */
    List<CouponResponse> getAvailableCoupons(String userId);

    /**
     * 获取用户已使用优惠券
     */
    List<CouponResponse> getUsedCoupons(String userId);

    /**
     * 获取用户已过期优惠券
     */
    List<CouponResponse> getExpiredCoupons(String userId);

    /**
     * 使用优惠券
     */
    boolean useCoupon(CouponUseRequest request);

    /**
     * 获取可领取的优惠券列表
     */
    List<CouponResponse> getAvailableToReceive(String userId);

/**
     * 统计用户优惠券数量
     */
    int countUserCoupons(String userId);

    /**
     * 订单计价时校验优惠券并计算抵扣金额（不核销）。
     * 校验：归属/未使用/有效期内/ACTIVE/门槛/适用范围；userCouponId 为空返回抵扣 0。
     *
     * @param userId      用户ID
     * @param userCouponId 用户优惠券ID（user_coupon.id），可空
     * @param orderAmount 订单商品原价总额
     * @param items       订单商品明细（用于适用范围校验）
     * @return 抵扣计算结果
     */
    CouponCalcResult calcCouponDiscount(String userId, String userCouponId, BigDecimal orderAmount,
                                        List<CreateOrderRequest.OrderItemRequest> items);

    /**
     * 订单创建时应用优惠券：内部先 calcCouponDiscount 校验计算，再核销用户券并回写使用计数。
     * userCouponId 为空时直接返回抵扣 0。
     *
     * @param orderAmount 订单商品原价总额
     * @param items       订单商品明细（用于适用范围校验）
     * @return 抵扣计算结果
     */
    CouponCalcResult applyCouponForOrder(String userId, String userCouponId, String orderNo,
                                         BigDecimal orderAmount, List<CreateOrderRequest.OrderItemRequest> items);

    /**
     * 返还优惠券（订单取消/换券）：恢复 user_coupon 为未使用并回减使用计数
     */
    boolean refundCouponForOrder(String userCouponId);
}
