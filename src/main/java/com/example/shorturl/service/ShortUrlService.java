package com.example.shorturl.service;

public interface ShortUrlService {
    String createShortUrl(String longUrl);
    String getLongUrl(String shortCode);
}