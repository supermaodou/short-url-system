package com.example.shorturl.mapper;

import com.example.shorturl.model.ShortUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShortUrlMapper {
    ShortUrl findByShortCode(@Param("shortCode") String shortCode);

    void insert(ShortUrl shortUrl);

    void updateVisitCount(@Param("shortCode") String shortCode);

    int countByShortCode(@Param("shortCode") String shortCode);
}