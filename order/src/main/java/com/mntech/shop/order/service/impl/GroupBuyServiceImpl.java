package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.GroupBuyActivityMapper;
import com.mntech.shop.order.dao.GroupBuyOrderMapper;
import com.mntech.shop.order.dao.GroupBuyProductMapper;
import com.mntech.shop.order.dao.GroupBuyTeamMapper;
import com.mntech.shop.order.dao.UserMapper;
import com.mntech.shop.order.dto.request.GroupBuyCreateRequest;
import com.mntech.shop.order.dto.request.GroupBuyJoinRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.GroupBuyActivityResponse;
import com.mntech.shop.order.dto.response.GroupBuyMemberResponse;
import com.mntech.shop.order.dto.response.GroupBuyOrderResponse;
import com.mntech.shop.order.dto.response.GroupBuyProductResponse;
import com.mntech.shop.order.dto.response.GroupBuyTeamBriefResponse;
import com.mntech.shop.order.dto.response.GroupBuyTeamResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.model.GroupBuyActivityModel;
import com.mntech.shop.order.model.GroupBuyOrderModel;
import com.mntech.shop.order.model.GroupBuyProductModel;
import com.mntech.shop.order.model.GroupBuyTeamModel;
import com.mntech.shop.order.model.UserModel;
import com.mntech.shop.order.service.IGroupBuyService;
import com.mntech.shop.order.util.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团购用户端服务
 * 低并发场景：常规事务 + 乐观锁（version 字段）控制库存，不依赖消息队列。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupBuyServiceImpl implements IGroupBuyService {

    private final GroupBuyActivityMapper groupBuyActivityMapper;
    private final GroupBuyProductMapper groupBuyProductMapper;
    private final GroupBuyTeamMapper groupBuyTeamMapper;
    private final GroupBuyOrderMapper groupBuyOrderMapper;
    private final UserMapper userMapper;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    /** 待支付订单超时时间（分钟） */
    private static final int PAY_TIMEOUT_MINUTES = 15;

    // ==================== 活动 ====================

    @Override
    public List<GroupBuyActivityResponse> getOngoingActivities() {
        LambdaQueryWrapper<GroupBuyActivityModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupBuyActivityModel::getStatus, "APPROVED", "ONGOING")
                .eq(GroupBuyActivityModel::getDeleted, 0)
                .le(GroupBuyActivityModel::getStartTime, LocalDateTime.now())
                .ge(GroupBuyActivityModel::getEndTime, LocalDateTime.now())
                .orderByAsc(GroupBuyActivityModel::getCreatedAt);

        List<GroupBuyActivityModel> activities = groupBuyActivityMapper.selectList(wrapper);
        if (CollectionUtil.isEmpty(activities)) {
            return new ArrayList<>();
        }
        return activities.stream()
                .map(this::convertToActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GroupBuyActivityResponse getActivityDetailForUser(String activityId) {
        GroupBuyActivityModel activity = groupBuyActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return convertToActivityResponse(activity);
    }

    @Override
    public GroupBuyProductResponse getProductDetail(String groupBuyProductId, String userId) {
        GroupBuyProductModel product = groupBuyProductMapper.selectByIdWithProduct(groupBuyProductId);
        if (product == null) {
            throw new BusinessException("团购商品不存在");
        }
        GroupBuyProductResponse response = convertToProductResponse(product);

        // 当前用户是否已参与该活动（进行中订单 PENDING/PAID 且团 ONGOING）
        if (userId != null && !userId.trim().isEmpty()) {
            int active = groupBuyOrderMapper.countActiveByUserAndActivity(userId.trim(), product.getActivityId());
            response.setParticipated(active > 0);
        } else {
            response.setParticipated(Boolean.FALSE);
        }

        // 正在拼团的团
        List<GroupBuyTeamModel> teams = groupBuyTeamMapper.selectOngoingByProductId(groupBuyProductId);
        List<GroupBuyTeamBriefResponse> teamResponses = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(teams)) {
            for (GroupBuyTeamModel team : teams) {
                teamResponses.add(convertToTeamBriefResponse(team, product));
            }
        }
        response.setOngoingTeams(teamResponses);
        return response;
    }

    // ==================== 开团 / 参团 ====================

    @Override
    @Transactional
    public GroupBuyOrderResponse createGroup(GroupBuyCreateRequest request) {
        String userId = request.getUserId();
        String groupBuyProductId = request.getGroupBuyProductId();
        Integer quantity = request.getQuantity();

        // 1. 校验商品与活动
        GroupBuyProductModel product = groupBuyProductMapper.selectByIdWithProduct(groupBuyProductId);
        if (product == null) {
            throw new BusinessException("团购商品不存在");
        }
        GroupBuyActivityModel activity = groupBuyActivityMapper.selectById(product.getActivityId());
        validateActivity(activity);

        // 2. 校验购买数量与限购
        int limit = product.getPerUserLimit() != null ? product.getPerUserLimit() : 1;
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("购买数量必须大于0");
        }
        if (quantity > limit) {
            throw new BusinessException("购买数量超出限购（每人限购" + limit + "件）");
        }
        Integer bought = groupBuyOrderMapper.countUserBuy(userId, groupBuyProductId);
        if (bought != null && bought + quantity > limit) {
            throw new BusinessException("您已达到限购数量");
        }
        // 活动级防重复：同一用户同一活动只允许一个进行中订单（PENDING/PAID 且团 ONGOING），
        // 兼容同活动多商品与 per_user_limit>1 场景，避免首单未支付仍可再次开团/参团
        if (groupBuyOrderMapper.countActiveByUserAndActivity(userId, activity.getId()) > 0) {
            throw new BusinessException("您在该活动中已有进行中的团购订单，请勿重复开团");
        }

        // 3. 校验库存（可售 = remaining - locked）
        int available = product.getAvailableStock() != null ? product.getAvailableStock() : 0;
        if (quantity > available) {
            throw new BusinessException("库存不足");
        }

        // 4. 扣减库存（乐观锁）
        int affected = groupBuyProductMapper.decreaseStock(groupBuyProductId, quantity, product.getVersion());
        if (affected == 0) {
            throw new BusinessException("库存扣减失败，请重试");
        }

        // 5. 创建团
        LocalDateTime now = LocalDateTime.now();
        int groupHours = product.getGroupHours() != null ? product.getGroupHours() : 24;
        GroupBuyTeamModel team = new GroupBuyTeamModel();
        team.setId(snowflakeIdWorker.generateId());
        team.setGroupNo("GB" + snowflakeIdWorker.nextId());
        team.setActivityId(product.getActivityId());
        team.setGroupBuyProductId(groupBuyProductId);
        team.setLeaderUserId(userId);
        team.setCurrentNum(0);
        team.setTargetNum(product.getMinPeople() != null ? product.getMinPeople() : 2);
        team.setStatus("ONGOING");
        team.setEndTime(now.plusHours(groupHours));
        team.setDeleted(0);
        groupBuyTeamMapper.insert(team);

        // 6. 创建待支付订单（开团 = 团长，享受 9 折）
        GroupBuyOrderModel order = createOrder(userId, team, product, quantity,
                request.getReceiverName(), request.getReceiverPhone(), request.getReceiverAddress(), true);

        log.info("开团成功: userId={}, teamId={}, orderNo={}", userId, team.getId(), order.getOrderNo());
        return convertToOrderResponse(order);
    }

    @Override
    @Transactional
    public GroupBuyOrderResponse joinGroup(GroupBuyJoinRequest request) {
        String userId = request.getUserId();
        String teamId = request.getTeamId();
        String groupBuyProductId = request.getGroupBuyProductId();
        Integer quantity = request.getQuantity();

        // 1. 校验团
        GroupBuyTeamModel team = groupBuyTeamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团不存在");
        }
        if (!"ONGOING".equals(team.getStatus())) {
            throw new BusinessException("该团已结束，无法参团");
        }
        if (!team.getGroupBuyProductId().equals(groupBuyProductId)) {
            throw new BusinessException("参团商品不匹配");
        }
        if (team.getEndTime() != null && team.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该团已过期");
        }
        // 满员校验
        if (team.getCurrentNum() != null && team.getTargetNum() != null && team.getCurrentNum() >= team.getTargetNum()) {
            throw new BusinessException("该团已满员，请开新团");
        }
        // 防止重复参团
        if (groupBuyOrderMapper.countUserBuyInTeam(userId, teamId) > 0) {
            throw new BusinessException("您已参与该团，无需重复参团");
        }

        // 2. 校验商品与活动
        GroupBuyProductModel product = groupBuyProductMapper.selectByIdWithProduct(groupBuyProductId);
        if (product == null) {
            throw new BusinessException("团购商品不存在");
        }
        GroupBuyActivityModel activity = groupBuyActivityMapper.selectById(team.getActivityId());
        validateActivity(activity);

        // 3. 校验数量与限购
        int limit = product.getPerUserLimit() != null ? product.getPerUserLimit() : 1;
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("购买数量必须大于0");
        }
        if (quantity > limit) {
            throw new BusinessException("购买数量超出限购（每人限购" + limit + "件）");
        }
        Integer bought = groupBuyOrderMapper.countUserBuy(userId, groupBuyProductId);
        if (bought != null && bought + quantity > limit) {
            throw new BusinessException("您已达到限购数量");
        }
        // 活动级防重复：同一用户同一活动只允许一个进行中订单（PENDING/PAID 且团 ONGOING）
        if (groupBuyOrderMapper.countActiveByUserAndActivity(userId, activity.getId()) > 0) {
            throw new BusinessException("您在该活动中已有进行中的团购订单，请勿重复参团");
        }

        // 4. 校验并扣减库存
        int available = product.getAvailableStock() != null ? product.getAvailableStock() : 0;
        if (quantity > available) {
            throw new BusinessException("库存不足");
        }
        int affected = groupBuyProductMapper.decreaseStock(groupBuyProductId, quantity, product.getVersion());
        if (affected == 0) {
            throw new BusinessException("库存扣减失败，请重试");
        }

        // 5. 创建待支付订单（参团 = 普通成员，不打折）
        GroupBuyOrderModel order = createOrder(userId, team, product, quantity,
                request.getReceiverName(), request.getReceiverPhone(), request.getReceiverAddress(), false);

        log.info("参团成功: userId={}, teamId={}, orderNo={}", userId, teamId, order.getOrderNo());
        return convertToOrderResponse(order);
    }

    // ==================== 支付 / 取消 ====================

    @Override
    @Transactional
    public GroupBuyOrderResponse payGroupBuyOrder(String orderId) {
        GroupBuyOrderModel order = groupBuyOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能支付");
        }

        // 支付超时检查
        LocalDateTime now = LocalDateTime.now();
        if (order.getCreatedAt() != null && order.getCreatedAt().plusMinutes(PAY_TIMEOUT_MINUTES).isBefore(now)) {
            cancelOrderInternal(order);
            throw new BusinessException("订单已超时，请重新下单");
        }

        // 团队有效性检查
        GroupBuyTeamModel team = groupBuyTeamMapper.selectById(order.getTeamId());
        if (team == null || !"ONGOING".equals(team.getStatus())) {
            throw new BusinessException("拼团已结束，无法支付");
        }

        // 模拟支付成功
        String transactionId = "MOCK" + snowflakeIdWorker.nextId();
        int updated = groupBuyOrderMapper.updatePaySuccess(orderId, transactionId, now);
        if (updated == 0) {
            throw new BusinessException("支付失败，请重试");
        }

        // 确认扣减库存（锁定转已售），乐观锁失败重试一次
        confirmStockDeduct(order.getGroupBuyProductId(), order.getQuantity());

        // 团队人数 +1
        groupBuyTeamMapper.incrementCurrentNum(team.getId());

        // 满员则成团
        GroupBuyTeamModel latestTeam = groupBuyTeamMapper.selectById(team.getId());
        if (latestTeam != null && latestTeam.getTargetNum() != null
                && latestTeam.getCurrentNum() != null && latestTeam.getCurrentNum() >= latestTeam.getTargetNum()) {
            groupBuyTeamMapper.updateStatus(team.getId(), "SUCCESS");
            log.info("拼团成功: teamId={}", team.getId());
        }

        log.info("团购订单支付成功: orderId={}, transactionId={}", orderId, transactionId);
        GroupBuyOrderModel paidOrder = groupBuyOrderMapper.selectById(orderId);
        return paidOrder != null ? convertToOrderResponse(paidOrder) : convertToOrderResponse(order);
    }

    @Override
    @Transactional
    public void cancelGroupBuyOrder(String orderId, String userId) {
        GroupBuyOrderModel order = groupBuyOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException("请先登录后再操作");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("只能取消自己的订单");
        }

        GroupBuyTeamModel team = order.getTeamId() == null ? null : groupBuyTeamMapper.selectById(order.getTeamId());

        // 团长取消整团：已有人跟团（PENDING/PAID 任一）则不允许；
        // 无人跟团则取消订单（已支付需退款）、回滚库存并关闭团，避免遗留 ONGOING 孤儿团
        if (team != null && userId.equals(team.getLeaderUserId())) {
            if (!"ONGOING".equals(team.getStatus())) {
                throw new BusinessException("拼团已结束，无法取消");
            }
            int otherActive = groupBuyOrderMapper.countOtherActiveByTeam(order.getTeamId(), userId);
            if (otherActive > 0) {
                throw new BusinessException("该团已有人参团，无法取消");
            }
            cancelOrderStrict(order);
            groupBuyTeamMapper.updateStatus(order.getTeamId(), "CLOSED");
            log.info("团长取消整团成功: orderId={}, teamId={}, orderStatus={}", orderId, order.getTeamId(), order.getStatus());
            return;
        }

        // 非团长（普通成员）：仅允许取消自己的待支付订单，回补库存，不影响团状态
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能取消");
        }
        cancelOrderInternal(order);
        log.info("取消团购订单成功: orderId={}", orderId);
    }

    // ==================== 订单查询 ====================

    @Override
    public PageResponse<GroupBuyOrderResponse> getUserGroupBuyOrders(OrderQueryRequest pageRequest) {
        int page = pageRequest.getPage() == null || pageRequest.getPage() < 1 ? 1 : pageRequest.getPage();
        int size = pageRequest.getSize() == null || pageRequest.getSize() < 1 ? 20 : pageRequest.getSize();
        size = Math.min(size, 100);

        String status = pageRequest.getStatus();
        List<GroupBuyOrderModel> orders;
        PageHelper.startPage(page, size);
        if (status == null || "all".equalsIgnoreCase(status)) {
            orders = groupBuyOrderMapper.selectByUserId(pageRequest.getUserId());
        } else {
            orders = groupBuyOrderMapper.selectByUserIdAndStatus(pageRequest.getUserId(), status);
        }

        List<GroupBuyOrderResponse> result = new ArrayList<>();
        for (GroupBuyOrderModel order : orders) {
            result.add(convertToOrderResponse(order));
        }

        PageInfo<GroupBuyOrderModel> pageInfo = new PageInfo<>(orders);
        PageResponse<GroupBuyOrderResponse> response = new PageResponse<>();
        response.setRecords(result);
        response.setTotal(pageInfo.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages(pageInfo.getPages());
        response.setHasNext(page < pageInfo.getPages());
        response.setHasPrev(page > 1);
        return response;
    }

    @Override
    public GroupBuyOrderResponse getGroupBuyOrderDetail(String orderId) {
        GroupBuyOrderModel order = groupBuyOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 待支付且超时则自动取消
        if ("PENDING".equals(order.getStatus()) && order.getCreatedAt() != null
                && order.getCreatedAt().plusMinutes(PAY_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
            cancelOrderInternal(order);
            order.setStatus("CANCELLED");
            order.setStatusDesc("已超时取消");
        }
        return convertToOrderResponse(order);
    }

    @Override
    public GroupBuyTeamResponse getTeamDetail(String teamId, String userId) {
        GroupBuyTeamModel team = groupBuyTeamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("团不存在");
        }

        GroupBuyTeamResponse response = new GroupBuyTeamResponse();
        response.setId(team.getId());
        response.setGroupNo(team.getGroupNo());
        response.setActivityId(team.getActivityId());
        response.setGroupBuyProductId(team.getGroupBuyProductId());
        response.setTargetNum(team.getTargetNum());
        response.setCurrentNum(team.getCurrentNum());
        response.setStatus(team.getStatus());
        response.setStatusDesc(teamStatusDesc(team.getStatus()));
        response.setEndTime(team.getEndTime());
        response.setCreatedAt(team.getCreatedAt());
        response.setRemainingSeconds(calcTeamRemainingSeconds(team));

        // 商品信息
        GroupBuyProductModel product = groupBuyProductMapper.selectByIdWithProduct(team.getGroupBuyProductId());
        if (product != null) {
            response.setProductName(product.getProductName());
            response.setProductImage(product.getImageUrl());
            response.setGroupPrice(product.getGroupPrice());
        }

        // 团长信息
        UserModel leader = userMapper.selectById(team.getLeaderUserId());
        if (leader != null) {
            response.setLeaderUserId(leader.getId());
            response.setLeaderNickname(leader.getNickname());
            response.setLeaderAvatar(leader.getAvatar());
        }

        // 成员列表
        List<GroupBuyOrderModel> orders = groupBuyOrderMapper.selectByTeamId(teamId);
        List<GroupBuyMemberResponse> members = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(orders)) {
            for (GroupBuyOrderModel order : orders) {
                GroupBuyMemberResponse member = new GroupBuyMemberResponse();
                member.setUserId(order.getUserId());
                member.setNickname(order.getBuyerNickname());
                member.setAvatar(order.getBuyerAvatar());
                member.setIsLeader(team.getLeaderUserId() != null && team.getLeaderUserId().equals(order.getUserId()));
                member.setOrderId(order.getId());
                member.setQuantity(order.getQuantity());
                member.setOrderStatus(order.getStatus());
                member.setOrderStatusDesc(order.getStatusDesc());
                member.setCreatedAt(order.getCreatedAt());
                members.add(member);
            }
        }
        response.setMembers(members);

        // 当前用户是否已参与
        if (userId != null && !userId.isEmpty()) {
            response.setIsJoined(groupBuyOrderMapper.countUserBuyInTeam(userId, teamId) > 0);
        } else {
            response.setIsJoined(false);
        }
        return response;
    }

    // ==================== 定时任务：过期处理 ====================

    @Override
    @Transactional
    public void processExpiredTeamCore() {
        // 1. 过期未成团的团 → 拼团失败：已支付订单退款、待支付订单取消、库存回补
        List<GroupBuyTeamModel> expiredTeams = groupBuyTeamMapper.selectExpiredTeams();
        if (CollectionUtil.isNotEmpty(expiredTeams)) {
            for (GroupBuyTeamModel team : expiredTeams) {
                try {
                    processFailedTeam(team.getId());
                } catch (Exception e) {
                    log.error("处理失败团异常: teamId={}", team.getId(), e);
                }
            }
        }

        // 2. 支付超时的待支付订单 → 取消并回补库存
        List<GroupBuyOrderModel> timeoutOrders = groupBuyOrderMapper.selectPendingTimeoutOrders();
        if (CollectionUtil.isNotEmpty(timeoutOrders)) {
            for (GroupBuyOrderModel order : timeoutOrders) {
                try {
                    cancelOrderInternal(order);
                } catch (Exception e) {
                    log.error("取消超时团购订单异常: orderId={}", order.getId(), e);
                }
            }
        }
    }

    /**
     * 处理拼团失败的团：先置 CLOSED 防并发，再退款/取消订单与回补库存，最后置 FAILED
     */
    private void processFailedTeam(String teamId) {
        GroupBuyTeamModel team = groupBuyTeamMapper.selectById(teamId);
        if (team == null || !"ONGOING".equals(team.getStatus())) {
            return;
        }
        groupBuyTeamMapper.updateStatus(teamId, "CLOSED");

        List<GroupBuyOrderModel> orders = groupBuyOrderMapper.selectByTeamId(teamId);
        if (CollectionUtil.isNotEmpty(orders)) {
            for (GroupBuyOrderModel order : orders) {
                if ("PAID".equals(order.getStatus())) {
                    groupBuyOrderMapper.updateRefunded(order.getId());
                    refundStock(order.getGroupBuyProductId(), order.getQuantity());
                } else if ("PENDING".equals(order.getStatus())) {
                    cancelOrderInternal(order);
                }
            }
        }
        groupBuyTeamMapper.updateStatus(teamId, "FAILED");
        log.info("拼团失败处理完成: teamId={}", teamId);
    }

    // ==================== 私有方法 ====================

    private void validateActivity(GroupBuyActivityModel activity) {
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        if (!"APPROVED".equals(activity.getStatus()) && !"ONGOING".equals(activity.getStatus())) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动尚未开始");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }
    }

    private GroupBuyOrderModel createOrder(String userId, GroupBuyTeamModel team, GroupBuyProductModel product,
                                           Integer quantity, String receiverName, String receiverPhone, String receiverAddress,
                                           boolean isLeader) {
        GroupBuyOrderModel order = new GroupBuyOrderModel();
        order.setId(snowflakeIdWorker.generateId());
        order.setOrderNo("GBO" + snowflakeIdWorker.nextId());
        order.setTeamId(team.getId());
        order.setActivityId(team.getActivityId());
        order.setGroupBuyProductId(team.getGroupBuyProductId());
        order.setUserId(userId);
        order.setQuantity(quantity);
        order.setGroupPrice(product.getGroupPrice());
        BigDecimal totalAmount = product.getGroupPrice().multiply(BigDecimal.valueOf(quantity));
        // 团长 9 折：优惠金额 = 原合计 * 0.1，实付 = 原合计 - 优惠；参团不打折
        BigDecimal discountAmount = isLeader
                ? totalAmount.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP));
        order.setStatus("PENDING");
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setDeleted(0);
        groupBuyOrderMapper.insert(order);
        return order;
    }

    /**
     * 取消待支付订单并回补库存
     */
    private void cancelOrderInternal(GroupBuyOrderModel order) {
        int updated = groupBuyOrderMapper.markCancelled(order.getId());
        if (updated == 0) {
            return;
        }
        releaseStock(order.getGroupBuyProductId(), order.getQuantity());
    }

    /**
     * 按状态取消订单（团长整团取消时使用）：
     * PENDING → 置 CANCELLED 并回补锁定库存；PAID → 置 REFUNDED 记录退款并回补已售库存
     */
    private void cancelOrderStrict(GroupBuyOrderModel order) {
        if ("PENDING".equals(order.getStatus())) {
            int updated = groupBuyOrderMapper.markCancelled(order.getId());
            if (updated == 0) {
                throw new BusinessException("当前订单状态不能取消");
            }
            releaseStock(order.getGroupBuyProductId(), order.getQuantity());
        } else if ("PAID".equals(order.getStatus())) {
            int updated = groupBuyOrderMapper.updateRefunded(order.getId());
            if (updated == 0) {
                throw new BusinessException("当前订单状态不能取消");
            }
            refundStock(order.getGroupBuyProductId(), order.getQuantity());
        } else {
            throw new BusinessException("当前订单状态不能取消");
        }
    }

    /**
     * 待支付订单取消：可售加、锁定减
     */
    private void releaseStock(String groupBuyProductId, Integer quantity) {
        GroupBuyProductModel product = groupBuyProductMapper.selectById(groupBuyProductId);
        if (product == null) {
            return;
        }
        int affected = groupBuyProductMapper.increaseStock(groupBuyProductId, quantity, product.getVersion());
        if (affected == 0) {
            GroupBuyProductModel latest = groupBuyProductMapper.selectById(groupBuyProductId);
            if (latest != null) {
                groupBuyProductMapper.increaseStock(groupBuyProductId, quantity, latest.getVersion());
            }
        }
    }

    /**
     * 已支付订单确认扣减：锁定转已售
     */
    private void confirmStockDeduct(String groupBuyProductId, Integer quantity) {
        GroupBuyProductModel product = groupBuyProductMapper.selectById(groupBuyProductId);
        if (product == null) {
            return;
        }
        int affected = groupBuyProductMapper.confirmPay(groupBuyProductId, quantity, product.getVersion());
        if (affected == 0) {
            GroupBuyProductModel latest = groupBuyProductMapper.selectById(groupBuyProductId);
            if (latest != null) {
                groupBuyProductMapper.confirmPay(groupBuyProductId, quantity, latest.getVersion());
            }
        }
    }

    /**
     * 已支付订单退款：可售加、已售减
     */
    private void refundStock(String groupBuyProductId, Integer quantity) {
        GroupBuyProductModel product = groupBuyProductMapper.selectById(groupBuyProductId);
        if (product == null) {
            return;
        }
        int affected = groupBuyProductMapper.refundStock(groupBuyProductId, quantity, product.getVersion());
        if (affected == 0) {
            GroupBuyProductModel latest = groupBuyProductMapper.selectById(groupBuyProductId);
            if (latest != null) {
                groupBuyProductMapper.refundStock(groupBuyProductId, quantity, latest.getVersion());
            }
        }
    }

    private GroupBuyActivityResponse convertToActivityResponse(GroupBuyActivityModel activity) {
        if (activity == null) {
            return null;
        }
        List<GroupBuyProductModel> products = groupBuyProductMapper.selectByActivityIdWithProduct(activity.getId());

        GroupBuyActivityResponse response = new GroupBuyActivityResponse();
        BeanUtil.copyProperties(activity, response);

        int totalStock = 0;
        int totalRemaining = 0;
        List<GroupBuyProductResponse> productResponses = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(products)) {
            for (GroupBuyProductModel product : products) {
                totalStock += product.getStock() != null ? product.getStock() : 0;
                totalRemaining += product.getRemainingStock() != null ? product.getRemainingStock() : 0;
                productResponses.add(convertToProductResponse(product));
            }
        }

        response.setProductCount(products.size());
        response.setTotalStock(totalStock);
        response.setTotalRemainingStock(totalRemaining);
        response.setProducts(productResponses);
        response.setIsOngoing("ONGOING".equals(activity.getStatus()));
        response.setIsEnded("ENDED".equals(activity.getStatus()));

        if (activity.getEndTime() != null) {
            long remaining = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
            response.setRemainingTime(remaining > 0 ? remaining : 0);
        } else {
            response.setRemainingTime(0L);
        }
        return response;
    }

    private GroupBuyProductResponse convertToProductResponse(GroupBuyProductModel product) {
        GroupBuyProductResponse response = new GroupBuyProductResponse();
        BeanUtil.copyProperties(product, response);
        response.setIsSoldOut(product.getIsSoldOut());
        response.setGroupPriceDisplay(product.getGroupPriceDisplay());
        response.setDiscountDisplay(product.getDiscountDisplay());
        response.setAvailableStock(product.getAvailableStock());
        return response;
    }

    private GroupBuyTeamBriefResponse convertToTeamBriefResponse(GroupBuyTeamModel team, GroupBuyProductModel product) {
        GroupBuyTeamBriefResponse response = new GroupBuyTeamBriefResponse();
        response.setId(team.getId());
        response.setGroupNo(team.getGroupNo());
        response.setActivityId(team.getActivityId());
        response.setGroupBuyProductId(team.getGroupBuyProductId());
        response.setLeaderUserId(team.getLeaderUserId());
        response.setTargetNum(team.getTargetNum());
        response.setCurrentNum(team.getCurrentNum());
        response.setStatus(team.getStatus());
        response.setStatusDesc(teamStatusDesc(team.getStatus()));
        response.setEndTime(team.getEndTime());
        response.setCreatedAt(team.getCreatedAt());
        response.setRemainingSeconds(calcTeamRemainingSeconds(team));

        if (product != null) {
            response.setProductName(product.getProductName());
            response.setProductImage(product.getImageUrl());
            response.setGroupPrice(product.getGroupPrice());
        }

        UserModel leader = userMapper.selectById(team.getLeaderUserId());
        if (leader != null) {
            response.setLeaderNickname(leader.getNickname());
            response.setLeaderAvatar(leader.getAvatar());
        }
        return response;
    }

    private GroupBuyOrderResponse convertToOrderResponse(GroupBuyOrderModel order) {
        GroupBuyOrderResponse response = new GroupBuyOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setTeamId(order.getTeamId());
        response.setActivityId(order.getActivityId());
        response.setGroupBuyProductId(order.getGroupBuyProductId());
        response.setGroupPrice(order.getGroupPrice());
        response.setQuantity(order.getQuantity());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setPayAmount(order.getPayAmount());
        response.setRefundAmount(order.getRefundAmount());
        response.setRefundTime(order.getRefundTime());
        response.setStatus(order.getStatus());
        response.setStatusDesc(order.getStatusDesc());
        response.setCreatedAt(order.getCreatedAt());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());
        response.setReceiverAddress(order.getReceiverAddress());
        response.setProductName(order.getProductName());
        response.setProductImage(order.getProductImage());

        // 商品名称为空时补查
        if (order.getProductName() == null || order.getProductName().isEmpty()) {
            GroupBuyProductModel product = groupBuyProductMapper.selectByIdWithProduct(order.getGroupBuyProductId());
            if (product != null) {
                response.setProductName(product.getProductName());
                response.setProductImage(product.getImageUrl());
            }
        }

        // 剩余支付时间（待支付）
        if ("PENDING".equals(order.getStatus())) {
            if (order.getCreatedAt() != null) {
                long remaining = Duration.between(LocalDateTime.now(), order.getCreatedAt().plusMinutes(PAY_TIMEOUT_MINUTES)).getSeconds();
                response.setRemainingPayTime(remaining > 0 ? remaining : 0);
            } else {
                response.setRemainingPayTime(0L);
            }
        } else {
            response.setRemainingPayTime(0L);
        }

        // 所属团信息
        if (order.getTeamId() != null) {
            GroupBuyTeamModel team = groupBuyTeamMapper.selectById(order.getTeamId());
            if (team != null) {
                response.setGroupNo(team.getGroupNo());
                response.setTeamCurrentNum(team.getCurrentNum());
                response.setTeamTargetNum(team.getTargetNum());
                response.setTeamStatus(team.getStatus());
                response.setTeamStatusDesc(teamStatusDesc(team.getStatus()));
                response.setIsLeader(order.getUserId() != null && order.getUserId().equals(team.getLeaderUserId()));
            }
        }
        return response;
    }

    private Long calcTeamRemainingSeconds(GroupBuyTeamModel team) {
        if (team == null || team.getEndTime() == null) {
            return 0L;
        }
        if (!"ONGOING".equals(team.getStatus())) {
            return 0L;
        }
        long remaining = Duration.between(LocalDateTime.now(), team.getEndTime()).getSeconds();
        return remaining > 0 ? remaining : 0;
    }

    private String teamStatusDesc(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "ONGOING":
                return "拼团中";
            case "SUCCESS":
                return "已成团";
            case "FAILED":
                return "拼团失败";
            case "CLOSED":
                return "已关闭";
            default:
                return status;
        }
    }
}