package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.GroupBuyTeamModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GroupBuyTeamMapper extends BaseMapper<GroupBuyTeamModel> {

    /**
     * 查询某团购商品的进行中团队（供详情页展示），最多20条
     */
    @Select("SELECT * FROM group_buy_team " +
            "WHERE group_buy_product_id = #{groupBuyProductId} " +
            "AND status = 'ONGOING' AND deleted = 0 " +
            "ORDER BY created_at DESC LIMIT 20")
    List<GroupBuyTeamModel> selectOngoingByProductId(@Param("groupBuyProductId") String groupBuyProductId);

    /**
     * 查询已过期未成团的团队（定时任务处理）
     */
    @Select("SELECT * FROM group_buy_team " +
            "WHERE status = 'ONGOING' AND deleted = 0 " +
            "AND end_time < NOW()")
    List<GroupBuyTeamModel> selectExpiredTeams();

    /**
     * 成员支付成功后团队人数 +1
     */
    @Update("UPDATE group_buy_team SET current_num = current_num + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'ONGOING' AND deleted = 0")
    int incrementCurrentNum(@Param("id") String id);

    /**
     * 更新团队状态
     */
    @Update("UPDATE group_buy_team SET status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);
}