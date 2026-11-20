package com.mntech.shop.order.job;

import com.mntech.shop.order.service.IGroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 团购过期处理定时任务：
 * 1. 处理已过期未成团的团（拼团失败 → 已支付退款、待支付取消、库存回补）
 * 2. 处理支付超时的待支付订单（自动取消并回补库存）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GroupBuyExpireScheduler {

    private final IGroupBuyService groupBuyService;

    /**
     * 每 60 秒执行一次
     */
    @Scheduled(fixedDelay = 60000)
    public void processExpiredGroups() {
        try {
            groupBuyService.processExpiredTeamCore();
            log.info("团购过期处理完成");
        } catch (Exception e) {
            log.error("团购过期处理失败", e);
        }
    }
}