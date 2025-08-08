package com.example.shorturl.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shorturl.mapper.ShortUrlMapper;
import com.example.shorturl.model.ShortUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 定时清理配置类
 * 负责清理过期的短链接和相关缓存
 */
@Configuration
@EnableScheduling
public class CleanupConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CleanupConfig.class);
    
    @Autowired
    private ShortUrlMapper shortUrlMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 定时清理过期链接
     * 每天凌晨2点执行，避开业务高峰期
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLinks() {
        logger.info("开始执行过期链接清理任务...");
        
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            
            // 1. 查询即将过期的链接（用于清理Redis缓存）
            List<ShortUrl> expiredLinks = getExpiredLinks(currentTime);
            
            if (expiredLinks.isEmpty()) {
                logger.info("没有发现过期链接，清理任务完成");
                return;
            }
            
            logger.info("发现 {} 个过期链接，开始清理...", expiredLinks.size());
            
            // 2. 清理Redis缓存
            int cacheCleanedCount = cleanupRedisCache(expiredLinks);
            
            // 3. 删除数据库中的过期记录
            int dbCleanedCount = cleanupDatabase(currentTime);
            
            logger.info("过期链接清理完成 - 数据库清理: {} 条, 缓存清理: {} 条", 
                    dbCleanedCount, cacheCleanedCount);
            
        } catch (Exception e) {
            logger.error("清理过期链接时发生错误", e);
        }
    }
    
    /**
     * 手动触发清理任务（用于测试或紧急清理）
     */
    @Scheduled(fixedRate = 3600000) // 每小时检查一次
    public void hourlyCleanupCheck() {
        logger.debug("执行每小时清理检查...");
        
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            
            // 只清理严重过期的链接（过期超过1天）
            LocalDateTime severlyExpiredTime = currentTime.minusDays(1);
            
            // 查询严重过期的链接数量
            long expiredCount = shortUrlMapper.selectCount(
                new QueryWrapper<ShortUrl>()
                    .isNotNull("expire_at")
                    .lt("expire_at", severlyExpiredTime)
            );
            
            if (expiredCount > 100) { // 如果严重过期链接超过100个，立即清理
                logger.warn("发现 {} 个严重过期链接，立即执行清理", expiredCount);
                cleanupExpiredLinks();
            }
            
        } catch (Exception e) {
            logger.error("每小时清理检查时发生错误", e);
        }
    }
    
    /**
     * 获取过期的链接列表
     */
    private List<ShortUrl> getExpiredLinks(LocalDateTime currentTime) {
        return shortUrlMapper.selectList(
            new QueryWrapper<ShortUrl>()
                .isNotNull("expire_at")
                .lt("expire_at", currentTime)
                .last("LIMIT 1000") // 限制每次处理的数量，避免内存溢出
        );
    }
    
    /**
     * 清理Redis缓存
     */
    private int cleanupRedisCache(List<ShortUrl> expiredLinks) {
        int cleanedCount = 0;
        
        try {
            for (ShortUrl expiredLink : expiredLinks) {
                String cacheKey = "short:" + expiredLink.getShortCode();
                
                if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
                    redisTemplate.delete(cacheKey);
                    cleanedCount++;
                }
            }
            
            // 批量清理可能的其他相关缓存
            Set<String> expiredKeys = redisTemplate.keys("short:*");
            if (expiredKeys != null && !expiredKeys.isEmpty()) {
                // 这里可以添加更复杂的缓存清理逻辑
                logger.debug("检查到 {} 个缓存键", expiredKeys.size());
            }
            
        } catch (Exception e) {
            logger.error("清理Redis缓存时发生错误", e);
        }
        
        return cleanedCount;
    }
    
    /**
     * 清理数据库中的过期记录
     */
    private int cleanupDatabase(LocalDateTime currentTime) {
        try {
            // 使用Mapper中的deleteExpired方法
            shortUrlMapper.deleteExpired(currentTime);
            
            // 由于deleteExpired方法没有返回值，我们需要先查询数量
            // 这里返回一个估算值，实际项目中可以修改Mapper方法返回删除数量
            return shortUrlMapper.selectCount(
                new QueryWrapper<ShortUrl>()
                    .isNotNull("expire_at")
                    .lt("expire_at", currentTime)
            ).intValue();
            
        } catch (Exception e) {
            logger.error("清理数据库时发生错误", e);
            return 0;
        }
    }
    
    /**
     * 获取清理统计信息
     */
    public CleanupStats getCleanupStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 统计即将过期的链接（未来24小时内过期）
            long soonToExpire = shortUrlMapper.selectCount(
                new QueryWrapper<ShortUrl>()
                    .isNotNull("expire_at")
                    .between("expire_at", now, now.plusDays(1))
            );
            
            // 统计已过期的链接
            long expired = shortUrlMapper.selectCount(
                new QueryWrapper<ShortUrl>()
                    .isNotNull("expire_at")
                    .lt("expire_at", now)
            );
            
            // 统计总链接数
            long total = shortUrlMapper.selectCount(new QueryWrapper<>());
            
            return new CleanupStats(total, expired, soonToExpire);
            
        } catch (Exception e) {
            logger.error("获取清理统计信息时发生错误", e);
            return new CleanupStats(0, 0, 0);
        }
    }
    
    /**
     * 清理统计信息类
     */
    public static class CleanupStats {
        private final long totalLinks;
        private final long expiredLinks;
        private final long soonToExpireLinks;
        
        public CleanupStats(long totalLinks, long expiredLinks, long soonToExpireLinks) {
            this.totalLinks = totalLinks;
            this.expiredLinks = expiredLinks;
            this.soonToExpireLinks = soonToExpireLinks;
        }
        
        public long getTotalLinks() { return totalLinks; }
        public long getExpiredLinks() { return expiredLinks; }
        public long getSoonToExpireLinks() { return soonToExpireLinks; }
        
        @Override
        public String toString() {
            return String.format("CleanupStats{总链接=%d, 已过期=%d, 即将过期=%d}", 
                    totalLinks, expiredLinks, soonToExpireLinks);
        }
    }
}
