package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.GroupBuyProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GroupBuyProductMapper extends BaseMapper<GroupBuyProductModel> {

    /**
     * 联表查询活动团购商品（含商品信息）
     */
    @Select("SELECT gp.*, " +
            "p.name AS product_name, " +
            "p.main_image AS image_url " +
            "FROM group_buy_product gp " +
            "LEFT JOIN product p ON gp.product_id = p.id " +
            "WHERE gp.activity_id = #{activityId} AND gp.deleted = 0 " +
            "ORDER BY gp.sort_order ASC")
    List<GroupBuyProductModel> selectByActivityIdWithProduct(@Param("activityId") String activityId);

    /**
     * 联表查询单个团购商品（含商品信息）
     */
    @Select("SELECT gp.*, " +
            "p.name AS product_name, " +
            "p.main_image AS image_url " +
            "FROM group_buy_product gp " +
            "LEFT JOIN product p ON gp.product_id = p.id " +
            "WHERE gp.id = #{id} AND gp.deleted = 0")
    GroupBuyProductModel selectByIdWithProduct(@Param("id") String id);

    /**
     * 扣减库存（下单：可售减、锁定加）
     */
    @Update("UPDATE group_buy_product SET " +
            "remaining_stock = remaining_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND remaining_stock >= #{quantity} AND version = #{version} AND deleted = 0")
    int decreaseStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 确认支付（锁定转已售）
     */
    @Update("UPDATE group_buy_product SET " +
            "locked_stock = locked_stock - #{quantity}, " +
            "sold_stock = sold_stock + #{quantity}, " +
            "sales_count = sales_count + #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND locked_stock >= #{quantity} AND version = #{version} AND deleted = 0")
    int confirmPay(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 取消待支付订单回补库存（可售加、锁定减）
     */
    @Update("UPDATE group_buy_product SET " +
            "remaining_stock = remaining_stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND locked_stock >= #{quantity} AND version = #{version} AND deleted = 0")
    int increaseStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 已支付订单退款回补库存（可售加、已售减）
     */
    @Update("UPDATE group_buy_product SET " +
            "remaining_stock = remaining_stock + #{quantity}, " +
            "sold_stock = sold_stock - #{quantity}, " +
            "sales_count = sales_count - #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND sold_stock >= #{quantity} AND version = #{version} AND deleted = 0")
    int refundStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);
}