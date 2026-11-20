package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.PointsAccountModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PointsAccountMapper extends BaseMapper<PointsAccountModel> {

    /**
     * 原子累加积分（已存在账户时使用，避免并发覆盖）
     */
    @Update("UPDATE points_account SET total_points = total_points + #{points}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int increasePoints(@Param("userId") String userId, @Param("points") int points);
}