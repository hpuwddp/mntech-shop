package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.CouponReceiveRequest;
import com.mntech.shop.order.dto.request.CouponUseRequest;
import com.mntech.shop.order.dto.response.CouponResponse;
import com.mntech.shop.order.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {
    private final ICouponService couponService;

    /**
     * 获取用户所有优惠券
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CouponResponse>> getUserCoupons(@PathVariable String userId) {
        List<CouponResponse> coupons = couponService.getUserCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 获取用户可用优惠券
     */
    @GetMapping("/user/{userId}/available")
    public ResponseEntity<List<CouponResponse>> getAvailableCoupons(@PathVariable String userId) {
        List<CouponResponse> coupons = couponService.getAvailableCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 获取用户已使用优惠券
     */
    @GetMapping("/user/{userId}/used")
    public ResponseEntity<List<CouponResponse>> getUsedCoupons(@PathVariable String userId) {
        List<CouponResponse> coupons = couponService.getUsedCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 获取用户已过期优惠券
     */
    @GetMapping("/user/{userId}/expired")
    public ResponseEntity<List<CouponResponse>> getExpiredCoupons(@PathVariable String userId) {
        List<CouponResponse> coupons = couponService.getExpiredCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/receive")
    public ResponseEntity<CouponResponse> receiveCoupon(@Valid @RequestBody CouponReceiveRequest request) {
        CouponResponse response = couponService.receiveCoupon(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 使用优惠券
     */
    @PostMapping("/use")
    public ResponseEntity<Map<String, Object>> useCoupon(@Valid @RequestBody CouponUseRequest request) {
        boolean result = couponService.useCoupon(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", result ? "优惠券使用成功" : "优惠券使用失败");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取可领取的优惠券列表
     */
    @GetMapping("/available/{userId}")
    public ResponseEntity<List<CouponResponse>> getAvailableToReceive(@PathVariable String userId) {
        List<CouponResponse> coupons = couponService.getAvailableToReceive(userId);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 统计用户优惠券数量
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Object>> countUserCoupons(@PathVariable String userId) {
        int count = couponService.countUserCoupons(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
