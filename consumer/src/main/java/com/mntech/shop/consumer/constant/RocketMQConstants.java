package com.mntech.shop.consumer.constant;

public class RocketMQConstants {
    // Topic
    public static final String SECKILL_ORDER_TOPIC = "seckill_order_topic";
    public static final String ORDER_TOPIC = "order_topic";

    // Tag
    public static final String ORDER_CHECK_TAG = "order_check";
    public static final String ORDER_PAY_TAG = "order_pay";
    public static final String ORDER_CANCEL_TAG = "order_cancel";
    public static final String ORDER_Pending_TAG = "order_pengding";//待支付
    public static final String ORDER_DecStock_TAG = "order_decstock";//待扣库存
}
