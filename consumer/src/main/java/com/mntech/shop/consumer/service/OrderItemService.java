package com.mntech.shop.consumer.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mntech.shop.consumer.mapper.OrderItemMapper;
import com.mntech.shop.consumer.mapper.OrderMapper;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.OrderItemModel;
import com.mntech.shop.pojo.model.OrderModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItemModel> {
    @Autowired
    private OrderItemMapper orderItemMapper;

    public List<OrderItemModel> getOrderItemsByOrderNo(String orderNo) {
        // 1. 查询订单明细
        List<OrderItemModel> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单明细为空: orderNo={}", orderNo);
            return new ArrayList<>();
        }
        return  orderItems;
    }

}
