package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.CommissionRecordModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CommissionRecordMapper extends BaseMapper<CommissionRecordModel> {

    /**
     * 查询某订单已生成的佣金记录（幂等判断）
     */
    @Select("SELECT * FROM commission_record WHERE order_no = #{orderNo} AND deleted = 0")
    List<CommissionRecordModel> selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT COUNT(*) FROM commission_record WHERE order_no = #{orderNo} AND deleted = 0")
    int countByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 按用户与状态汇总佣金金额
     */
    @Select("SELECT COALESCE(SUM(commission_amount), 0) FROM commission_record " +
            "WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    BigDecimal sumAmountByUserAndStatus(@Param("userId") String userId, @Param("status") String status);

    /**
     * 查询用户全部可提现佣金（按生成时间升序，用于提现锁定）
     */
    @Select("SELECT * FROM commission_record WHERE user_id = #{userId} AND status = 'AVAILABLE' " +
            "AND deleted = 0 ORDER BY created_at ASC, id ASC")
    List<CommissionRecordModel> selectAvailableByUser(@Param("userId") String userId);

    /**
     * 确认收货后：订单佣金入账 PENDING -> AVAILABLE
     */
    @Update("UPDATE commission_record SET status = 'AVAILABLE', settled_time = NOW(), updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status = 'PENDING' AND deleted = 0")
    int settleByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 订单取消/退款：待入账或已入账但未提现的佣金作废
     */
    @Update("UPDATE commission_record SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND status IN ('PENDING','AVAILABLE') AND deleted = 0")
    int cancelByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 提现申请：锁定单条佣金
     */
    @Update("UPDATE commission_record SET status = 'LOCKED', withdrawal_id = #{withdrawalId}, updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'AVAILABLE' AND deleted = 0")
    int lockById(@Param("id") String id, @Param("withdrawalId") String withdrawalId);

    /**
     * 提现审核拒绝：退回佣金为可提现
     */
    @Update("UPDATE commission_record SET status = 'AVAILABLE', withdrawal_id = NULL, updated_at = NOW() " +
            "WHERE withdrawal_id = #{withdrawalId} AND status = 'LOCKED' AND deleted = 0")
    int unlockByWithdrawalId(@Param("withdrawalId") String withdrawalId);

    /**
     * 提现审核通过：佣金置为已提现
     */
    @Update("UPDATE commission_record SET status = 'WITHDRAWN', updated_at = NOW() " +
            "WHERE withdrawal_id = #{withdrawalId} AND status = 'LOCKED' AND deleted = 0")
    int markWithdrawnByWithdrawalId(@Param("withdrawalId") String withdrawalId);
}