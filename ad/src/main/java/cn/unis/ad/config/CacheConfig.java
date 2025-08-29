package cn.unis.ad.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, Object> caffeineCache() {
        Cache<String, Object> cache = Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(100)
                // 最大缓存数量
                .maximumSize(500)
                // 缓存过期时间：写入缓存后，经过某个时间缓存失效
                .expireAfterWrite(7200, TimeUnit.SECONDS)
                // 缓存失效监听器
                .removalListener((key, value, cause) -> System.out.println("key:" + key + " value:" + value + " cause:" + cause))
                // 开启统计功能
                .recordStats()
                .build();
        return cache;
    }
}
