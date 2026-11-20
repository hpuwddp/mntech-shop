package com.mntech.shop.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 构建 Redis 地址
        String address = "redis://" + redisHost + ":" + redisPort;

        // ✅ 只有当密码不为空且不为null时才设置密码
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(redisPassword)
                    .setDatabase(redisDatabase)
                    .setConnectionPoolSize(10)
                    .setConnectionMinimumIdleSize(5);
        } else {
            // ✅ 没有密码时，不调用 setPassword
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(redisDatabase)
                    .setConnectionPoolSize(10)
                    .setConnectionMinimumIdleSize(5);
        }

        return Redisson.create(config);
    }
}
