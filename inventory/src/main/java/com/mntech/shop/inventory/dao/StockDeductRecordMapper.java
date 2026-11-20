package com.mntech.shop.inventory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.inventory.model.StockDeductRecord;
import com.mntech.shop.pojo.model.ProductModel;
import com.mntech.shop.pojo.model.SkuModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockDeductRecordMapper  extends BaseMapper<StockDeductRecord> {

    @Select("SELECT * FROM stock_deduct_record WHERE biz_no = #{bizNo} AND status = 1")
     StockDeductRecord  selectByBizNo(@Param("bizNo") String bizNo);
}
