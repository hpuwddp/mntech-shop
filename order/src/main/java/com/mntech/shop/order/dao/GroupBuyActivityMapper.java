package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.GroupBuyActivityModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupBuyActivityMapper extends BaseMapper<GroupBuyActivityModel> {

    /**
     * 获取进行中的团购活动（已审核通过且处于时间窗内）
     */
    @Select("SELECT * FROM group_buy_activity " +
            "WHERE status IN ('APPROVED', 'ONGOING') " +
            "AND deleted = 0 " +
            "AND start_time <= NOW() " +
            "AND end_time >= NOW() " +
            "ORDER BY created_at ASC")
    List<GroupBuyActivityModel> selectOngoingActivities();

    /**
     * 统计活动商品数
     */
    @Select("SELECT COUNT(*) FROM group_buy_product WHERE activity_id = #{activityId} AND deleted = 0")
    int countProducts(@Param("activityId") String activityId);

    /**
     * 统计活动总库存
     */
    @Select("SELECT COALESCE(SUM(stock), 0) FROM group_buy_product WHERE activity_id = #{activityId} AND deleted = 0")
    int sumTotalStock(@Param("activityId") String activityId);

    /**
     * 统计活动剩余库存
     */
    @Select("SELECT COALESCE(SUM(remaining_stock), 0) FROM group_buy_product WHERE activity_id = #{activityId} AND deleted = 0")
    int sumRemainingStock(@Param("activityId") String activityId);
}