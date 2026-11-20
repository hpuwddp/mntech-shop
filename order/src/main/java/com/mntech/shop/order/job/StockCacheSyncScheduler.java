package com.mntech.shop.order.job;

import com.mntech.shop.order.dao.SkuMapper;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StockCacheSyncScheduler {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SkuMapper skuMapper;

    private static final String STOCK_PREFIX = "stock:sku:";

    // 每5分钟同步一次
    @Scheduled(fixedDelay = 300000)
    public void syncStockCache() {
        log.info("🔄 开始同步库存缓存...");

        List<SkuModel> skuList = skuMapper.selectList(null);
        for (SkuModel sku : skuList) {
            String key = STOCK_PREFIX + sku.getId();
            String dbStock = String.valueOf(sku.getAvailableStock());
            String redisStock = redisTemplate.opsForValue().get(key);

            if (!dbStock.equals(redisStock)) {
                redisTemplate.opsForValue().set(key, dbStock);
                log.info("  🔄 同步库存: {} = {} (原Redis值: {})", key, dbStock, redisStock);
            }
        }

        log.info("✅ 库存缓存同步完成");
    }
}
