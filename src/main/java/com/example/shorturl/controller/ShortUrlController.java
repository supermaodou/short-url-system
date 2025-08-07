package com.example.shorturl.controller;

import com.example.shorturl.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ShortUrlController {
    @Autowired
    private ShortUrlService shortUrlService;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {
        String shortUrl = shortUrlService.createShortUrl(longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirect(@PathVariable String shortCode) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        return ResponseEntity.status(302).header("Location", longUrl).build();
    }
}