package com.mntech.shop.order.job;

import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.SeckillProductMapper;
import com.mntech.shop.order.dto.StreamKeys;
import com.mntech.shop.order.model.SeckillProductModel;
import com.mntech.shop.order.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class InventoryStreamConsumer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SeckillProductMapper seckillProductMapper;
    @Resource
    private ISeckillService iSeckillService;
    private static final String LOCK_PREFIX  = "lock:seckill:stock:";
    private static final String IDEM_PREFIX  = "seckill:idem:stock:";   // 幂等标记
    private static final long   IDEM_TTL_H   = 24;                      // 小时
    private static final long   LOCK_TTL_SECONDS = 10;
    private static final String CONSUMER_NAME =
            "consumer-" + UUID.randomUUID().toString().substring(0, 8);

    /**
     * 每 1 秒拉一批，处理完再拉下一批
     * 多实例部署时，每个实例的 CONSUMER_NAME 不同，组内自动负载均衡
     */
    @Scheduled(fixedDelay = 1000)
    public void consume() {
        int maxRounds = 10;               // 每轮最多10批，防止长时间占用
        for (int i = 0; i < maxRounds; i++) {
            StreamOperations<String, String, String> streamOps = stringRedisTemplate.opsForStream();
            List<MapRecord<String, String, String>> records = streamOps.read(
                    Consumer.from(StreamKeys.INVENTORY_GROUP, CONSUMER_NAME),
                    StreamReadOptions.empty().count(10),
                    StreamOffset.create(StreamKeys.INVENTORY_STREAM, ReadOffset.lastConsumed())
            );
            if (records == null || records.isEmpty()) break;
            records.forEach(this::handleRecord);
        }
    }

    private void handleRecord(MapRecord<String, String, String> record) {
        RecordId id = record.getId();
        Map<String, String> v = record.getValue();
        String productId = v.get("productId");
        String userId    = v.get("userId");
        String activityId    = v.get("activityId");
        int    num       = Integer.parseInt(v.get("num"));

        String lockKey = LOCK_PREFIX + userId + ":" + productId;
        String idemKey = IDEM_PREFIX + userId + ":" + productId;

        // 1) 幂等标记：已处理过直接 ack 丢弃（挡重复投递）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idemKey))) {
            log.warn("幂等命中，跳过: id={}, userId={}, productId={}", id, userId, productId);
            ackAndDel(id);
            return;
        }

        // 2) 加锁：减少同一业务键的并发窗口
        String token = tryLock(lockKey);
        if (token == null) {
            log.warn("未获取到锁，跳过本轮: id={}", id);
            return;  // 不 ack，留 PENDING 重试
        }

        try {
            // 3) 双重检查：拿到锁后再查一次幂等标记
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idemKey))) {
                log.warn("幂等命中(双重检查)，跳过: id={}", id);
                ackAndDel(id);
                return;
            }

            //查询 该活动该产品的秒杀单价
            SeckillProductModel seckillProductModel= seckillProductMapper.selectSeckillProductInfoByProductId(activityId,productId);
            if (seckillProductModel!=null){
                // 4) 事务：先扣库存，再建订单（Service 内部顺序已调整）
                iSeckillService.createOrderAndDecreaseStock(userId,productId,num,activityId,seckillProductModel.getSeckillPrice());
                // 5) 成功写幂等标记
                stringRedisTemplate.opsForValue().set(idemKey, "1", IDEM_TTL_H, TimeUnit.HOURS);
            }
            ackAndDel(id);
            log.info("扣库存成功: id={}, productId={}, userId={}, num={}", id, productId, userId, num);

        } catch (DuplicateKeyException dup) {
            // 订单唯一索引冲突：已下过单（重复消息），幂等丢弃
            // 注意：先扣库存顺序下，这里库存已被扣过一次但事务回滚了，最终库存不变
            log.warn("重复订单(唯一索引)，忽略: id={}, userId={}, productId={}", id, userId, productId);
            ackAndDel(id);

        } catch (BusinessException be) {
            // 业务失败（DB库存不足）：ack 丢弃，不重试
            log.warn("业务失败，丢弃: id={}, msg={}", id, be.getMessage());
            ackAndDel(id);

        } catch (Exception e) {
            // 系统异常：不 ack，留 PENDING 重试
            log.error("处理异常，留PENDING待重试: id={}", id, e);

        } finally {
            unlock(lockKey, token);
        }
    }

    private void ackAndDel(RecordId id) {
        stringRedisTemplate.opsForStream().acknowledge(StreamKeys.INVENTORY_STREAM, StreamKeys.INVENTORY_GROUP, id);
        stringRedisTemplate.opsForStream().delete(StreamKeys.INVENTORY_STREAM, id);
    }
    /** 尝试加锁，返回 token（成功）或 null（失败） */
    private String tryLock(String key) {
        String token = UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, token, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    /** Lua 安全释放：只有 value 等于自己的 token 才删 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "  return redis.call('del', KEYS[1]) " +
                    "else return 0 end", Long.class);

    private void unlock(String key, String token) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
    }

}
