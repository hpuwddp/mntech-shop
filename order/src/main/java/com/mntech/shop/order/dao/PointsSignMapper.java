package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.PointsSignModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface PointsSignMapper extends BaseMapper<PointsSignModel> {

    /**
     * 查询某用户某天签到记录（唯一索引保证至多一条）
     */
    @Select("SELECT * FROM points_sign WHERE user_id = #{userId} AND sign_date = #{signDate} AND deleted = 0")
    PointsSignModel selectByUserIdAndDate(@Param("userId") String userId, @Param("signDate") LocalDate signDate);

    /**
     * 查询最近一次签到日期（没有则返回 null）
     */
    @Select("SELECT MAX(sign_date) FROM points_sign WHERE user_id = #{userId} AND deleted = 0")
    LocalDate selectLatestSignDate(@Param("userId") String userId);

    /**
     * 统计截止到 targetDate（含）的连续签到天数。
     * 思路：按日期正序编号，sign_date - 编号 得到分组键（连续日期分组键相同）；目标日所在分组的行数即连续天数。
     * 注意：必须使用 ASC。DESC 下日期递减而编号递增，分组键每个都不同，会导致连续天数恒为 1。
     */
    @Select("WITH tmp AS (" +
            "  SELECT sign_date, sign_date - (ROW_NUMBER() OVER (ORDER BY sign_date ASC))::int AS grp " +
            "  FROM points_sign WHERE user_id = #{userId} AND deleted = 0 AND sign_date <= #{targetDate}" +
            ") SELECT COUNT(*) FROM tmp WHERE grp = (SELECT grp FROM tmp WHERE sign_date = #{targetDate} LIMIT 1)")
    int countConsecutiveEndingAt(@Param("userId") String userId, @Param("targetDate") LocalDate targetDate);
}