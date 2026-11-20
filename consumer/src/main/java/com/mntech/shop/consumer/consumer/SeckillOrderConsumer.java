package com.mntech.shop.consumer.consumer;

import com.alibaba.fastjson.JSON;
import com.mntech.shop.consumer.constant.RocketMQConstants;
import com.mntech.shop.consumer.dto.SeckillOrderMessage;
import com.mntech.shop.consumer.enums.OrderStatus;
import com.mntech.shop.consumer.model.SeckillOrderModel;
import com.mntech.shop.consumer.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//@RocketMQMessageListener(
//        topic = RocketMQConstants.SECKILL_ORDER_TOPIC,
//        consumerGroup = "${rocketmq.consumer.group}",
//        selectorExpression = RocketMQConstants.ORDER_PAY_TAG
//)
public class SeckillOrderConsumer implements RocketMQListener<String> {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Override
    public void onMessage(String messageJson) {
        log.info("收到秒杀订单超时检查消息: {}", messageJson);

        try {
            // 解析消息
            SeckillOrderMessage message = JSON.parseObject(messageJson, SeckillOrderMessage.class);

            // 只处理 CHECK 类型的消息
            if (!"CHECK".equals(message.getMessageType())) {
                log.info("忽略非检查类型消息: {}", message.getMessageType());
                return;
            }

            // 查询订单状态
            SeckillOrderModel order = seckillOrderService.getById(message.getOrderId());

            if (order == null) {
                log.warn("订单不存在: orderId={}", message.getOrderId());
                return;
            }

            // 如果订单状态是 PENDING（待支付），执行取消
            if (OrderStatus.PENDING.name().equals(order.getStatus())) {
                log.info("订单超时未支付，开始取消: orderId={}, userId={}",
                        order.getId(), order.getUserId());
                seckillOrderService.cancelTimeoutOrder(order);
            } else {
                log.info("订单已处理，无需取消: orderId={}, status={}",
                        order.getId(), order.getStatus());
            }

        } catch (Exception e) {
            log.error("处理秒杀订单消息失败", e);
            // 抛出异常让 RocketMQ 重试
            throw new RuntimeException("处理消息失败", e);
        }
    }
}