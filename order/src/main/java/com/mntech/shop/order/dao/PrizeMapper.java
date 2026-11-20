package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.Prize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PrizeMapper extends BaseMapper<Prize> {
    /**
     * 查询所有可用的奖品（按概率排序）
     */
    @Select("SELECT * FROM prize WHERE status = 'ACTIVE' AND remaining_quantity > 0 AND deleted = 0 ORDER BY probability DESC")
    List<Prize> selectAvailablePrizes();

    /**
     * 扣减奖品库存
     */
    @Update("UPDATE prize SET remaining_quantity = remaining_quantity - 1, updated_at = NOW() " +
            "WHERE id = #{prizeId} AND remaining_quantity > 0 AND deleted = 0")
    int deductPrizeStock(String prizeId);
}
