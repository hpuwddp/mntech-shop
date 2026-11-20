package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.SeckillProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProductModel> {

    /**
     * 联表查询活动商品（含商品和SKU信息）
     */
    @Select("SELECT sp.*, " +
            "p.name AS product_name, " +
            "p.main_image AS image_url, " +
            "p.selling_price AS original_price, " +
            "s.sku_name AS sku_name " +
            "FROM seckill_product sp " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "LEFT JOIN product_sku s ON sp.sku_id = s.id " +
            "WHERE sp.activity_id = #{activityId} AND sp.deleted = 0 " +
            "ORDER BY sp.sort_order ASC")
    List<SeckillProductModel> selectByActivityIdWithProduct(@Param("activityId") String activityId);


    /**
     * 查询活动商品（
     */
    @Select("SELECT sp.* FROM seckill_product sp WHERE sp.activity_id = #{activityId} AND sp.deleted = 0 and sp.product_id= #{productId} " +
            "ORDER BY sp.sort_order ASC")
    SeckillProductModel selectSeckillProductInfoByProductId(@Param("activityId") String activityId,@Param("productId") String productId);

    /**
     * 联表查询单个秒杀商品（含商品和SKU信息）
     */
    @Select("SELECT sp.*, " +
            "p.name AS product_name, " +
            "p.main_image AS image_url, " +
            "p.selling_price AS original_price, " +
            "s.sku_name AS sku_name " +
            "FROM seckill_product sp " +
            "LEFT JOIN product p ON sp.product_id = p.id " +
            "LEFT JOIN product_sku s ON sp.sku_id = s.id " +
            "WHERE sp.id = #{id} AND sp.deleted = 0")
    SeckillProductModel selectByIdWithProduct(@Param("id") String id);


    /**
     * 联表查询活动商品（含商品和SKU信息）
     * 给秒杀订单详情页使用
     */
    @Select("SELECT sp.*, " +
            "s.sku_name AS product_name,s.image_url as product_image " +
            "FROM seckill_product sp " +
            "LEFT JOIN product_sku s ON sp.sku_id = s.id " +
            "WHERE sp.activity_id = #{activityId} and sp.id = #{skuId} AND sp.deleted = 0 " +
            "ORDER BY sp.sort_order ASC")
    SeckillProductModel selectByProductIdWithProduct(@Param("skuId") String skuId, @Param("activityId") String activityId);


    /**
     * 扣减库存
     */
    @Update("UPDATE seckill_product SET " +
            "remaining_stock = remaining_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}, " +
            "version = version + 1 " +
            "WHERE product_id = #{id} AND remaining_stock >= #{quantity} AND version = #{version} ")
    int decreaseStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);


    /**
     * Redis消费者消费
     * 扣减库存
     * 由于是已经串行化 所以不需要版本号
     * 扣减库存
     */
    @Update("UPDATE seckill_product SET " +
            "remaining_stock = remaining_stock - #{quantity}, " +
            "locked_stock = locked_stock + #{quantity}," +
            "version = version + 1 " +
            "WHERE product_id = #{id} AND remaining_stock >= #{quantity}")
    int decreaseStockRedis(@Param("id") String id, @Param("quantity") Integer quantity);

    /**
     * 确认支付（真正扣减）
     */
    @Update("UPDATE seckill_product SET " +
            "locked_stock = locked_stock - #{quantity}, " +
            "sold_stock = sold_stock + #{quantity}, sales_count = sales_count + #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND locked_stock >= #{quantity}  AND version = #{version} ")
    int confirmPay(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * 增加库存
     * （支付超时/取消订单）
     */
    @Update("UPDATE seckill_product SET " +
            "remaining_stock = remaining_stock + #{quantity}, " +
            "locked_stock = locked_stock - #{quantity}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND locked_stock >= #{quantity}  AND version = #{version} ")
    int increaseStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    /**
     * ✅ 获取所有活跃的秒杀商品（进行中活动的商品）
     */
    @Select("SELECT sp.* FROM seckill_product sp " +
            "INNER JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE sa.status = 'APPROVED' " +
            "AND sa.deleted = 0 " +
            "AND sp.deleted = 0 " +
            "AND sa.start_time <= NOW() " +
            "AND sa.end_time >= NOW()")
    List<SeckillProductModel> selectAllActive();
}
