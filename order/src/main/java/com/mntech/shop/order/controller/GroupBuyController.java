package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.GroupBuyCreateRequest;
import com.mntech.shop.order.dto.request.GroupBuyJoinRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.GroupBuyActivityResponse;
import com.mntech.shop.order.dto.response.GroupBuyOrderResponse;
import com.mntech.shop.order.dto.response.GroupBuyProductResponse;
import com.mntech.shop.order.dto.response.GroupBuyTeamResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.service.IGroupBuyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 团购用户端接口
 */
@RestController
@RequestMapping("/api/groupbuy")
@RequiredArgsConstructor
@Validated
public class GroupBuyController {

    private final IGroupBuyService groupBuyService;

    /**
     * 获取进行中的团购活动列表
     */
    @GetMapping("/user/activities")
    public ResponseEntity<List<GroupBuyActivityResponse>> getOngoingActivities() {
        return ResponseEntity.ok(groupBuyService.getOngoingActivities());
    }

    /**
     * 获取团购活动详情
     */
    @GetMapping("/user/activity/{activityId}")
    public ResponseEntity<GroupBuyActivityResponse> getActivityDetail(@PathVariable String activityId) {
        return ResponseEntity.ok(groupBuyService.getActivityDetailForUser(activityId));
    }

    /**
     * 获取团购商品详情（含正在拼团的团；可传当前 userId 判断是否已参与该活动）
     */
    @GetMapping("/user/product/{groupBuyProductId}")
    public ResponseEntity<GroupBuyProductResponse> getProductDetail(@PathVariable String groupBuyProductId,
                                                                   @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(groupBuyService.getProductDetail(groupBuyProductId, userId));
    }

    /**
     * 开团
     */
    @PostMapping("/user/create")
    public ResponseEntity<GroupBuyOrderResponse> createGroup(@Valid @RequestBody GroupBuyCreateRequest request) {
        return ResponseEntity.ok(groupBuyService.createGroup(request));
    }

    /**
     * 参团
     */
    @PostMapping("/user/join")
    public ResponseEntity<GroupBuyOrderResponse> joinGroup(@Valid @RequestBody GroupBuyJoinRequest request) {
        return ResponseEntity.ok(groupBuyService.joinGroup(request));
    }

    /**
     * 模拟支付团购订单
     */
    @PostMapping("/user/order/{orderId}/pay")
    public ResponseEntity<GroupBuyOrderResponse> payOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(groupBuyService.payGroupBuyOrder(orderId));
    }

    /**
     * 取消团购订单
     * 团长取消整团时：已有人跟团（PENDING/PAID 任一）则不允许；无人跟团则取消订单、回滚库存并关闭团
     */
    @PutMapping("/user/order/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                            @RequestParam(required = false) String userId) {
        groupBuyService.cancelGroupBuyOrder(orderId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户团购订单列表（分页）
     */
    @GetMapping("/user/orders/{userId}")
    public ResponseEntity<PageResponse<GroupBuyOrderResponse>> getUserOrders(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status) {
        OrderQueryRequest pageRequest = new OrderQueryRequest();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setUserId(userId);
        pageRequest.setStatus(status);
        return ResponseEntity.ok(groupBuyService.getUserGroupBuyOrders(pageRequest));
    }

    /**
     * 获取团购订单详情
     */
    @GetMapping("/user/order/{orderId}")
    public ResponseEntity<GroupBuyOrderResponse> getOrderDetail(@PathVariable String orderId) {
        return ResponseEntity.ok(groupBuyService.getGroupBuyOrderDetail(orderId));
    }

    /**
     * 获取团详情（含成员列表）
     */
    @GetMapping("/user/team/{teamId}")
    public ResponseEntity<GroupBuyTeamResponse> getTeamDetail(@PathVariable String teamId,
                                                             @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(groupBuyService.getTeamDetail(teamId, userId));
    }
}