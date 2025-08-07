package com.example.shorturl.service.impl;

import com.example.shorturl.mapper.ShortUrlMapper;
import com.example.shorturl.model.ShortUrl;
import com.example.shorturl.service.ShortUrlService;
import com.example.shorturl.util.ShortUrlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

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

    @Override
    public String createShortUrl(String longUrl) {
        // Input validation
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        String shortCode;
        do {
            shortCode = shortUrlGenerator.generateShortCode(longUrl);
        } while (shortUrlMapper.countByShortCode(shortCode) > 0);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setLongUrl(longUrl);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setVisitCount(0L);

        shortUrlMapper.insert(shortUrl);
        redisTemplate.opsForValue().set("short:" + shortCode, longUrl);
        
        return domain + shortCode;
    }

    @Override
    public String getLongUrl(String shortCode) {
        // Check cache first
        String longUrl = redisTemplate.opsForValue().get("short:" + shortCode);
        if (longUrl != null) {
            shortUrlMapper.updateVisitCount(shortCode);
            return longUrl;
        }

        // Check database
        ShortUrl shortUrl = shortUrlMapper.findByShortCode(shortCode);
        if (shortUrl == null) {
            throw new IllegalArgumentException("Short URL not found");
        }

        // Update cache
        redisTemplate.opsForValue().set("short:" + shortCode, shortUrl.getLongUrl());
        shortUrlMapper.updateVisitCount(shortCode);
        
        return shortUrl.getLongUrl();
    }

    private boolean isValidUrl(String url) {
        return url != null && url.matches("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$");
    }
}