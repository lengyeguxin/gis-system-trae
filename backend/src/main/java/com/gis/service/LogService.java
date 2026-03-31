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

    public List<Log> getAllLogs() {
        return logRepository.findAllOrderByCreateTimeDesc();
    }

    public Log getLogById(@NonNull Long id) {
        return logRepository.findById(id).orElse(null);
    }

    public List<Log> getLogsByUsername(@NonNull String username) {
        return logRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    public List<Log> getLogsByOperation(@NonNull String operation) {
        return logRepository.findByOperationOrderByCreateTimeDesc(operation);
    }

    public List<Log> getLogsByTimeRange(@NonNull Date startDate, @NonNull Date endDate) {
        return logRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(startDate, endDate);
    }

    public Log saveLog(@NonNull Log log) {
        return logRepository.save(log);
    }

    public void deleteLog(@NonNull Long id) {
        logRepository.deleteById(id);
    }

    public void deleteLogs(@NonNull List<Long> ids) {
        for (Long id : ids) {
            if (id != null) {
                logRepository.deleteById(id);
            }
        }
    }
}
