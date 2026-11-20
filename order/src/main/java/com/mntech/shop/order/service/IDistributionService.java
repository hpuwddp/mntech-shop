package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.BindRelationRequest;
import com.mntech.shop.order.dto.request.WithdrawalApplyRequest;
import com.mntech.shop.order.dto.response.CommissionRecordResponse;
import com.mntech.shop.order.dto.response.DistributionCenterResponse;
import com.mntech.shop.order.dto.response.DistributionTeamResponse;
import com.mntech.shop.order.dto.response.DistributionWithdrawalResponse;
import com.mntech.shop.order.dto.response.PageResponse;

import java.util.List;
import java.util.Map;

public interface IDistributionService {

    /**
     * 绑定/更换上级（推荐人）
     */
    Map<String, Object> bindRelation(BindRelationRequest request);

    /**
     * 查询我的上级（推广人）
     */
    Map<String, Object> getParent(String userId);

    /**
     * 分销中心汇总
     */
    DistributionCenterResponse getCenter(String userId);

    /**
     * 佣金明细（分页）
     */
    PageResponse<CommissionRecordResponse> getCommissionRecords(String userId, int page, int size);

    /**
     * 我的团队（三级）
     */
    List<DistributionTeamResponse> getTeam(String userId);

    /**
     * 申请提现
     */
    Map<String, Object> applyWithdrawal(WithdrawalApplyRequest request);

    /**
     * 提现记录
     */
    List<DistributionWithdrawalResponse> getWithdrawals(String userId);

    /**
     * 生成分销海报（头像+昵称+店铺信息+二维码），返回 base64 PNG
     */
    Map<String, Object> generatePoster(String userId);

    /**
     * 佣金比例配置（前端说明展示）
     */
    List<Map<String, Object>> getConfig();

    /**
     * 分享配置（标题/描述/封面图/落地页路径/是否已配置微信），供小程序分享与前端展示
     */
    Map<String, Object> getShareConfig();

    // ==================== 供支付/订单链路调用的内部方法 ====================

    /**
     * 订单支付成功后按上级链生成三级佣金（幂等，只处理普通商城订单 ORD）
     */
    void generateCommission(String orderNo);

    /**
     * 确认收货后佣金入账 PENDING -> AVAILABLE
     */
    void settleCommission(String orderNo);

    /**
     * 订单取消/退款后佣金作废
     */
    void cancelCommission(String orderNo);
}