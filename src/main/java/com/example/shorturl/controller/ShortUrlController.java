package com.example.shorturl.controller;

import com.example.shorturl.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class ShortUrlController {
    @Autowired
    private ShortUrlService shortUrlService;

    @PostMapping(value = "/shorten", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {
        // 解码 URL 编码的输入
        String decodedUrl = URLDecoder.decode(longUrl, StandardCharsets.UTF_8);
        System.out.println("Received longUrl: [" + longUrl + "], Decoded: [" + decodedUrl + "], Length: " + decodedUrl.length());
        String shortUrl = shortUrlService.createShortUrl(decodedUrl);
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirect(@PathVariable String shortCode) {
        String longUrl = shortUrlService.getLongUrl(shortCode);
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "http://" + longUrl;
        }
        return ResponseEntity.status(302).header("Location", longUrl).build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleExpiredLink(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidLink(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}