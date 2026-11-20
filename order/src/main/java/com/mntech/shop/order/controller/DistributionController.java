package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.BindRelationRequest;
import com.mntech.shop.order.dto.request.WithdrawalApplyRequest;
import com.mntech.shop.order.dto.response.CommissionRecordResponse;
import com.mntech.shop.order.dto.response.DistributionCenterResponse;
import com.mntech.shop.order.dto.response.DistributionTeamResponse;
import com.mntech.shop.order.dto.response.DistributionWithdrawalResponse;
import com.mntech.shop.order.dto.response.PageResponse;
import com.mntech.shop.order.service.IDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/distribution")
@RequiredArgsConstructor
public class DistributionController {

    private final IDistributionService distributionService;

    /**
     * 绑定/更换上级（分享链接携带推荐人时调用）
     */
    @PostMapping("/relation/bind")
    public ResponseEntity<Map<String, Object>> bindRelation(@RequestBody BindRelationRequest request) {
        return ResponseEntity.ok(distributionService.bindRelation(request));
    }

    /**
     * 查询我的上级（推广人）
     */
    @GetMapping("/relation/{userId}")
    public ResponseEntity<Map<String, Object>> getParent(@PathVariable String userId) {
        return ResponseEntity.ok(distributionService.getParent(userId));
    }

    /**
     * 分销中心汇总
     */
    @GetMapping("/center/{userId}")
    public ResponseEntity<DistributionCenterResponse> getCenter(@PathVariable String userId) {
        return ResponseEntity.ok(distributionService.getCenter(userId));
    }

    /**
     * 佣金明细（分页）
     */
    @GetMapping("/commission/{userId}")
    public ResponseEntity<PageResponse<CommissionRecordResponse>> getCommissionRecords(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(distributionService.getCommissionRecords(userId, page, size));
    }

    /**
     * 我的团队（三级）
     */
    @GetMapping("/team/{userId}")
    public ResponseEntity<List<DistributionTeamResponse>> getTeam(@PathVariable String userId) {
        return ResponseEntity.ok(distributionService.getTeam(userId));
    }

    /**
     * 申请提现（满 10 元可申请）
     */
    @PostMapping("/withdrawal/apply")
    public ResponseEntity<Map<String, Object>> applyWithdrawal(@RequestBody WithdrawalApplyRequest request) {
        return ResponseEntity.ok(distributionService.applyWithdrawal(request));
    }

    /**
     * 提现记录
     */
    @GetMapping("/withdrawal/{userId}")
    public ResponseEntity<List<DistributionWithdrawalResponse>> getWithdrawals(@PathVariable String userId) {
        return ResponseEntity.ok(distributionService.getWithdrawals(userId));
    }

    /**
     * 生成分销海报（头像+昵称+店铺信息+二维码），返回 base64 PNG
     */
    @GetMapping("/poster/{userId}")
    public ResponseEntity<Map<String, Object>> getPoster(@PathVariable String userId) {
        return ResponseEntity.ok(distributionService.generatePoster(userId));
    }

    /**
     * 佣金比例配置（前端说明展示）
     */
    @GetMapping("/config")
    public ResponseEntity<List<Map<String, Object>>> getConfig() {
        return ResponseEntity.ok(distributionService.getConfig());
    }

    /**
     * 分享配置（标题/描述/封面图/落地页路径/是否已配置微信），供小程序分享与前端展示
     */
    @GetMapping("/share/config")
    public ResponseEntity<Map<String, Object>> getShareConfig() {
        return ResponseEntity.ok(distributionService.getShareConfig());
    }
}