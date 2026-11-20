package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.GroupBuyOrderModel;
import com.mntech.shop.order.model.GroupBuyTeamModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GroupBuyOrderMapper extends BaseMapper<GroupBuyOrderModel> {

    /**
     * 获取用户团购订单列表（联查商品与团信息）
     */
    @Select("SELECT go.*, p.name AS product_name, p.main_image AS product_image, gt.group_no AS group_no " +
            "FROM group_buy_order go " +
            "LEFT JOIN group_buy_product gp ON go.group_buy_product_id = gp.id " +
            "LEFT JOIN product p ON gp.product_id = p.id " +
            "LEFT JOIN group_buy_team gt ON go.team_id = gt.id " +
            "WHERE go.user_id = #{userId} AND go.deleted = 0 " +
            "ORDER BY go.created_at DESC")
    List<GroupBuyOrderModel> selectByUserId(@Param("userId") String userId);

    /**
     * 获取用户团购订单列表（按状态）
     */
    @Select("SELECT go.*, p.name AS product_name, p.main_image AS product_image, gt.group_no AS group_no " +
            "FROM group_buy_order go " +
            "LEFT JOIN group_buy_product gp ON go.group_buy_product_id = gp.id " +
            "LEFT JOIN product p ON gp.product_id = p.id " +
            "LEFT JOIN group_buy_team gt ON go.team_id = gt.id " +
            "WHERE go.user_id = #{userId} AND go.deleted = 0 AND go.status = #{status} " +
            "ORDER BY go.created_at DESC")
    List<GroupBuyOrderModel> selectByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

    /**
     * 用户参与的团（去重，供“我的团购”使用）
     */
    @Select("SELECT DISTINCT gt.* FROM group_buy_team gt " +
            "INNER JOIN group_buy_order go ON go.team_id = gt.id " +
            "WHERE go.user_id = #{userId} AND gt.deleted = 0 AND go.deleted = 0 " +
            "ORDER BY gt.created_at DESC")
    List<GroupBuyTeamModel> selectTeamsByUserId(@Param("userId") String userId);

    /**
     * 获取团内订单列表（联查用户昵称头像）
     */
    @Select("SELECT go.*, u.nickname AS buyer_nickname, u.avatar AS buyer_avatar " +
            "FROM group_buy_order go " +
            "LEFT JOIN users u ON go.user_id = u.id " +
            "WHERE go.team_id = #{teamId} AND go.deleted = 0 " +
            "ORDER BY go.created_at ASC")
    List<GroupBuyOrderModel> selectByTeamId(@Param("teamId") String teamId);

    /**
     * 获取用户对某团购商品的累计购买数量（限购校验，含待支付与已支付）
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM group_buy_order " +
            "WHERE user_id = #{userId} " +
            "AND group_buy_product_id = #{groupBuyProductId} " +
            "AND status IN ('PENDING', 'PAID') " +
            "AND deleted = 0")
    Integer countUserBuy(@Param("userId") String userId, @Param("groupBuyProductId") String groupBuyProductId);

    /**
     * 检查用户是否已参与某团（防止重复参团）
     */
    @Select("SELECT COUNT(*) FROM group_buy_order " +
            "WHERE user_id = #{userId} AND team_id = #{teamId} " +
            "AND status IN ('PENDING', 'PAID') AND deleted = 0")
    int countUserBuyInTeam(@Param("userId") String userId, @Param("teamId") String teamId);

    /**
     * 统计某团内除指定用户（团长）外仍有进行中订单的人数（PENDING/PAID）
     * 用于团长取消整团时判断"是否已有人跟团"
     */
    @Select("SELECT COUNT(*) FROM group_buy_order " +
            "WHERE team_id = #{teamId} AND user_id <> #{userId} " +
            "AND status IN ('PENDING', 'PAID') AND deleted = 0")
    int countOtherActiveByTeam(@Param("teamId") String teamId, @Param("userId") String userId);

    /**
     * 统计用户在某活动下进行中的团购订单数（PENDING/PAID 且团 ONGOING）
     * 用于防止同一用户在同一活动下重复开团/参团（兼容同活动多商品、per_user_limit>1 场景）
     */
    @Select("SELECT COUNT(*) FROM group_buy_order go " +
            "INNER JOIN group_buy_team gt ON go.team_id = gt.id " +
            "WHERE go.user_id = #{userId} " +
            "AND go.activity_id = #{activityId} " +
            "AND go.status IN ('PENDING', 'PAID') " +
            "AND gt.status = 'ONGOING' AND go.deleted = 0 AND gt.deleted = 0")
    int countActiveByUserAndActivity(@Param("userId") String userId, @Param("activityId") String activityId);

    /**
     * 模拟支付成功
     */
    @Update("UPDATE group_buy_order SET status = 'PAID', pay_time = #{payTime}, transaction_id = #{transactionId}, updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'PENDING'")
    int updatePaySuccess(@Param("id") String id, @Param("transactionId") String transactionId, @Param("payTime") LocalDateTime payTime);

    /**
     * 取消订单（待支付）
     */
    @Update("UPDATE group_buy_order SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'PENDING'")
    int markCancelled(@Param("id") String id);

    /**
     * 已支付订单退款（记录退款金额=实付金额，退款时间）
     */
    @Update("UPDATE group_buy_order SET status = 'REFUNDED', " +
            "refund_amount = COALESCE(pay_amount, total_amount), refund_time = NOW(), updated_at = NOW() " +
            "WHERE id = #{id} AND status = 'PAID'")
    int updateRefunded(@Param("id") String id);

    /**
     * 批量取消团的待支付订单（拼团失败时）
     */
    @Update("UPDATE group_buy_order SET status = 'CANCELLED', updated_at = NOW() " +
            "WHERE team_id = #{teamId} AND status = 'PENDING'")
    int cancelPendingByTeam(@Param("teamId") String teamId);

    /**
     * 批量退款团的已支付订单（拼团失败时，记录退款金额=实付金额）
     */
    @Update("UPDATE group_buy_order SET status = 'REFUNDED', " +
            "refund_amount = COALESCE(pay_amount, total_amount), refund_time = NOW(), updated_at = NOW() " +
            "WHERE team_id = #{teamId} AND status = 'PAID'")
    int refundPaidByTeam(@Param("teamId") String teamId);

    /**
     * 获取支付超时的待支付订单（15分钟，PostgreSQL 语法）
     */
    @Select("SELECT * FROM group_buy_order " +
            "WHERE status = 'PENDING' AND deleted = 0 " +
            "AND created_at < NOW() - INTERVAL '15 minutes'")
    List<GroupBuyOrderModel> selectPendingTimeoutOrders();
}