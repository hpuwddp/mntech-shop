package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.mntech.shop.common.enums.OrderStatus;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.LotteryRecordMapper;
import com.mntech.shop.order.dao.PrizeMapper;
import com.mntech.shop.order.dao.UserLotteryQuotaMapper;
import com.mntech.shop.order.dto.request.LotteryRequest;
import com.mntech.shop.order.dto.request.ReceivePrizeRequest;
import com.mntech.shop.order.dto.response.LotteryRecordResponse;
import com.mntech.shop.order.dto.response.LotteryResultResponse;
import com.mntech.shop.order.dto.response.PrizeResponse;
import com.mntech.shop.order.dto.response.ReceivePrizeResponse;
import com.mntech.shop.order.model.LotteryRecord;
import com.mntech.shop.order.model.Prize;
import com.mntech.shop.order.model.UserLotteryQuota;
import com.mntech.shop.order.service.ILotteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LotteryServiceImpl implements ILotteryService {
    private final PrizeMapper prizeMapper;
    private final LotteryRecordMapper lotteryRecordMapper;
    private final UserLotteryQuotaMapper userLotteryQuotaMapper;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional
    public LotteryResultResponse drawLottery(LotteryRequest request) {
        String userId = request.getUserId();
        UserLotteryQuota quota = getOrCreateQuota(userId);
        // 检查剩余次数
        if (quota.getRemainingQuota() <= 0) {
            throw new BusinessException("抽奖次数不足");
        }


        // 1. 检查用户剩余抽奖次数
        Integer remainingQuota = userLotteryQuotaMapper.selectRemainingQuota(userId);
        if (remainingQuota == null || remainingQuota <= 0) {
            throw new BusinessException("抽奖次数不足，请获取更多抽奖机会");
        }

        // 2. 获取所有可用奖品
        List<Prize> prizes = prizeMapper.selectAvailablePrizes();
        if (CollectionUtil.isEmpty(prizes)) {
            throw new BusinessException("暂无可用奖品，请稍后再试");
        }

        // 3. 执行抽奖算法
        Prize selectedPrize = performLottery(prizes);

        // 4. 记录抽奖结果
        LotteryRecord record = new LotteryRecord();
        record.setUserId(userId);
        record.setCreatedAt(LocalDateTime.now());

        LotteryResultResponse result = new LotteryResultResponse();

        if (selectedPrize != null) {
            // 中奖
            // 扣减奖品库存
            int updateResult = prizeMapper.deductPrizeStock(selectedPrize.getId());
            if (updateResult == 0) {
                throw new BusinessException("奖品库存不足，请重试");
            }

            record.setPrizeId(selectedPrize.getId());
            record.setPrizeName(selectedPrize.getName());
            record.setPrizeLevel(selectedPrize.getLevel());
            record.setPrizeImage(selectedPrize.getImage());
            record.setIsWin(true);
            record.setStatus(OrderStatus.PENDING.name());
            result.setIsWin(true);
            result.setPrizeId(selectedPrize.getId());
            result.setPrizeName(selectedPrize.getName());
            result.setPrizeImage(selectedPrize.getImage());
            result.setPrizeLevel(selectedPrize.getLevel());
            result.setPrizeLevelDesc(getLevelDesc(selectedPrize.getLevel()));
            result.setMessage("🎉 恭喜您获得：" + selectedPrize.getName());

            log.info("用户 {} 中奖：{}", userId, selectedPrize.getName());
        } else {
            // 未中奖
            record.setIsWin(false);
            record.setRemark("谢谢参与");

            result.setIsWin(false);
            result.setMessage("谢谢参与，下次好运！");

            log.info("用户 {} 未中奖", userId);
        }

        // 5. 保存抽奖记录
        lotteryRecordMapper.insert(record);

        // 6. 扣减用户抽奖次数
        int quotaResult = userLotteryQuotaMapper.deductQuota(userId);
        if (quotaResult == 0) {
            throw new BusinessException("抽奖次数扣减失败");
        }

        // 7. 查询剩余次数
        Integer remaining = userLotteryQuotaMapper.selectRemainingQuota(userId);
        result.setRemainingQuota(remaining != null ? remaining : 0);

        return result;
    }

    /**
     * 获取或创建用户抽奖配额
     */
    private UserLotteryQuota getOrCreateQuota(String userId) {
        UserLotteryQuota quota = userLotteryQuotaMapper.selectByUserId(userId);
        if (quota == null) {
            // 新用户：自动创建，分配默认抽奖次数
            quota = new UserLotteryQuota();
            quota.setUserId(userId);
            quota.setTotalQuota(3);      // 默认给3次
            quota.setUsedQuota(0);
            quota.setRemainingQuota(3);
            quota.setCreatedAt(LocalDateTime.now());
            quota.setUpdatedAt(LocalDateTime.now());
            quota.setDeleted(0);
            userLotteryQuotaMapper.insert(quota);
            log.info("为新用户 {} 创建抽奖配额，默认 {} 次", userId, 3);
        }
        return quota;
    }

    /**
     * 抽奖算法
     * 根据奖品概率随机抽取
     */
    private Prize performLottery(List<Prize> prizes) {
        // 1. 过滤掉库存为0的奖品
        List<Prize> availablePrizes = prizes.stream()
                .filter(p -> p.getRemainingQuantity() > 0)
                .collect(Collectors.toList());

        if (CollectionUtil.isEmpty(availablePrizes)) {
            return null;  // 没有可用奖品
        }

        // 2. 重新计算总概率（只计算有库存的奖品）
        BigDecimal totalProbability = availablePrizes.stream()
                .map(Prize::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 随机数 0-1
        double randomValue = RANDOM.nextDouble();

        // 4. 累加概率
        double cumulative = 0.0;
        for (Prize prize : availablePrizes) {
            cumulative += prize.getProbability().doubleValue();
            // 检查是否落在该奖品区间
            if (randomValue <= cumulative) {
                return prize;
            }
        }

        // 5. 如果随机数大于总概率（理论上不会发生），返回null
        return null;
    }

    @Override
    public Integer getRemainingQuota(String userId) {
        return userLotteryQuotaMapper.selectRemainingQuota(userId);
    }

    @Override
    public List<LotteryRecordResponse> getWinRecordsPage(String userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<LotteryRecord> records = lotteryRecordMapper.selectWinRecordsByUserIdPage(userId, offset, pageSize);
        return convertToRecordDTO(records);
    }

    @Override
    public int countWinRecords(String userId) {
        return lotteryRecordMapper.countWinRecords(userId);
    }

    @Override
    public List<PrizeResponse> getAvailablePrizes() {
        List<Prize> prizes = prizeMapper.selectAvailablePrizes();
        List<PrizeResponse> result = new ArrayList<>();

        for (Prize prize : prizes) {
            PrizeResponse dto = new PrizeResponse();
            BeanUtil.copyProperties(prize, dto);
            dto.setLevelDesc(getLevelDesc(prize.getLevel()));
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<LotteryRecordResponse> getLotteryRecords(String userId) {
        List<LotteryRecord> records = lotteryRecordMapper.selectByUserId(userId);
        return convertToRecordDTO(records);
    }

    @Override
    public List<LotteryRecordResponse> getWinRecords(String userId) {
        List<LotteryRecord> records = lotteryRecordMapper.selectWinRecordsByUserId(userId);
        return convertToRecordDTO(records);
    }

    @Override
    public int initQuota(String id, String userId, int totalQuota) {
        return userLotteryQuotaMapper.initQuota(id, userId, totalQuota);
    }

    // ===== 私有方法 =====

    private String getLevelDesc(Integer level) {
        if (level == null) return "参与奖";
        switch (level) {
            case 1:
                return "一等奖";
            case 2:
                return "二等奖";
            case 3:
                return "三等奖";
            case 4:
                return "四等奖";
            case 0:
                return "谢谢参与";
            default:
                return "参与奖";
        }
    }

    private List<LotteryRecordResponse> convertToRecordDTO(List<LotteryRecord> records) {
        List<LotteryRecordResponse> result = new ArrayList<>();
        for (LotteryRecord record : records) {
            LotteryRecordResponse dto = new LotteryRecordResponse();
            BeanUtil.copyProperties(record, dto);
            dto.setPrizeLevelDesc(getLevelDesc(record.getPrizeLevel()));
            result.add(dto);
        }
        return result;
    }

    /**
     * ============================================
     * 领取奖品
     * ============================================
     */
    @Override
    @Transactional
    public ReceivePrizeResponse receivePrize(String recordId, ReceivePrizeRequest request) {
        String userId = request.getUserId();

        log.info("领取奖品请求: recordId={}, userId={}", recordId, userId);

        // 1. 查询中奖记录
        LotteryRecord record = lotteryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("中奖记录不存在");
        }

        // 2. 验证是否是当前用户的中奖记录
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此记录");
        }

        // 3. 验证是否已领取
        if ("RECEIVED".equals(record.getStatus())) {
            throw new BusinessException("该奖品已领取");
        }

        // 4. 验证是否已过期（可以根据业务需要设置过期时间，比如7天）
        if (record.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException("奖品已过期，无法领取");
        }

        // 5. 验证是否中奖
        if (record.getIsWin() == null || !record.getIsWin()) {
            throw new BusinessException("该记录未中奖");
        }

        // 6. 保存收货地址信息（如果记录表有地址字段就更新，否则可以单独存到地址表）
        // 方式一：直接更新 lottery_record 表
        record.setStatus("RECEIVED");

        record.setReceiverName(request.getAddress().getReceiverName());
        record.setReceiverPhone(request.getAddress().getReceiverPhone());
        record.setReceiverAddress(request.getAddress().getDetailAddress());

        int updateResult = lotteryRecordMapper.updateById(record);
        if (updateResult == 0) {
            throw new BusinessException("领取失败，请重试");
        }

        // 7. 构建响应
        ReceivePrizeResponse response = new ReceivePrizeResponse();
        response.setRecordId(record.getId());
        response.setPrizeName(record.getPrizeName());
        response.setPrizeLevel(record.getPrizeLevel());
        response.setPrizeLevelDesc(getLevelDesc(record.getPrizeLevel()));
        response.setStatus("RECEIVED");

        String fullAddress = record.getReceiverAddress();
        response.setReceiveAddress(fullAddress);
        response.setReceiveTime(LocalDateTime.now());
        response.setMessage("领取成功！");

        log.info("用户 {} 成功领取奖品：{}", userId, record.getPrizeName());

        return response;
    }


}
