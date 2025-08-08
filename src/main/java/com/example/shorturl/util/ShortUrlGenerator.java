package com.example.shorturl.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

@Component
public class ShortUrlGenerator {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    
    @Value("${short-url.length}")
    private int length;

    public String generateShortCode(String longUrl) {
        try {
            // 结合时间戳和随机数增加唯一性
            String seed = longUrl + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(seed.getBytes());
            StringBuilder sb = new StringBuilder();
            long num = Math.abs(hash.hashCode() ^ System.currentTimeMillis());

            while (sb.length() < length) {
                sb.append(BASE62.charAt((int)(num % BASE)));
                num /= BASE;
            }
            return sb.toString();
        } catch (Exception e) {
            return randomString(length);
        }
    }

    private String randomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE)));
        }
        return sb.toString();
    }
}