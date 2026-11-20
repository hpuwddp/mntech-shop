package com.mntech.shop.consumer.consumer;

import com.alibaba.fastjson.JSON;
import com.mntech.shop.consumer.constant.RocketMQConstants;
import com.mntech.shop.consumer.enums.OrderStatus;
import com.mntech.shop.consumer.service.OrderItemService;
import com.mntech.shop.consumer.service.OrderService;
import com.mntech.shop.consumer.service.SkuService;
import com.mntech.shop.pojo.dto.OrderMessage;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.OrderItemModel;
import com.mntech.shop.pojo.model.OrderModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.ORDER_TOPIC,
        consumerGroup = "${rocketmq.consumer.group}",
        selectorExpression = RocketMQConstants.ORDER_Pending_TAG
)
public class OrderConsumer implements RocketMQListener<String> {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private SkuService skuService;
    @Override
    public void onMessage(String messageJson) {
        log.info("收到订单消息: {}", messageJson);

        try {
            // 解析消息
            OrderMessage message = JSON.parseObject(messageJson, OrderMessage.class);

            if (message == null) {
                log.warn("消息解析为空");
                return;
            }

            String orderId = message.getOrderId();
            String messageType = message.getMessageType();

            log.info("订单消息: orderId={}, messageType={}", orderId, messageType);

            // ✅ 根据消息类型处理
            if(OrderStatus.DESCSTOCK.name().equals(messageType)){
                //待扣减库存
                    handDescStock(message);

            }

            else if ("PENDING".equals(messageType)) {
                // 待支付订单超时检查
                handlePendingOrder(message);
            } else if ("PAID".equals(messageType)) {
                // 支付成功后的处理（如扣减库存）
                handlePaidOrder(message);
            } else if ("CANCELLED".equals(messageType)) {
                // 订单取消处理（如回滚库存）
                handleCancelledOrder(message);
            } else {
                log.warn("未知消息类型: {}", messageType);
            }

        } catch (Exception e) {
            log.error("处理订单消息失败", e);
            // 抛出异常让 RocketMQ 重试
            throw new RuntimeException("处理消息失败", e);
        }
    }

    /**
     * 处理待支付订单（超时检查）
     */
    private void handlePendingOrder(OrderMessage message) {
        String orderId = message.getOrderId();

        // 查询订单状态
        OrderModel order = orderService.getById(orderId);

        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return;
        }

        // 如果订单状态是 PENDING（待支付），执行取消
        if (OrderStatus.PENDING.name().equals(order.getStatus())) {
            log.info("订单超时未支付，开始取消: orderId={}, userId={}",
                    order.getId(), order.getUserId());
            orderService.cancelTimeoutOrder(order);
        } else {
            log.info("订单已处理，无需取消: orderId={}, status={}",
                    order.getId(), order.getStatus());
        }
    }

    /**
     * 处理待扣减库存订单
     */
    private void handDescStock(OrderMessage message) {
//        String orderId = message.getOrderId();
//
//        // 查询订单状态
//        OrderModel order = orderService.getById(orderId);
//
//        if (order == null) {
//            log.warn("订单不存在: orderId={}", orderId);
//            return;
//        }
//
//        //找订单明细下的所有skus
//      List<OrderItemModel> orderItemModelList= orderItemService.getOrderItemsByOrderNo(order.getOrderNo());
//        if (orderItemModelList!=null && orderItemModelList.size()>0){
//
////            List<String> skuids=new ArrayList<>();
////            skuids=orderItemModelList.stream().map(item->{
////                return item.getSkuId();
////            }).collect(Collectors.toList());
////            //再去找对应的sku的version
////            List<SkuModel> skuModels= skuService.batchGetSkus(skuids);
////
//
//
//
//            //批量更新库存
//            List<SkuStockUpdate> updateList = new ArrayList<>();
//            updateList = orderItemModelList.stream()
//                    .map(item -> {
//                        SkuStockUpdate dto = new SkuStockUpdate();
//                        dto.setSkuId(item.getSkuId());
//                        dto.setQuantity(item.getQuantity());
//                        // version 按需替换真实库存乐观锁版本号
////                        dto.setVersion(item.get);
//                        return dto;
//                    }).collect(Collectors.toList());
//
//           int rowsNum= skuService.batchReduceStock(updateList);
//           if (rowsNum==updateList.size()){
//               //更改订单status状态为待支付
//               //这个状态啊 还真的得仔细设计
//                orderService.updateOrderStatus(orderId,OrderStatus.PENDING);
//           }
//           else
//           {
//
//           }
//        }
    }
    /**
     * 处理已支付订单（扣减库存）
     */
    private void handlePaidOrder(OrderMessage message) {
        String orderId = message.getOrderId();

        OrderModel order = orderService.getById(orderId);
        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return;
        }

        // 检查订单状态是否为 PAID
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            log.warn("订单状态不是已支付: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        log.info("订单支付成功，开始扣减库存: orderId={}", orderId);

        try {
            // 扣减库存
            orderService.deductStockForOrder(order);
            log.info("库存扣减成功: orderId={}", orderId);
        } catch (Exception e) {
            log.error("库存扣减失败: orderId={}", orderId, e);
            // 这里可以记录失败，后续人工处理
        }
    }

    /**
     * 处理已取消订单（回滚库存）
     */
    private void handleCancelledOrder(OrderMessage message) {
        String orderId = message.getOrderId();

        OrderModel order = orderService.getById(orderId);
        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return;
        }

        // 检查订单状态是否为 CANCELLED
        if (!OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            log.warn("订单状态不是已取消: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        log.info("订单已取消，开始回滚库存: orderId={}", orderId);

        try {
            // 回滚库存
            orderService.rollbackStockForOrder(order);
            log.info("库存回滚成功: orderId={}", orderId);
        } catch (Exception e) {
            log.error("库存回滚失败: orderId={}", orderId, e);
        }
    }
}