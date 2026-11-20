package com.mntech.shop.order.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONConverter;
import cn.hutool.json.JSONUtil;
import com.mntech.shop.common.Feign.InventoryFeignClient;
import com.mntech.shop.common.enums.OrderStatus;
import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.request.ApplyCouponRequest;
import com.mntech.shop.order.dto.request.CreateOrderRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.*;
import com.mntech.shop.order.service.IPayService;
import com.mntech.shop.order.util.LocalUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Slf4j
public class PayController {
    private final IPayService payService;
    private final InventoryFeignClient inventoryFeignClient;
    /**
     * 创建订单并发起支付
     */
    @PostMapping("/create")
    public ResponseEntity<WechatPayResponse> createOrder(@RequestBody CreateOrderRequest request,HttpServletRequest httpRequest) {


            WechatPayResponse response = payService.createOrderAndPay(request);
            return ResponseEntity.ok(response);

    }

    /**
     * 只创建订单
     * 不支付
     *
     * @param request
     * @return
     */
    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponse> createOrderOnly(@Valid @RequestBody CreateOrderRequest request,HttpServletRequest httpRequest) {
        String currentUserId = LocalUserUtil.getCurrentUserId(httpRequest);

        if (currentUserId == null) {
            return ResponseEntity.status(401).body(null);
        }
        else
        {
            request.setUserId(currentUserId);
            OrderResponse response = payService.createOrderOnly(request);
            return ResponseEntity.ok(response);
        }


    }

    /**
     * 压力测试时候使用
     * 支付成功以后
     * 扣减库存逻辑测试
     *
     * @param request
     * @return
     */
    @PostMapping("/paySuccess")
    public ResponseEntity<Map<String, Object>> paySuccess(@Valid @RequestBody CreateOrderRequest request) {

        Map<String, Object> result = new HashMap<>();
        payService.testpaySuccess(request.getOrderNo());
        return ResponseEntity.ok(result);
    }

/**
     * ✅ 给待支付订单应用/更换/清除优惠券
     * userCouponId 为空表示不使用优惠券（清除已选券）
     */
    @PostMapping("/order/{orderNo}/coupon")
    public ResponseEntity<OrderResponse> applyCoupon(@PathVariable String orderNo,
                                                     @RequestBody ApplyCouponRequest request,
                                                     HttpServletRequest httpRequest) {
        String currentUserId = LocalUserUtil.getCurrentUserId(httpRequest);
        if (currentUserId != null) {
            request.setUserId(currentUserId);
        }
        OrderResponse response = payService.applyCouponToOrder(orderNo, request.getUserCouponId());
        return ResponseEntity.ok(response);
    }

    // ========== ✅ 新增接口4：发起支付 ==========
    @PostMapping("/order/{orderNo}/pay")
    public ResponseEntity<WechatPayResponse> payOrder(@RequestBody CreateOrderRequest createOrderRequest, @PathVariable String orderNo) {

        WechatPayResponse response = payService.payOrder(createOrderRequest.getUserId(), orderNo);
        return ResponseEntity.ok(response);
    }

    // ========== ✅ 新增接口3：更新订单地址 ==========
    @PutMapping("/order/{orderNo}/address")
    public ResponseEntity<Void> updateAddress(
            @PathVariable String orderNo,
            @RequestBody AddressRequest request) {
        payService.updateAddress(orderNo, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 微信支付回调
     * 这里应该加一个 将产品从购物车里删除
     */
    @PostMapping("/notify")
    public String wechatNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println("微信支付回调函数");
            // 获取请求头
            String signature = request.getHeader("Wechatpay-Signature");
            String serial = request.getHeader("Wechatpay-Serial");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            // 读取请求体
            BufferedReader reader = request.getReader();
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            // 处理回调
            payService.handleWechatNotify(body.toString(), signature, serial, timestamp, nonce);
            System.out.println("success");
            // 返回成功响应
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";

        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
        }
    }

    /**
     * ✅ 查询微信支付状态
     */
    @GetMapping("/WechatPaystatus/{orderNo}")
    public ResponseEntity<WechatPayStatusResponse> queryPayStatus(@PathVariable String orderNo) {
        WechatPayStatusResponse response = payService.queryWechatPayStatus(orderNo);
        return ResponseEntity.ok(response);
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/status/{orderNo}")
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable String orderNo) {
        String status = payService.getOrderStatus(orderNo);
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", status);
        result.put("isPaid", OrderStatus.PAID.name().equals(status));
        return ResponseEntity.ok(result);
    }

    /**
     * 查询订单详情（含明细）
     */
    @GetMapping("/order/{orderNo}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable String orderNo) {

        OrderResponse response = payService.getOrderDetail(orderNo);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping("/orders/{userId}")
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrders(@PathVariable String userId,
                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer size,
                                                                     @RequestParam(required = false) String status) {
        OrderQueryRequest pageRequest = new OrderQueryRequest();

        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setUserId(userId);
        PageResponse<OrderResponse> result = payService.getUserOrders(pageRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 根据用户ID和状态查询订单（支持状态筛选）
     */
    @GetMapping("/orders/{userId}/{status}")
    public ResponseEntity<PageResponse<OrderResponse>> getUserOrdersByStatus(@PathVariable String userId,
                                                                             @PathVariable String status,
                                                                             @RequestParam(defaultValue = "1") Integer page,
                                                                             @RequestParam(defaultValue = "20") Integer size) {
        OrderQueryRequest pageRequest = new OrderQueryRequest();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setUserId(userId);
        pageRequest.setStatus(status);
        PageResponse<OrderResponse> result = payService.getUserOrdersByStatus(pageRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 取消订单
     */
    @DeleteMapping("/order/{orderNo}")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderNo) {
        boolean result = payService.cancelOrder(orderNo);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", result ? "订单已取消" : "取消失败");
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 确认收货
     */
    @PutMapping("/order/{orderNo}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReceipt(@PathVariable String orderNo) {
        boolean result = payService.confirmReceipt(orderNo);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", result ? "确认收货成功" : "操作失败");
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 获取用户订单统计
     */
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics(@PathVariable String userId) {
        OrderStatisticsResponse response = payService.getOrderStatistics(userId);
        return ResponseEntity.ok(response);
    }


}
