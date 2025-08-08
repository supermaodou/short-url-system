package com.example.shorturl.config;

import com.example.shorturl.mapper.ShortUrlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class CleanupConfig {
    @Autowired
    private ShortUrlMapper shortUrlMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void cleanupExpiredLinks() {
        // 查询过期链接
        // 注意：需要添加新的 Mapper 方法
        // 这里仅为示例，实际需实现 deleteExpired 方法
        System.out.println("Cleaning up expired links...");
        // shortUrlMapper.deleteExpired(LocalDateTime.now());
    }
}