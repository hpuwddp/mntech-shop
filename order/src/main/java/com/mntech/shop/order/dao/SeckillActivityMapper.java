package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.SeckillActivityModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivityModel> {

    /**
     * 获取进行中的活动
     */
    @Select("SELECT * FROM seckill_activity " +
            "WHERE status = 'APPROVED' " +
            "AND deleted = 0 " +
            "AND start_time <= NOW() " +
            "AND end_time >= NOW() " +
            "ORDER BY end_time ASC, created_at ASC")
    List<SeckillActivityModel> selectOngoingActivities();



    /**
     * 更新活动状态
     */
    @Update("UPDATE seckill_activity SET status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);




    /**
     * 统计活动商品数
     */
    @Select("SELECT COUNT(*) FROM seckill_product WHERE activity_id = #{activityId} AND deleted = 0")
    int countProducts(@Param("activityId") String activityId);

    /**
     * 统计活动总库存
     */
    @Select("SELECT COALESCE(SUM(stock), 0) FROM seckill_product WHERE activity_id = #{activityId} AND deleted = 0")
    int sumTotalStock(@Param("activityId") String activityId);

    /**
     * 统计活动剩余库存
     */
    @Select("SELECT COALESCE(SUM(remaining_stock), 0) FROM seckill_product WHERE activity_id = #{activityId} AND deleted = 0")
    int sumRemainingStock(@Param("activityId") String activityId);
}