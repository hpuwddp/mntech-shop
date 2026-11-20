package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.PointsSignRequest;
import com.mntech.shop.order.dto.response.PointsRecordResponse;
import com.mntech.shop.order.dto.response.PointsSummaryResponse;
import com.mntech.shop.order.service.IPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final IPointsService pointsService;

    /**
     * 用户签到送积分（每日一次，连续 7 天奖励）
     */
    @PostMapping("/sign")
    public ResponseEntity<Map<String, Object>> sign(@RequestBody PointsSignRequest request) {
        return ResponseEntity.ok(pointsService.sign(request));
    }

    /**
     * 积分汇总（总积分/今日是否已签/连续天数/规则说明）
     */
    @GetMapping("/summary/{userId}")
    public ResponseEntity<PointsSummaryResponse> getSummary(@PathVariable String userId) {
        return ResponseEntity.ok(pointsService.getSummary(userId));
    }

    /**
     * 积分流水（倒序）
     */
    @GetMapping("/records/{userId}")
    public ResponseEntity<List<PointsRecordResponse>> getRecords(@PathVariable String userId) {
        return ResponseEntity.ok(pointsService.getRecords(userId));
    }
}