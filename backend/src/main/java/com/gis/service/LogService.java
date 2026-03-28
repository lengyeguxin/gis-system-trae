package com.gis.service;

import com.gis.entity.Log;
import com.gis.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    // 获取所有日志
    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    // 根据ID获取日志
    public Log getLogById(@NonNull Long id) {
        return logRepository.findById(id).orElse(null);
    }

    // 根据用户名获取日志
    public List<Log> getLogsByUsername(@NonNull String username) {
        return logRepository.findByUsername(username);
    }

    // 根据操作类型获取日志
    public List<Log> getLogsByOperation(@NonNull String operation) {
        return logRepository.findByOperation(operation);
    }

    // 根据时间范围获取日志
    public List<Log> getLogsByTimeRange(@NonNull Date startDate, @NonNull Date endDate) {
        return logRepository.findByCreateTimeBetween(startDate, endDate);
    }

    // 保存日志
    public Log saveLog(@NonNull Log log) {
        return logRepository.save(log);
    }

    // 删除日志
    public void deleteLog(@NonNull Long id) {
        logRepository.deleteById(id);
    }

    // 批量删除日志
    public void deleteLogs(@NonNull List<Long> ids) {
        for (Long id : ids) {
            if (id != null) {
                logRepository.deleteById(id);
            }
        }
    }
}
