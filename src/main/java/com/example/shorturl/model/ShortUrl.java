package com.example.shorturl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {
    private Long id;
    private String shortCode;
    private String longUrl;
    private LocalDateTime createdAt;
    private Long visitCount;
    private LocalDateTime expireAt;
}