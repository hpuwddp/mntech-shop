package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.CouponModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<CouponModel> {

    /**
     * 查询可领取的优惠券
     */
    @Select("SELECT * FROM coupon WHERE status = 'ACTIVE' AND " +
            "start_time <= NOW() AND end_time >= NOW() AND " +
            "(total_quantity = 0 OR remaining_quantity > 0) AND deleted = 0 " +
            "ORDER BY sort_order ASC, created_at DESC")
    List<CouponModel> selectAvailableCoupons();

    /**
     * 扣减优惠券库存
     */
    @Update("UPDATE coupon SET remaining_quantity = remaining_quantity - 1, " +
            "updated_at = NOW() WHERE id = #{couponId} AND remaining_quantity > 0")
    int deductCouponStock(@Param("couponId") String couponId);

/**
     * 增加优惠券使用数量
     */
    @Update("UPDATE coupon SET used_quantity = used_quantity + 1, " +
            "updated_at = NOW() WHERE id = #{couponId}")
    int incrementUsedCount(@Param("couponId") String couponId);

    /**
     * 减少优惠券使用数量（订单取消/换券退还时）
     */
    @Update("UPDATE coupon SET used_quantity = used_quantity - 1, " +
            "updated_at = NOW() WHERE id = #{couponId} AND used_quantity > 0")
    int decrementUsedCount(@Param("couponId") String couponId);
}