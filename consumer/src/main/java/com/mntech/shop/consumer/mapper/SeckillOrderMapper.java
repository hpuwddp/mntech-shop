package com.mntech.shop.consumer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.consumer.model.SeckillOrderModel;
import com.mntech.shop.consumer.model.SeckillProductModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrderModel> {

    @Update("UPDATE seckill_order SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);
}
