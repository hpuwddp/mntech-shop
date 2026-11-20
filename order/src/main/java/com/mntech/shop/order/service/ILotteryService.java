package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.LotteryRequest;
import com.mntech.shop.order.dto.request.ReceivePrizeRequest;
import com.mntech.shop.order.dto.response.LotteryRecordResponse;
import com.mntech.shop.order.dto.response.LotteryResultResponse;
import com.mntech.shop.order.dto.response.PrizeResponse;
import com.mntech.shop.order.dto.response.ReceivePrizeResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ILotteryService {
    /**
     * 执行抽奖
     */
    LotteryResultResponse drawLottery(LotteryRequest request);

    /**
     * 获取用户剩余抽奖次数
     */
    Integer getRemainingQuota(String userId);

    /**
     * 获取所有可用奖品
     */
    List<PrizeResponse> getAvailablePrizes();

    /**
     * 获取用户抽奖记录
     */
    List<LotteryRecordResponse> getLotteryRecords(String userId);

    /**
     * 获取用户中奖记录
     */
    List<LotteryRecordResponse> getWinRecords(String userId);

    int initQuota(String id, String userId, int totalQuota);

    /**
     * 获取用户中奖记录（分页）
     */
    List<LotteryRecordResponse> getWinRecordsPage(String userId, int pageNum, int pageSize);

    /**
     * 统计用户中奖数量
     */
    int countWinRecords(String userId);

    /**
     * 领取奖品
     */
    ReceivePrizeResponse receivePrize(String recordId, ReceivePrizeRequest request);
}
