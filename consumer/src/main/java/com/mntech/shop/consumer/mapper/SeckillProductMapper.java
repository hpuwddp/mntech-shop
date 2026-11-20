package com.mntech.shop.consumer.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.consumer.model.SeckillProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProductModel> {

    @Update("UPDATE seckill_product SET remaining_stock = remaining_stock + #{quantity} WHERE id = #{id}")
    int increaseStock(@Param("id") String id, @Param("quantity") Integer quantity);
}