package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.UserLotteryQuota;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserLotteryQuotaMapper extends BaseMapper<UserLotteryQuota> {

    /**
     * 查询用户剩余抽奖次数
     */
    @Select("SELECT remaining_quota FROM user_lottery_quota WHERE user_id = #{userId} AND deleted = 0")
    Integer selectRemainingQuota(@Param("userId") String userId);

    /**
     * 扣减用户抽奖次数
     */
    @Update("UPDATE user_lottery_quota SET used_quota = used_quota + 1, " +
            "remaining_quota = remaining_quota - 1, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND remaining_quota > 0 AND deleted = 0")
    int deductQuota(@Param("userId") String userId);

    /**
     * 初始化用户抽奖配额（新用户）
     */
    @Insert("INSERT INTO user_lottery_quota (id, user_id, total_quota, used_quota, remaining_quota, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, #{totalQuota}, 0, #{totalQuota}, NOW(), NOW())")
    int initQuota(@Param("id") String id, @Param("userId") String userId, @Param("totalQuota") int totalQuota);

    /**
     * 增加用户抽奖次数
     */
    @Update("UPDATE user_lottery_quota SET " +
            "total_quota = total_quota + #{amount}, " +
            "remaining_quota = remaining_quota + #{amount}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int addQuota(@Param("userId") String userId, @Param("amount") int amount);


    /**
     * 根据用户ID查询配额
     */
    @Select("SELECT * FROM user_lottery_quota WHERE user_id = #{userId} AND deleted = 0")
    UserLotteryQuota selectByUserId(@Param("userId") String userId);


}
