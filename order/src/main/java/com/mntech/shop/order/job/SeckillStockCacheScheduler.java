package com.mntech.shop.order.job;

import cn.hutool.json.JSONUtil;
import com.mntech.shop.order.dao.SeckillActivityMapper;
import com.mntech.shop.order.dao.SeckillProductMapper;
import com.mntech.shop.order.model.SeckillActivityModel;
import com.mntech.shop.order.model.SeckillProductModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SeckillStockCacheScheduler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    // ==================== Redis Key 前缀 ====================
    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    private static final String SECKILL_ACTIVITY_PREFIX = "seckill:activity:";
    private static final String SECKILL_PRODUCT_PREFIX = "seckill:product:";
    private static final String SECKILL_ACTIVITY_PRODUCTS_PREFIX = "seckill:activity:products:";

    // ==================== 缓存过期时间 ====================
    private static final long CACHE_EXPIRE_HOURS = 24;  // 活动信息缓存24小时
    private static final long STOCK_EXPIRE_DAYS = 7;    // 库存缓存7天
    /**
     * 每5分钟同步一次秒杀库存
     */
    @Scheduled(fixedDelay = 300000)
    public void syncSeckillStockCache() {
        try {
            // 1. 同步活动信息
            syncActivityCache();

            // 2. 同步商品信息
            syncProductCache();

            // 3. 同步库存
            syncStockCache();

            log.info("✅ 秒杀缓存同步完成");
        } catch (Exception e) {
            log.error("❌ 秒杀缓存同步失败", e);
        }
    }
    // ==================== 1. 同步活动信息 ====================
    private void syncActivityCache() {
        log.info("  📋 同步活动信息...");

        // 获取所有进行中的活动
        List<SeckillActivityModel> activities = seckillActivityMapper.selectOngoingActivities();
        if (activities == null || activities.isEmpty()) {
            log.info("    没有活跃的活动");
            return;
        }

        int syncCount = 0;
        for (SeckillActivityModel activity : activities) {
            String key = SECKILL_ACTIVITY_PREFIX + activity.getId();
            String json = JSONUtil.toJsonStr(activity);
            String cached = redisTemplate.opsForValue().get(key);

            if (!json.equals(cached)) {
                redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                syncCount++;
                log.info("    ✅ 同步活动: id={}, name={}", activity.getId(), activity.getName());
            }
        }
        log.info("    📋 活动信息同步完成，共 {} 条", syncCount);
    }

    // ==================== 2. 同步商品信息 ====================
    private void syncProductCache() {
        log.info("  📦 同步商品信息...");

        List<SeckillProductModel> products = seckillProductMapper.selectAllActive();
        if (products == null || products.isEmpty()) {
            log.info("    没有活跃的商品");
            return;
        }

        int syncCount = 0;
        for (SeckillProductModel product : products) {
            String key = SECKILL_PRODUCT_PREFIX + product.getId();
            String json = JSONUtil.toJsonStr(product);
            String cached = redisTemplate.opsForValue().get(key);

            if (!json.equals(cached)) {
                redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                syncCount++;
                log.info("    ✅ 同步商品: id={}, name={}", product.getId(), product.getProductName());
            }
        }
        log.info("    📦 商品信息同步完成，共 {} 条", syncCount);
    }

    // ==================== 3. 同步库存 ====================
    private void syncStockCache() {
        log.info("  📊 同步库存...");

        List<SeckillProductModel> products = seckillProductMapper.selectAllActive();
        if (products == null || products.isEmpty()) {
            return;
        }

        int syncCount = 0;
        for (SeckillProductModel product : products) {
            String key = SECKILL_STOCK_PREFIX + product.getId();
            String dbStock = String.valueOf(product.getRemainingStock() != null ? product.getRemainingStock() : 0);
            String redisStock = redisTemplate.opsForValue().get(key);

            // ✅ 如果 Redis 没有库存，或者 DB 库存小于 Redis（DB 减少了，同步）
            if (redisStock == null || !dbStock.equals(redisStock)) {
                redisTemplate.opsForValue().set(key, dbStock, STOCK_EXPIRE_DAYS, TimeUnit.DAYS);
                syncCount++;
                log.info("    ✅ 同步库存: productId={}, db={}, redis={}",
                        product.getId(), dbStock, redisStock);
            }
        }
        log.info("    📊 库存同步完成，共 {} 条", syncCount);
    }

    /**
     * 活动开始时预热库存（可手动调用）
     */
    public void warmUpStock(String activityId) {
        List<SeckillProductModel> products = seckillProductMapper.selectByActivityIdWithProduct(activityId);
        if (products == null || products.isEmpty()) {
            return;
        }

        for (SeckillProductModel product : products) {
            String key = SECKILL_STOCK_PREFIX + product.getId();
            String dbStock = String.valueOf(product.getRemainingStock() != null ? product.getRemainingStock() : 0);
            redisTemplate.opsForValue().set(key, dbStock);
            log.info("🔥 预热秒杀库存: productId={}, stock={}", product.getId(), dbStock);
        }
    }

    /**
     * 手动同步单个商品库存
     */
    public void syncSingleStock(String seckillProductId) {
        SeckillProductModel product = seckillProductMapper.selectById(seckillProductId);
        if (product == null) {
            log.warn("秒杀商品不存在: {}", seckillProductId);
            return;
        }

        String key = SECKILL_STOCK_PREFIX + seckillProductId;
        String dbStock = String.valueOf(product.getRemainingStock() != null ? product.getRemainingStock() : 0);
        redisTemplate.opsForValue().set(key, dbStock);
        log.info("🔄 手动同步秒杀库存: productId={}, stock={}", seckillProductId, dbStock);
    }
}

