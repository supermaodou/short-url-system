package com.example.shorturl;

import com.example.shorturl.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ShortUrlSystemApplicationTests {

    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void testCreateAndRedirect() {
        String longUrl = "https://www.example.com";
        String shortUrl = shortUrlService.createShortUrl(longUrl);
        assert shortUrl.startsWith("http://localhost:8087/");
        String retrievedUrl = shortUrlService.getLongUrl(shortUrl.substring(shortUrl.length() - 6));
        assert longUrl.equals(retrievedUrl);
    }

    @Test
    public void TestRedis() {
        redisTemplate.opsForValue().set("test1", "test1");
        assert Objects.equals(redisTemplate.opsForValue().get("test1"), "test1");

        redisTemplate.opsForValue().set("test2", "test2", 1, TimeUnit.SECONDS);
    }


}
