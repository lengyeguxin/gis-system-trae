package com.gis.util;

import com.gis.entity.Log;
import com.gis.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class LogHelper {

    private static LogHelper instance;

    @Autowired
    private LogService logService;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static void log(String username, String operation, String details, String ipAddress) {
        if (instance == null || instance.logService == null) {
            return;
        }
        try {
            Log log = new Log();
            log.setUsername(username != null ? username : "system");
            log.setOperation(operation);
            log.setDetails(details);
            log.setIpAddress(ipAddress != null ? ipAddress : "unknown");
            log.setCreateTime(new Date());
            instance.logService.saveLog(log);
        } catch (Exception e) {
            System.err.println("Failed to save log: " + e.getMessage());
        }
    }

    public static void log(String username, String operation, String details) {
        log(username, operation, details, null);
    }

    public static String getRealIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index);
            }
            return ip.trim();
        }
        
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        return request.getRemoteAddr();
    }
}