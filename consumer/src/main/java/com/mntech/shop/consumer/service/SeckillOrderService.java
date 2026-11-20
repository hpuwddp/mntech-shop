package com.mntech.shop.consumer.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mntech.shop.consumer.enums.OrderStatus;
import com.mntech.shop.consumer.mapper.SeckillOrderMapper;
import com.mntech.shop.consumer.mapper.SeckillProductMapper;
import com.mntech.shop.consumer.model.SeckillOrderModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SeckillOrderService extends ServiceImpl<SeckillOrderMapper, SeckillOrderModel> {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillProductMapper seckillProductMapper;


    /**
     * 取消超时订单
     */
    @Transactional
    public void cancelTimeoutOrder(SeckillOrderModel order) {
        String orderId = order.getId();
        String seckillProductId = order.getSeckillProductId();
        String userId = order.getUserId();
        Integer quantity = order.getQuantity();

        // 1. 更新订单状态为已取消
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        seckillOrderMapper.updateById(order);



        // 4. 恢复数据库库存
        seckillProductMapper.increaseStock(seckillProductId, quantity);
        log.info("恢复数据库库存: productId={}, quantity={}", seckillProductId, quantity);

        log.info("超时订单已取消: orderId={}, userId={}, productId={}, quantity={}",
                orderId, userId, seckillProductId, quantity);
    }
}