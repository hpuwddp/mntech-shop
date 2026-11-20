package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.GroupBuyCreateRequest;
import com.mntech.shop.order.dto.request.GroupBuyJoinRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.GroupBuyActivityResponse;
import com.mntech.shop.order.dto.response.GroupBuyOrderResponse;
import com.mntech.shop.order.dto.response.GroupBuyProductResponse;
import com.mntech.shop.order.dto.response.GroupBuyTeamResponse;
import com.mntech.shop.order.dto.response.PageResponse;

import java.util.List;

public interface IGroupBuyService {

    /**
     * 获取进行中的团购活动列表（用户端）
     */
    List<GroupBuyActivityResponse> getOngoingActivities();

    /**
     * 获取团购活动详情（用户端）
     */
    GroupBuyActivityResponse getActivityDetailForUser(String activityId);

    /**
     * 获取团购商品详情（含正在拼团的团）
     *
     * @param userId 当前用户ID（可选），用于返回该用户是否已参与该活动
     */
    GroupBuyProductResponse getProductDetail(String groupBuyProductId, String userId);

    /**
     * 开团
     */
    GroupBuyOrderResponse createGroup(GroupBuyCreateRequest request);

    /**
     * 参团
     */
    GroupBuyOrderResponse joinGroup(GroupBuyJoinRequest request);

    /**
     * 模拟支付团购订单
     */
    GroupBuyOrderResponse payGroupBuyOrder(String orderId);

    /**
     * 取消团购订单
     *
     * @param orderId 团购订单ID
     * @param userId  当前用户ID（仅本人可操作；团长取消整团时校验是否已有人跟团）
     */
    void cancelGroupBuyOrder(String orderId, String userId);

    /**
     * 查询用户团购订单列表（分页）
     */
    PageResponse<GroupBuyOrderResponse> getUserGroupBuyOrders(OrderQueryRequest pageRequest);

    /**
     * 获取团购订单详情
     */
    GroupBuyOrderResponse getGroupBuyOrderDetail(String orderId);

    /**
     * 获取团详情（含成员列表）
     */
    GroupBuyTeamResponse getTeamDetail(String teamId, String userId);

    /**
     * 处理过期团（拼团失败退款/取消）与支付超时订单（定时任务调用）
     */
    void processExpiredTeamCore();
}