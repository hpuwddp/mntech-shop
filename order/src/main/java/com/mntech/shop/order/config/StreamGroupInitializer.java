package com.mntech.shop.order.config;

import com.mntech.shop.order.dto.StreamKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class StreamGroupInitializer implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // MKSTREAM: 流不存在时自动创建；已存在则抛 BUSYGROUP，忽略即可
            redisTemplate.opsForStream().createGroup(
                    StreamKeys.INVENTORY_STREAM,
                    ReadOffset.from("0"),          // 从最早消息开始消费（新组用 "0"；只要新消息用 "$"）
                    StreamKeys.INVENTORY_GROUP
            );
            log.info("Stream 消费组创建成功: {}", StreamKeys.INVENTORY_GROUP);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.info("消费组已存在，跳过创建");
            } else {
                log.error("创建消费组失败", e);
            }
        }
    }
}