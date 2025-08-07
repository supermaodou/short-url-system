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
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(longUrl.getBytes());
            StringBuilder sb = new StringBuilder();
            long num = Math.abs(Arrays.hashCode(hash));
            
            while (sb.length() < length) {
                sb.append(BASE62.charAt((int)(num % BASE)));
                num /= BASE;
            }
            return sb.toString();
        } catch (Exception e) {
            // Fallback to random string if hash fails
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