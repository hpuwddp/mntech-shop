package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.LotteryRequest;
import com.mntech.shop.order.dto.request.ReceivePrizeRequest;
import com.mntech.shop.order.dto.response.LotteryRecordResponse;
import com.mntech.shop.order.dto.response.LotteryResultResponse;
import com.mntech.shop.order.dto.response.PrizeResponse;
import com.mntech.shop.order.dto.response.ReceivePrizeResponse;
import com.mntech.shop.order.service.ILotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {
    private final ILotteryService lotteryService;

    /**
     * 执行抽奖
     */
    @PostMapping("/draw")
    public ResponseEntity<LotteryResultResponse> drawLottery(@Valid @RequestBody LotteryRequest request) {
        LotteryResultResponse result = lotteryService.drawLottery(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户剩余抽奖次数
     */
    @GetMapping("/quota/{userId}")
    public ResponseEntity<Map<String, Object>> getRemainingQuota(@PathVariable String userId) {
        Integer quota = lotteryService.getRemainingQuota(userId);
        // 2. 如果为null，说明是新用户，自动初始化
        if (quota == null) {
            // 插入默认配额
            String id = UUID.randomUUID().toString().replace("-", "");
            lotteryService.initQuota(id, userId, 2);//默认送抽奖次数2次
            quota = 2;
        }


        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("remainingQuota", quota != null ? quota : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有可用奖品
     */
    @GetMapping("/prizes")
    public ResponseEntity<List<PrizeResponse>> getAvailablePrizes() {
        List<PrizeResponse> prizes = lotteryService.getAvailablePrizes();
        return ResponseEntity.ok(prizes);
    }

    /**
     * 获取用户抽奖记录
     */
    @GetMapping("/records/{userId}")
    public ResponseEntity<List<LotteryRecordResponse>> getLotteryRecords(@PathVariable String userId) {
        List<LotteryRecordResponse> records = lotteryService.getLotteryRecords(userId);
        return ResponseEntity.ok(records);
    }

    /**
     * 获取用户中奖记录
     */
    @GetMapping("/win-records/{userId}")
    public ResponseEntity<List<LotteryRecordResponse>> getWinRecords(@PathVariable String userId, @RequestParam(defaultValue = "1") Integer pageNum,
                                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        List<LotteryRecordResponse> records = lotteryService.getWinRecords(userId);
        return ResponseEntity.ok(records);
    }


    /**
     * ============================================
     * 领取奖品接口
     * ============================================
     */
    @PostMapping("/win-records/{recordId}/receive")
    public ResponseEntity<ReceivePrizeResponse> receivePrize(
            @PathVariable String recordId,
            @Valid @RequestBody ReceivePrizeRequest request) {
        ReceivePrizeResponse response = lotteryService.receivePrize(recordId, request);
        return ResponseEntity.ok(response);
    }
}
