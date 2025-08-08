package com.example.shorturl.service.impl;

import com.example.shorturl.mapper.ShortUrlMapper;
import com.example.shorturl.model.ShortUrl;
import com.example.shorturl.service.ShortUrlService;
import com.example.shorturl.util.ShortUrlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlServiceImpl implements ShortUrlService {
    @Autowired
    private ShortUrlMapper shortUrlMapper;

    @Autowired
    private ShortUrlGenerator shortUrlGenerator;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${short-url.domain}")
    private String domain;

    @Value("${short-url.expiration-days:7}") // 默认过期时间 7 天
    private long expirationDays;

    @Override
    public String createShortUrl(String longUrl) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("无效的URL格式，链接必须以http://或https://开头");
        }

        String shortCode;
        int maxAttempts = 10;
        int attempts = 0;
        do {
            if (attempts++ >= maxAttempts) {
                throw new RuntimeException("无法生成唯一短链接，请稍后重试");
            }
            shortCode = shortUrlGenerator.generateShortCode(longUrl);
        } while (shortUrlMapper.countByShortCode(shortCode) > 0);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setLongUrl(longUrl);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setVisitCount(0L);
        shortUrl.setExpireAt(LocalDateTime.now().plusDays(expirationDays)); // 设置过期时间

        shortUrlMapper.insert(shortUrl);
        redisTemplate.opsForValue().set("short:" + shortCode, longUrl, expirationDays * 24 * 60 * 60, TimeUnit.SECONDS); // 设置 Redis TTL（秒）

        return domain + shortCode;
    }

    @Override
    public String getLongUrl(String shortCode) {
        // 检查缓存
        String longUrl = redisTemplate.opsForValue().get("short:" + shortCode);
        if (longUrl != null) {
            ShortUrl shortUrl = shortUrlMapper.findByShortCode(shortCode);
            if (shortUrl != null && isExpired(shortUrl)) {
                throw new IllegalStateException("短链接已过期");
            }
            shortUrlMapper.updateVisitCount(shortCode);
            return longUrl;
        }

        // 检查数据库
        ShortUrl shortUrl = shortUrlMapper.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new IllegalArgumentException("短链接不存在");
        }
        if (isExpired(shortUrl)) {
            throw new IllegalStateException("短链接已过期");
        }

        // 更新缓存
        redisTemplate.opsForValue().set("short:" + shortCode, shortUrl.getLongUrl(), expirationDays * 24 * 60 * 60, TimeUnit.SECONDS);
        shortUrlMapper.updateVisitCount(shortCode);

        return shortUrl.getLongUrl();
    }

    private boolean isValidUrl(String url) {
        return url != null && url.matches("^(https?://)([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    }

    private boolean isExpired(ShortUrl shortUrl) {
        if (shortUrl.getExpireAt() == null) {
            return false; // 永不过期
        }
        return LocalDateTime.now().isAfter(shortUrl.getExpireAt());
    }
}