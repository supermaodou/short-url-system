package com.example.shorturl.util;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * URL验证工具类
 * 提供多种URL验证方式，确保输入URL的有效性和安全性
 */
@Component
public class UrlValidator {

    // URL最大长度限制
    private static final int MAX_URL_LENGTH = 2048;
    
    // 危险协议列表
    private static final String[] DANGEROUS_PROTOCOLS = {
        "javascript:", "data:", "vbscript:", "file:", "ftp:"
    };
    
    // 完整的URL正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)(" +
        // 域名部分：支持国际化域名、IPv4地址
        "([\\w\\u4e00-\\u9fa5-]+\\.)*[\\w\\u4e00-\\u9fa5-]+(\\.[a-zA-Z]{2,})|" +  // 普通域名
        "localhost|" +  // localhost
        "((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)" +  // IPv4
        ")" +
        "(:\\d{1,5})?" +  // 端口号 (1-65535)
        "(/[\\w\\u4e00-\\u9fa5.~:/?#\\[\\]@!$&'()*+,;=%-]*)?$",  // 路径部分
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 验证URL是否有效
     * 
     * @param url 待验证的URL
     * @return true表示有效，false表示无效
     */
    public boolean isValidUrl(String url) {
        // 基础检查
        if (!basicCheck(url)) {
            return false;
        }
        
        url = url.trim();
        
        // 安全检查
        if (!securityCheck(url)) {
            return false;
        }
        
        // 格式检查
        if (!formatCheck(url)) {
            return false;
        }
        
        // Java URL类验证
        return javaUrlCheck(url);
    }
    
    /**
     * 基础检查：空值、长度
     */
    private boolean basicCheck(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        if (url.trim().length() > MAX_URL_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 安全检查：防止XSS和其他安全问题
     */
    private boolean securityCheck(String url) {
        String lowerUrl = url.toLowerCase();
        
        // 检查危险协议
        for (String protocol : DANGEROUS_PROTOCOLS) {
            if (lowerUrl.startsWith(protocol)) {
                return false;
            }
        }
        
        // 检查是否包含危险字符
        if (lowerUrl.contains("<script") || lowerUrl.contains("javascript:") || 
            lowerUrl.contains("onload=") || lowerUrl.contains("onerror=")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 格式检查：使用正则表达式验证URL格式
     */
    private boolean formatCheck(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * Java URL类验证：最终验证
     */
    private boolean javaUrlCheck(String url) {
        try {
            URL urlObj = new URL(url);
            
            // 检查协议
            String protocol = urlObj.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                return false;
            }
            
            // 检查主机名
            String host = urlObj.getHost();
            if (host == null || host.trim().isEmpty()) {
                return false;
            }
            
            // 检查端口号
            int port = urlObj.getPort();
            if (port != -1 && (port < 1 || port > 65535)) {
                return false;
            }
            
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    /**
     * 标准化URL：添加协议前缀、移除多余空格等
     */
    public String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        
        url = url.trim();
        
        // 如果没有协议前缀，默认添加http://
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        
        return url;
    }
    
    /**
     * 获取URL验证失败的详细原因
     */
    public String getValidationError(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "URL不能为空";
        }
        
        url = url.trim();
        
        if (url.length() > MAX_URL_LENGTH) {
            return "URL长度超过限制（最大" + MAX_URL_LENGTH + "字符）";
        }
        
        String lowerUrl = url.toLowerCase();
        for (String protocol : DANGEROUS_PROTOCOLS) {
            if (lowerUrl.startsWith(protocol)) {
                return "不支持的协议：" + protocol;
            }
        }
        
        if (!formatCheck(url)) {
            return "URL格式不正确，请确保URL以http://或https://开头";
        }
        
        if (!javaUrlCheck(url)) {
            return "URL格式验证失败，请检查URL是否正确";
        }
        
        return "URL验证通过";
    }
}