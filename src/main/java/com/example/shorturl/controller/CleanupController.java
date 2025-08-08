package com.example.shorturl.controller;

import com.example.shorturl.config.CleanupConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 清理任务管理控制器
 * 提供手动触发清理和查看清理统计的API
 */
@RestController
@RequestMapping("/api/admin/cleanup")
public class CleanupController {

    @Autowired
    private CleanupConfig cleanupConfig;

    /**
     * 手动触发过期链接清理
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerCleanup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取清理前的统计信息
            CleanupConfig.CleanupStats beforeStats = cleanupConfig.getCleanupStats();
            
            // 执行清理
            cleanupConfig.cleanupExpiredLinks();
            
            // 获取清理后的统计信息
            CleanupConfig.CleanupStats afterStats = cleanupConfig.getCleanupStats();
            
            response.put("success", true);
            response.put("message", "清理任务执行完成");
            response.put("beforeCleanup", Map.of(
                "totalLinks", beforeStats.getTotalLinks(),
                "expiredLinks", beforeStats.getExpiredLinks(),
                "soonToExpireLinks", beforeStats.getSoonToExpireLinks()
            ));
            response.put("afterCleanup", Map.of(
                "totalLinks", afterStats.getTotalLinks(),
                "expiredLinks", afterStats.getExpiredLinks(),
                "soonToExpireLinks", afterStats.getSoonToExpireLinks()
            ));
            response.put("cleanedCount", beforeStats.getExpiredLinks() - afterStats.getExpiredLinks());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理任务执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取清理统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCleanupStats() {
        try {
            CleanupConfig.CleanupStats stats = cleanupConfig.getCleanupStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalLinks", stats.getTotalLinks());
            response.put("expiredLinks", stats.getExpiredLinks());
            response.put("soonToExpireLinks", stats.getSoonToExpireLinks());
            response.put("activeLinks", stats.getTotalLinks() - stats.getExpiredLinks());
            
            // 计算过期率
            double expiredRate = stats.getTotalLinks() > 0 ? 
                (double) stats.getExpiredLinks() / stats.getTotalLinks() * 100 : 0;
            response.put("expiredRate", Math.round(expiredRate * 100.0) / 100.0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取清理任务健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCleanupHealth() {
        try {
            CleanupConfig.CleanupStats stats = cleanupConfig.getCleanupStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", "healthy");
            
            // 健康检查逻辑
            String healthStatus = "healthy";
            String message = "清理任务运行正常";
            
            // 如果过期链接过多，标记为警告
            if (stats.getExpiredLinks() > 1000) {
                healthStatus = "warning";
                message = "发现大量过期链接，建议立即清理";
            }
            
            // 如果过期链接极多，标记为不健康
            if (stats.getExpiredLinks() > 10000) {
                healthStatus = "unhealthy";
                message = "过期链接数量过多，系统性能可能受影响";
            }
            
            response.put("status", healthStatus);
            response.put("message", message);
            response.put("expiredCount", stats.getExpiredLinks());
            response.put("totalCount", stats.getTotalLinks());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("status", "error");
            response.put("message", "健康检查失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}