package com.mntech.shop.order.job;

import com.mntech.shop.order.dao.SeckillProductMapper;
import com.mntech.shop.order.dto.StreamKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InventoryStreamRetryTask {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SeckillProductMapper seckillProductMapper;

    private static final int MAX_RETRY = 3;
    private static final Duration MIN_IDLE = Duration.ofMinutes(1); // 超过1分钟未ack视为超时

    @Scheduled(fixedDelay = 30000)
    public void retryPending() {
        // 1. 查看 PENDING 概要（每个消费者的 pending 数、最小id等）
        PendingMessagesSummary summary = stringRedisTemplate.opsForStream()
                .pending(StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP);
        if (summary == null || summary.getTotalPendingMessages() == 0) {
            return;
        }
        log.info("PENDING 总数: {}", summary.getTotalPendingMessages());

        // 2. 拉取超时未 ack 的消息详情
        PendingMessages pending = stringRedisTemplate.opsForStream().pending(
                StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP,
                Range.unbounded(), 100L);
        if (pending == null) return;

        for (PendingMessage pm : pending) {
            RecordId id = pm.getId();
            if (pm.getElapsedTimeSinceLastDelivery().compareTo(MIN_IDLE) < 0) {
                continue; // 还没超时，跳过
            }
            if (pm.getTotalDeliveryCount() > MAX_RETRY) {
                // 超过最大重试，进死信
                moveToDlq(id);
                continue;
            }
            // 3. XCLAIM 接管并重试
            try {
                // claim 到当前消费者
                List<MapRecord<String, Object, Object>> claimed = stringRedisTemplate.opsForStream().claim(
                        StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP,
                        "retry-consumer", MIN_IDLE, id);
                if (claimed == null || claimed.isEmpty()) continue;

                for (MapRecord<String, Object, Object> rec : claimed) {
                    Map<Object, Object> v = rec.getValue();
                    String productId = (String) v.get("productId");
                    int num = Integer.parseInt((String) v.get("num"));
                    int affected = seckillProductMapper.decreaseStockRedis(productId, num);
                    if (affected > 0) {
                        stringRedisTemplate.opsForStream().acknowledge(
                                StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP, rec.getId());
                        stringRedisTemplate.opsForStream().delete(StreamKeys.INVENTORY_STREAM, rec.getId());
                        log.info("重试成功: id={}", rec.getId());
                    }
                }
            } catch (Exception e) {
                log.error("重试失败: id={}", id, e);
            }
        }
    }

    private void moveToDlq(RecordId id) {
        try {
            StreamOperations<String, String, String> streamOps = stringRedisTemplate.opsForStream();
            List<MapRecord<String, String, String>> recs = streamOps.range(
                    StreamKeys.INVENTORY_STREAM,
                    Range.closed(id.getValue(), id.getValue()));

            if (recs != null && !recs.isEmpty()) {
                MapRecord<String, String, String> rec = recs.get(0);
                Map<String, String> dlqMsg = new HashMap<>(rec.getValue()); // 已经是 Map<String,String>
                dlqMsg.put("originId", id.getValue());
                dlqMsg.put("reason", "max-retry-exceeded");

                StringRecord dlqRecord = StreamRecords.string(dlqMsg)
                        .withStreamKey(StreamKeys.INVENTORY_DLQ);
                stringRedisTemplate.opsForStream().add(dlqRecord);
            }

            stringRedisTemplate.opsForStream().acknowledge(
                    StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP, id);
            stringRedisTemplate.opsForStream().delete(StreamKeys.INVENTORY_STREAM, id);
            log.warn("任务进入死信: id={}", id);
        } catch (Exception e) {
            log.error("移入死信失败: id={}", id, e);
        }
    }
}
