package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.PointsSignRequest;
import com.mntech.shop.order.dto.response.PointsRecordResponse;
import com.mntech.shop.order.dto.response.PointsSummaryResponse;

import java.util.List;
import java.util.Map;

public interface IPointsService {

    /**
     * 用户签到送积分（每日可签一次，连续 7 天奖励）
     */
    Map<String, Object> sign(PointsSignRequest request);

    /**
     * 积分汇总（总积分/今日是否已签/连续天数）
     */
    PointsSummaryResponse getSummary(String userId);

    /**
     * 积分流水（按时间倒序）
     */
    List<PointsRecordResponse> getRecords(String userId);
}