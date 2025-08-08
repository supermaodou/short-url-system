package com.example.shorturl.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("short_url")
public class ShortUrl {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("short_code")
    private String shortCode;
    
    @TableField("long_url")
    private String longUrl;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("visit_count")
    private Long visitCount;
    
    @TableField("expire_at")
    private LocalDateTime expireAt;
}
