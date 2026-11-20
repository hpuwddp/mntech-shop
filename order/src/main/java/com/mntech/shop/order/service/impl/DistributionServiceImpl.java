package com.mntech.shop.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mntech.shop.common.enums.OrderStatus;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.config.WechatUtil;
import com.mntech.shop.order.dao.CommissionRecordMapper;
import com.mntech.shop.order.dao.DistributionConfigMapper;
import com.mntech.shop.order.dao.DistributionRelationMapper;
import com.mntech.shop.order.dao.DistributionWithdrawalMapper;
import com.mntech.shop.order.dao.OrderMapper;
import com.mntech.shop.order.dao.UserMapper;
import com.mntech.shop.order.dto.request.BindRelationRequest;
import com.mntech.shop.order.dto.request.WithdrawalApplyRequest;
import com.mntech.shop.order.dto.response.CommissionRecordResponse;
import com.mntech.shop.order.dto.response.DistributionCenterResponse;
import com.mntech.shop.order.dto.response.DistributionTeamResponse;
import com.mntech.shop.order.dto.response.DistributionWithdrawalResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.model.CommissionRecordModel;
import com.mntech.shop.order.model.DistributionConfigModel;
import com.mntech.shop.order.model.DistributionRelationModel;
import com.mntech.shop.order.model.DistributionWithdrawalModel;
import com.mntech.shop.order.model.UserModel;
import com.mntech.shop.order.service.IDistributionService;
import com.mntech.shop.order.service.IShopConfigService;
import com.mntech.shop.order.util.PosterGenerator;
import com.mntech.shop.pojo.model.OrderModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionServiceImpl implements IDistributionService {

    /** 最低提现金额 */
    private static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("10.00");

    /** 默认佣金层级数 */
    private static final int MAX_LEVEL = 3;

    private final DistributionRelationMapper relationMapper;
    private final CommissionRecordMapper commissionRecordMapper;
    private final DistributionWithdrawalMapper withdrawalMapper;
    private final DistributionConfigMapper configMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final IShopConfigService shopConfigService;
    private final WechatUtil wechatUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> bindRelation(BindRelationRequest request) {
        String userId = request.getUserId();
        String parentId = request.getParentId();
        if (StrUtil.isBlank(userId) || StrUtil.isBlank(parentId)) {
            throw new BusinessException("参数不完整");
        }
        if (userId.equals(parentId)) {
            throw new BusinessException("不能绑定自己为上级");
        }
        UserModel parent = userMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("推荐人不存在");
        }
        // 防循环：检查父级链上是否已包含当前用户（最多查 10 层防止脏数据死循环）
        String cursor = parentId;
        int guard = 0;
        while (StrUtil.isNotBlank(cursor) && guard++ < 10) {
            DistributionRelationModel rel = relationMapper.selectByUserId(cursor);
            if (rel == null || StrUtil.isBlank(rel.getParentId())) {
                break;
            }
            if (userId.equals(rel.getParentId())) {
                throw new BusinessException("不能绑定自己的下级为上级");
            }
            cursor = rel.getParentId();
        }

        DistributionRelationModel exist = relationMapper.selectByUserId(userId);
        if (exist != null) {
            exist.setParentId(parentId);
            exist.setUpdatedAt(LocalDateTime.now());
            relationMapper.updateById(exist);
        } else {
            DistributionRelationModel relation = new DistributionRelationModel();
            relation.setUserId(userId);
            relation.setParentId(parentId);
            relationMapper.insert(relation);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", exist != null ? "更换上级成功" : "绑定成功");
        return result;
    }

    @Override
    public Map<String, Object> getParent(String userId) {
        Map<String, Object> result = new HashMap<>();
        DistributionRelationModel rel = relationMapper.selectByUserId(userId);
        if (rel == null) {
            result.put("hasParent", false);
            result.put("parentUserId", null);
            result.put("parentNickname", null);
            return result;
        }
        result.put("hasParent", true);
        result.put("parentUserId", rel.getParentId());
        UserModel parent = userMapper.selectById(rel.getParentId());
        result.put("parentNickname", parent != null ? parent.getNickname() : null);
        result.put("parentAvatar", parent != null ? parent.getAvatar() : null);
        return result;
    }

    @Override
    public DistributionCenterResponse getCenter(String userId) {
        BigDecimal available = commissionRecordMapper.sumAmountByUserAndStatus(userId, "AVAILABLE");
        BigDecimal locked = commissionRecordMapper.sumAmountByUserAndStatus(userId, "LOCKED");
        BigDecimal withdrawn = commissionRecordMapper.sumAmountByUserAndStatus(userId, "WITHDRAWN");
        BigDecimal total = available.add(locked).add(withdrawn);

        // 上级信息
        String parentUserId = null;
        String parentNickname = null;
        DistributionRelationModel myRel = relationMapper.selectByUserId(userId);
        if (myRel != null && StrUtil.isNotBlank(myRel.getParentId())) {
            parentUserId = myRel.getParentId();
            UserModel parent = userMapper.selectById(parentUserId);
            parentNickname = parent != null ? parent.getNickname() : null;
        }

        // 三级团队人数
        List<DistributionRelationModel> level1Rels = relationMapper.selectList(
                new LambdaQueryWrapper<DistributionRelationModel>()
                        .eq(DistributionRelationModel::getParentId, userId));
        long level1Count = level1Rels.size();
        long level2Count = 0;
        long level3Count = 0;
        if (level1Count > 0) {
            List<String> level1Ids = level1Rels.stream()
                    .map(DistributionRelationModel::getUserId)
                    .collect(Collectors.toList());
            List<DistributionRelationModel> level2Rels = relationMapper.selectList(
                    new LambdaQueryWrapper<DistributionRelationModel>()
                            .in(DistributionRelationModel::getParentId, level1Ids));
            level2Count = level2Rels.size();
            if (level2Count > 0) {
                List<String> level2Ids = level2Rels.stream()
                        .map(DistributionRelationModel::getUserId)
                        .collect(Collectors.toList());
                level3Count = relationMapper.selectCount(
                        new LambdaQueryWrapper<DistributionRelationModel>()
                                .in(DistributionRelationModel::getParentId, level2Ids));
            }
        }

        // 佣金比例配置（展示）
        BigDecimal rate1 = BigDecimal.ZERO;
        BigDecimal rate2 = BigDecimal.ZERO;
        BigDecimal rate3 = BigDecimal.ZERO;
        List<DistributionConfigModel> configs = configMapper.selectList(
                new LambdaQueryWrapper<DistributionConfigModel>()
                        .orderByAsc(DistributionConfigModel::getLevel));
        for (DistributionConfigModel cfg : configs) {
            if (cfg.getRate() == null) {
                continue;
            }
            if (cfg.getLevel() != null && cfg.getLevel() == 1) {
                rate1 = cfg.getRate();
            } else if (cfg.getLevel() != null && cfg.getLevel() == 2) {
                rate2 = cfg.getRate();
            } else if (cfg.getLevel() != null && cfg.getLevel() == 3) {
                rate3 = cfg.getRate();
            }
        }

        return DistributionCenterResponse.builder()
                .parentUserId(parentUserId)
                .parentNickname(parentNickname)
                .availableAmount(available)
                .lockedAmount(locked)
                .withdrawnAmount(withdrawn)
                .totalEarned(total)
                .level1Count(level1Count)
                .level2Count(level2Count)
                .level3Count(level3Count)
                .minWithdrawalAmount(MIN_WITHDRAWAL)
                .level1Rate(rate1)
                .level2Rate(rate2)
                .level3Rate(rate3)
                .build();
    }

    @Override
    public PageResponse<CommissionRecordResponse> getCommissionRecords(String userId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        }
        size = Math.min(size, 100);

        PageHelper.startPage(page, size);
        List<CommissionRecordModel> records = commissionRecordMapper.selectList(
                new LambdaQueryWrapper<CommissionRecordModel>()
                        .eq(CommissionRecordModel::getUserId, userId)
                        .orderByDesc(CommissionRecordModel::getCreatedAt));
        PageInfo<CommissionRecordModel> pageInfo = new PageInfo<>(records);

        List<CommissionRecordResponse> result = new ArrayList<>();
        for (CommissionRecordModel rec : records) {
            result.add(convertToResponse(rec));
        }

        PageResponse<CommissionRecordResponse> response = new PageResponse<>();
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
    public List<DistributionTeamResponse> getTeam(String userId) {
        List<DistributionTeamResponse> result = new ArrayList<>();
        // 一级
        List<DistributionRelationModel> level1Rels = relationMapper.selectList(
                new LambdaQueryWrapper<DistributionRelationModel>()
                        .eq(DistributionRelationModel::getParentId, userId)
                        .orderByDesc(DistributionRelationModel::getCreatedAt));
        List<String> level1Ids = new ArrayList<>();
        for (DistributionRelationModel rel : level1Rels) {
            level1Ids.add(rel.getUserId());
            result.add(buildTeamMember(rel.getUserId(), 1, rel.getCreatedAt()));
        }
        // 二级
        if (!level1Ids.isEmpty()) {
            List<DistributionRelationModel> level2Rels = relationMapper.selectList(
                    new LambdaQueryWrapper<DistributionRelationModel>()
                            .in(DistributionRelationModel::getParentId, level1Ids)
                            .orderByDesc(DistributionRelationModel::getCreatedAt));
            List<String> level2Ids = new ArrayList<>();
            for (DistributionRelationModel rel : level2Rels) {
                level2Ids.add(rel.getUserId());
                result.add(buildTeamMember(rel.getUserId(), 2, rel.getCreatedAt()));
            }
            // 三级
            if (!level2Ids.isEmpty()) {
                List<DistributionRelationModel> level3Rels = relationMapper.selectList(
                        new LambdaQueryWrapper<DistributionRelationModel>()
                                .in(DistributionRelationModel::getParentId, level2Ids)
                                .orderByDesc(DistributionRelationModel::getCreatedAt));
                for (DistributionRelationModel rel : level3Rels) {
                    result.add(buildTeamMember(rel.getUserId(), 3, rel.getCreatedAt()));
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyWithdrawal(WithdrawalApplyRequest request) {
        String userId = request.getUserId();
        BigDecimal amount = request.getAmount();
        if (StrUtil.isBlank(userId) || amount == null) {
            throw new BusinessException("参数不完整");
        }
        if (amount.compareTo(MIN_WITHDRAWAL) < 0) {
            throw new BusinessException("未达到最低提现金额（10元）");
        }
        BigDecimal available = commissionRecordMapper.sumAmountByUserAndStatus(userId, "AVAILABLE");
        if (available.compareTo(amount) < 0) {
            throw new BusinessException("可提现余额不足");
        }

        DistributionWithdrawalModel withdrawal = new DistributionWithdrawalModel();
        withdrawal.setUserId(userId);
        withdrawal.setAmount(amount);
        withdrawal.setStatus("PENDING");
        withdrawal.setApplyTime(LocalDateTime.now());
        withdrawalMapper.insert(withdrawal);

        // 从最早生成的佣金开始锁定，直到覆盖申请金额
        BigDecimal remain = amount;
        List<CommissionRecordModel> records = commissionRecordMapper.selectAvailableByUser(userId);
        for (CommissionRecordModel rec : records) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            int n = commissionRecordMapper.lockById(rec.getId(), withdrawal.getId());
            if (n > 0) {
                remain = remain.subtract(rec.getCommissionAmount());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "提现申请已提交，等待管理员审核");
        result.put("withdrawalId", withdrawal.getId());
        return result;
    }

    @Override
    public List<DistributionWithdrawalResponse> getWithdrawals(String userId) {
        List<DistributionWithdrawalModel> withdrawals = withdrawalMapper.selectList(
                new LambdaQueryWrapper<DistributionWithdrawalModel>()
                        .eq(DistributionWithdrawalModel::getUserId, userId)
                        .orderByDesc(DistributionWithdrawalModel::getApplyTime));
        List<DistributionWithdrawalResponse> result = new ArrayList<>();
        for (DistributionWithdrawalModel w : withdrawals) {
            result.add(DistributionWithdrawalResponse.builder()
                    .id(w.getId())
                    .amount(w.getAmount())
                    .status(w.getStatus())
                    .statusDesc(withdrawalStatusDesc(w.getStatus()))
                    .applyTime(w.getApplyTime())
                    .reviewTime(w.getReviewTime())
                    .reviewer(w.getReviewer())
                    .remark(w.getRemark())
                    .payoutStatus(w.getPayoutStatus())
                    .payoutStatusDesc(payoutStatusDesc(w.getPayoutStatus()))
                    .payoutTime(w.getPayoutTime())
                    .payoutNo(w.getPayoutNo())
                    .failReason(w.getFailReason())
                    .build());
        }
        return result;
    }

    @Override
    public Map<String, Object> generatePoster(String userId) {
        if (StrUtil.isBlank(userId)) {
            log.warn("生成分销海报失败: 缺少用户标识 userId={}", userId);
            throw new BusinessException("缺少用户标识，无法生成海报");
        }
        UserModel user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("生成分销海报失败: 用户不存在 userId={}", userId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        // 店铺名/落地页后台可配置（后台管理系统-分销管理-分享与海报设置）
        String shopName = shopConfigService.getString("poster.shopName", "琼浆玉液官方旗舰店");
        String nickname = user != null && StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : "分享好友";
        String avatarUrl = user != null ? user.getAvatar() : null;
        // 落地页默认首页：好友扫码/识别后直接进入小程序购物
        String qrPage = shopConfigService.getString("poster.qrPage", "pages/index/index");
        String qrContent = qrPage + "?referrer=" + userId;
        try {
            // 1) 优先使用微信真实小程序码（后台配置 appid/secret 后生效；scene 为分享人 userId）
            try {
                byte[] wxCodeBytes = wechatUtil.getWxacodeUnlimit(userId, qrPage, 430);
                if (wxCodeBytes != null && wxCodeBytes.length > 0) {
                    BufferedImage wxImage = ImageIO.read(new ByteArrayInputStream(wxCodeBytes));
                    if (wxImage != null) {
                        String base64 = PosterGenerator.generatePosterBase64(nickname, avatarUrl, shopName, wxImage, userId);
                        result.put("imageBase64", base64);
                        result.put("qrContent", qrContent);
                        result.put("isWxCode", true);
                        result.put("message", "生成成功");
                        return result;
                    }
                }
            } catch (Exception wxEx) {
                log.warn("生成微信小程序码失败，回退普通二维码: userId={}, page={}", userId, qrPage, wxEx);
            }
            // 2) 未配置/失败回退：ZXing 普通二维码
            String base64 = PosterGenerator.generatePosterBase64(nickname, avatarUrl, shopName, qrContent, userId);
            result.put("imageBase64", base64);
            result.put("qrContent", qrContent);
            result.put("isWxCode", false);
            result.put("message", "生成成功");
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成分销海报失败: userId={}, shopName={}, qrPage={}, avatarUrl={}", userId, shopName, qrPage, avatarUrl, e);
            throw new BusinessException("海报生成失败：请稍后重试");
        }
    }

    @Override
    public Map<String, Object> getShareConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("title", shopConfigService.getString("share.title", "邀请你一起买好酒，分享赚佣金"));
        result.put("desc", shopConfigService.getString("share.desc", "好友下单，你享返利，快来一起选购吧"));
        result.put("imageUrl", shopConfigService.getString("share.imageUrl", ""));
        result.put("path", shopConfigService.getString("share.path", "pages/index/index"));
        result.put("shopName", shopConfigService.getString("poster.shopName", "琼浆玉液官方旗舰店"));
        // 不回传 secret 等敏感信息，仅提示小程序码能力是否已就绪
        result.put("configured", shopConfigService.isWechatConfigured());
        return result;
    }

    @Override
    public List<Map<String, Object>> getConfig() {
        List<DistributionConfigModel> configs = configMapper.selectList(
                new LambdaQueryWrapper<DistributionConfigModel>()
                        .orderByAsc(DistributionConfigModel::getLevel));
        List<Map<String, Object>> result = new ArrayList<>();
        for (DistributionConfigModel cfg : configs) {
            Map<String, Object> item = new HashMap<>();
            item.put("level", cfg.getLevel());
            item.put("rate", cfg.getRate());
            item.put("ratePercent", cfg.getRate() != null
                    ? cfg.getRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            item.put("enabled", cfg.getEnabled());
            result.add(item);
        }
        return result;
    }

    // ==================== 支付/订单链路调用 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCommission(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return;
        }
        // 仅普通商城订单参与分销（秒杀 SEO / 团购 不走分销）
        if (!orderNo.startsWith("ORD")) {
            return;
        }
        // 幂等：同一订单已生成过则跳过
        if (commissionRecordMapper.countByOrderNo(orderNo) > 0) {
            return;
        }
        OrderModel order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !OrderStatus.PAID.name().equals(order.getStatus())) {
            return;
        }
        String buyerId = order.getUserId();
        if (StrUtil.isBlank(buyerId)) {
            return;
        }
        BigDecimal payAmount = order.getPayAmount() != null && order.getPayAmount().compareTo(BigDecimal.ZERO) > 0
                ? order.getPayAmount() : order.getTotalAmount();
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 启用中的佣金比例
        List<DistributionConfigModel> configs = configMapper.selectList(
                new LambdaQueryWrapper<DistributionConfigModel>()
                        .eq(DistributionConfigModel::getEnabled, 1)
                        .orderByAsc(DistributionConfigModel::getLevel));
        Map<Integer, BigDecimal> rateMap = new HashMap<>();
        for (DistributionConfigModel cfg : configs) {
            if (cfg.getLevel() != null && cfg.getRate() != null) {
                rateMap.put(cfg.getLevel(), cfg.getRate());
            }
        }

        // 沿最新上级链向上生成三级佣金
        String cursor = buyerId;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            DistributionRelationModel rel = relationMapper.selectByUserId(cursor);
            if (rel == null || StrUtil.isBlank(rel.getParentId())) {
                break;
            }
            String upperId = rel.getParentId();
            BigDecimal rate = rateMap.get(level);
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal amount = payAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    CommissionRecordModel record = new CommissionRecordModel();
                    record.setOrderNo(orderNo);
                    record.setBuyerUserId(buyerId);
                    record.setLevel(level);
                    record.setUserId(upperId);
                    record.setOrderAmount(payAmount);
                    record.setRate(rate);
                    record.setCommissionAmount(amount);
                    record.setStatus("PENDING");
                    commissionRecordMapper.insert(record);
                    log.info("生成分销佣金: orderNo={}, level={}, userId={}, amount={}", orderNo, level, upperId, amount);
                }
            }
            cursor = upperId;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleCommission(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return;
        }
        int n = commissionRecordMapper.settleByOrderNo(orderNo);
        if (n > 0) {
            log.info("分销佣金入账: orderNo={}, count={}", orderNo, n);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCommission(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            return;
        }
        int n = commissionRecordMapper.cancelByOrderNo(orderNo);
        if (n > 0) {
            log.info("分销佣金作废: orderNo={}, count={}", orderNo, n);
        }
    }

    // ==================== 私有方法 ====================

    private CommissionRecordResponse convertToResponse(CommissionRecordModel rec) {
        return CommissionRecordResponse.builder()
                .id(rec.getId())
                .orderNo(rec.getOrderNo())
                .level(rec.getLevel())
                .orderAmount(rec.getOrderAmount())
                .rate(rec.getRate())
                .commissionAmount(rec.getCommissionAmount())
                .status(rec.getStatus())
                .statusDesc(commissionStatusDesc(rec.getStatus()))
                .settledTime(rec.getSettledTime())
                .createdAt(rec.getCreatedAt())
                .build();
    }

    private DistributionTeamResponse buildTeamMember(String memberUserId, int level, LocalDateTime bindTime) {
        String nickname = null;
        String avatar = null;
        UserModel user = userMapper.selectById(memberUserId);
        if (user != null) {
            nickname = user.getNickname();
            avatar = user.getAvatar();
        }
        return DistributionTeamResponse.builder()
                .userId(memberUserId)
                .nickname(nickname)
                .avatar(avatar)
                .level(level)
                .bindTime(bindTime)
                .build();
    }

    private String commissionStatusDesc(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING":
                return "待入账";
            case "AVAILABLE":
                return "可提现";
            case "LOCKED":
                return "提现中";
            case "WITHDRAWN":
                return "已提现";
            case "CANCELLED":
                return "已作废";
            default:
                return status;
        }
    }

    private String withdrawalStatusDesc(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "PENDING":
                return "待审核";
            case "APPROVED":
                return "已通过";
            case "REJECTED":
                return "已拒绝";
            default:
                return status;
        }
    }

    private String payoutStatusDesc(String status) {
        if (status == null || "NONE".equals(status)) {
            return "未打款";
        }
        switch (status) {
            case "PENDING":
                return "打款中";
            case "SUCCESS":
                return "打款成功";
            case "FAILED":
                return "打款失败";
            default:
                return status;
        }
    }
}