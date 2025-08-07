package com.example.shorturl.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortUrl {
    private Long id;
    private String shortCode;
    private String longUrl;
    private LocalDateTime createdAt;
    private Long visitCount;
}