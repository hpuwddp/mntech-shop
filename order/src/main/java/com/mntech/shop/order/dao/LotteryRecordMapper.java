package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.LotteryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LotteryRecordMapper extends BaseMapper<LotteryRecord> {
    /**
     * 查询用户的抽奖记录（最近10条）
     */
    @Select("SELECT * FROM lottery_record WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC LIMIT 10")
    List<LotteryRecord> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户中奖记录
     */
    @Select("SELECT * FROM lottery_record WHERE user_id = #{userId} AND is_win = TRUE AND deleted = 0 ORDER BY created_at DESC")
    List<LotteryRecord> selectWinRecordsByUserId(@Param("userId") String userId);

    /**
     * 查询用户中奖记录（分页）
     */
    @Select("SELECT * FROM lottery_record WHERE user_id = #{userId} AND is_win = TRUE AND deleted = 0 ORDER BY created_at DESC LIMIT #{offset}, #{pageSize}")
    List<LotteryRecord> selectWinRecordsByUserIdPage(@Param("userId") String userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 统计用户中奖数量
     */
    @Select("SELECT COUNT(*) FROM lottery_record WHERE user_id = #{userId} AND is_win = TRUE AND deleted = 0")
    int countWinRecords(@Param("userId") String userId);

}
