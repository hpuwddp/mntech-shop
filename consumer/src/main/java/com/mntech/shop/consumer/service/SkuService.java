package com.mntech.shop.consumer.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mntech.shop.consumer.mapper.OrderItemMapper;
import com.mntech.shop.consumer.mapper.SkuMapper;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.OrderItemModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SkuService extends ServiceImpl<SkuMapper, SkuModel> {
    @Autowired
    private SkuMapper skuMapper;

    @Transactional
   public int batchReduceStock(@Param("list") List<SkuStockUpdate> list){
       return skuMapper.batchReduceStock(list);
   }


   public List<SkuModel> batchGetSkus(List<String> skuIds){
       if (CollectionUtils.isEmpty(skuIds)) {
           return Collections.emptyList();
       }
       return skuMapper.selectByIdsOrdered(skuIds);
   }
}
