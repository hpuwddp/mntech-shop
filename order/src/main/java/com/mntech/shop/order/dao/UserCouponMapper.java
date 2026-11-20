package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.UserCouponModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCouponModel> {

    /**
     * 查询用户优惠券列表（关联优惠券信息）
     */
@Select("SELECT uc.*, " +
            "c.id AS \"coupon.id\", c.name AS \"coupon.name\", c.description AS \"coupon.description\", " +
            "c.type AS \"coupon.type\", c.value AS \"coupon.value\", c.discount AS \"coupon.discount\", " +
            "c.min_amount AS \"coupon.minAmount\", c.max_discount AS \"coupon.maxDiscount\", " +
            "c.applicable_scope AS \"coupon.applicableScope\", c.applicable_ids AS \"coupon.applicableIds\", " +
            "c.total_quantity AS \"coupon.totalQuantity\", c.used_quantity AS \"coupon.usedQuantity\", " +
            "c.remaining_quantity AS \"coupon.remainingQuantity\", c.user_limit AS \"coupon.userLimit\", " +
            "c.start_time AS \"coupon.startTime\", c.end_time AS \"coupon.endTime\", " +
            "c.status AS \"coupon.status\", c.sort_order AS \"coupon.sortOrder\" " +
            "FROM user_coupon uc LEFT JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.deleted = 0 AND c.deleted = 0 " +
            "ORDER BY uc.created_at DESC")
    List<UserCouponModel> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户可用优惠券（未使用且未过期）
     */
@Select("SELECT uc.*, " +
            "c.id AS \"coupon.id\", c.name AS \"coupon.name\", c.description AS \"coupon.description\", " +
            "c.type AS \"coupon.type\", c.value AS \"coupon.value\", c.discount AS \"coupon.discount\", " +
            "c.min_amount AS \"coupon.minAmount\", c.max_discount AS \"coupon.maxDiscount\", " +
            "c.applicable_scope AS \"coupon.applicableScope\", c.applicable_ids AS \"coupon.applicableIds\", " +
            "c.total_quantity AS \"coupon.totalQuantity\", c.used_quantity AS \"coupon.usedQuantity\", " +
            "c.remaining_quantity AS \"coupon.remainingQuantity\", c.user_limit AS \"coupon.userLimit\", " +
            "c.start_time AS \"coupon.startTime\", c.end_time AS \"coupon.endTime\", " +
            "c.status AS \"coupon.status\", c.sort_order AS \"coupon.sortOrder\" " +
            "FROM user_coupon uc LEFT JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = 'UNUSED' AND " +
            "uc.start_time <= NOW() AND uc.end_time >= NOW() AND " +
            "uc.deleted = 0 AND c.deleted = 0 " +
            "ORDER BY c.sort_order ASC, uc.created_at DESC")
    List<UserCouponModel> selectAvailableByUserId(@Param("userId") String userId);

    /**
     * 统计用户优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") String userId);

    /**
     * 统计用户可用优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND status = 'UNUSED' " +
            "AND start_time <= NOW() AND end_time >= NOW() AND deleted = 0")
    int countAvailableByUserId(@Param("userId") String userId);

    /**
     * 统计用户已使用优惠券数量
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND status = 'USED' AND deleted = 0")
    int countUsedByUserId(@Param("userId") String userId);

    /**
     * 检查用户是否已领取该优惠券
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId} AND deleted = 0")
    int countUserCoupon(@Param("userId") String userId, @Param("couponId") String couponId);

/**
     * 使用优惠券
     */
    @Update("UPDATE user_coupon SET status = 'USED', used_time = NOW(), order_no = #{orderNo}, " +
            "updated_at = NOW() WHERE id = #{userCouponId} AND status = 'UNUSED'")
    int useCoupon(@Param("userCouponId") String userCouponId, @Param("orderNo") String orderNo);

    /**
     * 返还优惠券（订单取消/换券时恢复为未使用）
     */
    @Update("UPDATE user_coupon SET status = 'UNUSED', used_time = NULL, order_no = NULL, " +
            "updated_at = NOW() WHERE id = #{userCouponId} AND status = 'USED'")
    int restoreCoupon(@Param("userCouponId") String userCouponId);
}
