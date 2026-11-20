package com.mntech.shop.order.util;


import com.mntech.shop.common.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@Slf4j
public class RedisLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 50;

    /**
     * 执行带锁的业务逻辑（无重试）
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
                                 Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("executeWithLock 系统繁忙，请稍后重试");
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统异常，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 执行带锁的业务逻辑（带重试）
     */
    public <T> T executeWithLockAndRetry(String lockKey, long waitTime, long leaseTime,
                                         int maxRetries, long retryDelayMs,
                                         Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        int retryCount = 0;

        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("executeWithLockAndRetry 系统繁忙，请稍后重试");
            }

            while (retryCount < maxRetries) {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= maxRetries) {
                        log.error("业务执行失败，已达最大重试次数: {}", maxRetries, e);
                        throw e;
                    }
                    log.warn("业务执行失败，第{}次重试: {}", retryCount, e.getMessage());
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("操作被中断");
                    }
                }
            }
            throw new BusinessException("操作失败");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统异常，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 执行带锁的业务逻辑（使用默认重试参数）
     */
    public <T> T executeWithLockAndRetry(String lockKey, long waitTime, long leaseTime,
                                         Supplier<T> supplier) {
        return executeWithLockAndRetry(lockKey, waitTime, leaseTime,
                DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY_MS, supplier);
    }
}