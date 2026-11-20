package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.request.SeckillBuyRequest;
import com.mntech.shop.order.dto.response.OrderResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.dto.response.SeckillActivityResponse;
import com.mntech.shop.order.dto.response.SeckillOrderResponse;
import com.mntech.shop.order.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀用户端接口
 */
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Validated
public class SeckillController {

    private final ISeckillService seckillService;

    /**
     * 获取进行中的秒杀活动列表
     */
    @GetMapping("/user/activities")
    public ResponseEntity<List<SeckillActivityResponse>> getOngoingActivities() {
        List<SeckillActivityResponse> activities = seckillService.getOngoingActivities();
        return ResponseEntity.ok(activities);
    }

    /**
     * 获取秒杀活动详情
     */
    @GetMapping("/user/activity/{activityId}")
    public ResponseEntity<SeckillActivityResponse> getActivityDetail(@PathVariable String activityId) {
        SeckillActivityResponse activity = seckillService.getActivityDetailForUser(activityId);
        return ResponseEntity.ok(activity);
    }

    /**
     * 秒杀下单
     */
    @PostMapping("/user/buy")
    public ResponseEntity<SeckillOrderResponse> seckillBuy(@Valid @RequestBody SeckillBuyRequest request) {
        SeckillOrderResponse response = seckillService.seckillBuy(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取秒杀商品剩余库存
     */
    @GetMapping("/user/stock/{seckillProductId}")
    public ResponseEntity<Map<String, Object>> getRemainingStock(@PathVariable String seckillProductId) {
        Integer stock = seckillService.getRemainingStock(seckillProductId);
        Map<String, Object> result = new HashMap<>();
        result.put("seckillProductId", seckillProductId);
        result.put("remainingStock", stock != null ? stock : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户秒杀订单列表
     */
    @GetMapping("/user/orders/{userId}")
    public ResponseEntity<PageResponse<SeckillOrderResponse>> getUserSeckillOrders(@PathVariable String userId, @RequestParam(defaultValue = "1") Integer page,
                                                                                   @RequestParam(defaultValue = "20") Integer size,
                                                                                   @RequestParam(required = false) String status) {


        OrderQueryRequest pageRequest = new OrderQueryRequest();

        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setUserId(userId);
        pageRequest.setStatus(status);

        PageResponse<SeckillOrderResponse> result = seckillService.getUserSeckillOrders(pageRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消秒杀订单
     */
    @PutMapping("/user/order/{orderId}/cancel")
    public ResponseEntity<Void> cancelSeckillOrder(@PathVariable String orderId) {
        seckillService.cancelSeckillOrder(orderId);
        return ResponseEntity.ok().build();
    }


    /**
     * 支付秒杀订单
     */
    @PostMapping("/user/order/{orderId}/pay")
    public ResponseEntity<SeckillOrderResponse> paySeckillOrder(@PathVariable String orderId) {
        SeckillOrderResponse response = seckillService.paySeckillOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取秒杀订单详情
     */
    @GetMapping("/user/order/{orderId}")
    public ResponseEntity<SeckillOrderResponse> getSeckillOrderDetail(@PathVariable String orderId) {
        SeckillOrderResponse response = seckillService.getSeckillOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }
}
