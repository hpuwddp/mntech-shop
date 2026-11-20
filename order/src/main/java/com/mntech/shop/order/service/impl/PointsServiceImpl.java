package com.mntech.shop.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.PointsAccountMapper;
import com.mntech.shop.order.dao.PointsRecordMapper;
import com.mntech.shop.order.dao.PointsSignMapper;
import com.mntech.shop.order.dto.request.PointsSignRequest;
import com.mntech.shop.order.dto.response.PointsRecordResponse;
import com.mntech.shop.order.dto.response.PointsSummaryResponse;
import com.mntech.shop.order.model.PointsAccountModel;
import com.mntech.shop.order.model.PointsRecordModel;
import com.mntech.shop.order.model.PointsSignModel;
import com.mntech.shop.order.service.IPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointsServiceImpl implements IPointsService {

    /** 每日签到基础积分 */
    private static final int DAILY_POINTS = 5;

    /** 连续签到满 7 天奖励积分 */
    private static final int WEEK_BONUS = 20;

    /** 连续签到奖励周期 */
    private static final int WEEK_CYCLE = 7;

    private final PointsAccountMapper accountMapper;
    private final PointsRecordMapper recordMapper;
    private final PointsSignMapper signMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sign(PointsSignRequest request) {
        String userId = request.getUserId();
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException("参数不完整");
        }
        LocalDate today = LocalDate.now();
        PointsSignModel todaySign = signMapper.selectByUserIdAndDate(userId, today);
        if (todaySign != null) {
            throw new BusinessException("今日已签到");
        }
        // 连续签到天数：昨天为止的连续数 + 1（今天）
        int prevStreak = signMapper.countConsecutiveEndingAt(userId, today.minusDays(1));
        int newStreak = prevStreak + 1;
        int base = DAILY_POINTS;
        int bonus = 0;
        if (newStreak % WEEK_CYCLE == 0) {
            bonus = WEEK_BONUS;
        }
        int total = base + bonus;

        // 1) 写签到记录（唯一索引防并发重复；撞唯一键按已签到处理）
        PointsSignModel sign = new PointsSignModel();
        sign.setUserId(userId);
        sign.setSignDate(today);
        sign.setPoints(total);
        try {
            signMapper.insert(sign);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("今日已签到");
        }

        // 2) 写积分流水（基础签到 + 连续奖励分两条）
        writeRecord(userId, "SIGN", base, "每日签到");
        if (bonus > 0) {
            writeRecord(userId, "SIGN_BONUS", bonus, "连续签到第" + newStreak + "天奖励");
        }

        // 3) 累加积分账户（不存在则新建；并发下靠唯一索引 + 捕获冲突再原子累加）
        PointsAccountModel account = selectAccount(userId);
        if (account == null) {
            PointsAccountModel na = new PointsAccountModel();
            na.setUserId(userId);
            na.setTotalPoints(total);
            try {
                accountMapper.insert(na);
            } catch (DuplicateKeyException e) {
                accountMapper.increasePoints(userId, total);
            }
        } else {
            accountMapper.increasePoints(userId, total);
        }

        int totalPoints = refreshTotalPoints(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "签到成功");
        result.put("points", base);
        result.put("bonus", bonus);
        result.put("totalEarned", total);
        result.put("consecutiveDays", newStreak);
        result.put("totalPoints", totalPoints);
        log.info("用户签到成功: userId={}, streak={}, points={}, bonus={}", userId, newStreak, base, bonus);
        return result;
    }

    @Override
    public PointsSummaryResponse getSummary(String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException("参数不完整");
        }
        LocalDate today = LocalDate.now();
        PointsSignModel todaySign = signMapper.selectByUserIdAndDate(userId, today);
        boolean todaySigned = todaySign != null;
        int consecutiveDays = todaySigned
                ? signMapper.countConsecutiveEndingAt(userId, today)
                : signMapper.countConsecutiveEndingAt(userId, today.minusDays(1));
        LocalDate latest = signMapper.selectLatestSignDate(userId);
        PointsAccountModel account = selectAccount(userId);
        return PointsSummaryResponse.builder()
                .totalPoints(account != null ? account.getTotalPoints() : 0)
                .todaySigned(todaySigned)
                .consecutiveDays(consecutiveDays)
                .lastSignDate(latest != null ? latest.format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .dailyPoints(DAILY_POINTS)
                .weekBonus(WEEK_BONUS)
                .build();
    }

    @Override
    public List<PointsRecordResponse> getRecords(String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new BusinessException("参数不完整");
        }
        List<PointsRecordModel> records = recordMapper.selectList(
                new LambdaQueryWrapper<PointsRecordModel>()
                        .eq(PointsRecordModel::getUserId, userId)
                        .orderByDesc(PointsRecordModel::getCreatedAt)
                        .orderByDesc(PointsRecordModel::getId));
        List<PointsRecordResponse> result = new ArrayList<>();
        for (PointsRecordModel rec : records) {
            result.add(PointsRecordResponse.builder()
                    .id(rec.getId())
                    .changeType(rec.getChangeType())
                    .changeTypeDesc(changeTypeDesc(rec.getChangeType()))
                    .points(rec.getPoints())
                    .remark(rec.getRemark())
                    .createdAt(rec.getCreatedAt())
                    .build());
        }
        return result;
    }

    private void writeRecord(String userId, String changeType, int points, String remark) {
        PointsRecordModel rec = new PointsRecordModel();
        rec.setUserId(userId);
        rec.setChangeType(changeType);
        rec.setPoints(points);
        rec.setRemark(remark);
        recordMapper.insert(rec);
    }

    private PointsAccountModel selectAccount(String userId) {
        return accountMapper.selectOne(
                new LambdaQueryWrapper<PointsAccountModel>()
                        .eq(PointsAccountModel::getUserId, userId)
                        .last("LIMIT 1"));
    }

    private int refreshTotalPoints(String userId) {
        PointsAccountModel account = selectAccount(userId);
        return account != null && account.getTotalPoints() != null ? account.getTotalPoints() : 0;
    }

    private String changeTypeDesc(String changeType) {
        if (changeType == null) {
            return "";
        }
        switch (changeType) {
            case "SIGN":
                return "每日签到";
            case "SIGN_BONUS":
                return "连续签到奖励";
            default:
                return changeType;
        }
    }
}