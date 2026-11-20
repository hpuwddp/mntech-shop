package com.mntech.shop.common.Constants;


public class RocketMQConstants {
    // Topic
    public static final String SECKILL_ORDER_TOPIC = "seckill_order_topic";
    public static final String ORDER_TOPIC = "order_topic";

    // Tag
    public static final String ORDER_CREATE_TAG = "order_create";
    public static final String ORDER_PAY_TAG = "order_pay";
    public static final String ORDER_CANCEL_TAG = "order_cancel";
    public static final String ORDER_PENDING_TAG = "order_pengding";//待支付
    public static final String ORDER_DecStock_TAG = "order_decstock";//待扣库存
    // 延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
    // 20分钟对应延迟级别 14
    public static final int DELAY_LEVEL_20_MIN = 14;

    // 超时时间（毫秒）
    public static final long ORDER_TIMEOUT_MS = 20 * 60 * 1000;
}
