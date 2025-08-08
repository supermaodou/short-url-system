package com.example.shorturl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shorturl.model.ShortUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface ShortUrlMapper extends BaseMapper<ShortUrl> {
    
    void updateVisitCount(@Param("shortCode") String shortCode);

    void deleteExpired(@Param("currentTime") LocalDateTime currentTime);
}
