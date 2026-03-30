package com.gis.controller;

import com.gis.entity.Log;
import com.gis.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:8082")
public class LogController {

    @Autowired
    private LogService logService;

    // 获取所有日志
    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> logs = logService.getAllLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    // 根据ID获取日志
    @GetMapping("/{id}")
    public ResponseEntity<Log> getLogById(@PathVariable @NonNull Long id) {
        Log log = logService.getLogById(id);
        if (log != null) {
            return new ResponseEntity<>(log, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 根据用户名获取日志
    @GetMapping("/username/{username}")
    public ResponseEntity<List<Log>> getLogsByUsername(@PathVariable @NonNull String username) {
        List<Log> logs = logService.getLogsByUsername(username);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    // 根据操作类型获取日志
    @GetMapping("/operation/{operation}")
    public ResponseEntity<List<Log>> getLogsByOperation(@PathVariable @NonNull String operation) {
        List<Log> logs = logService.getLogsByOperation(operation);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    // 根据时间范围获取日志
    @GetMapping("/time-range")
    public ResponseEntity<List<Log>> getLogsByTimeRange(@RequestParam long startTimestamp, @RequestParam long endTimestamp) {
        Date startDate = new Date(startTimestamp);
        Date endDate = new Date(endTimestamp);
        List<Log> logs = logService.getLogsByTimeRange(startDate, endDate);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    // 创建日志
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody @NonNull Log log) {
        Log savedLog = logService.saveLog(log);
        return new ResponseEntity<>(savedLog, HttpStatus.CREATED);
    }

    // 删除日志
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable @NonNull Long id) {
        Log existingLog = logService.getLogById(id);
        if (existingLog != null) {
            logService.deleteLog(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 批量删除日志
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteLogs(@RequestBody @NonNull List<Long> ids) {
        logService.deleteLogs(ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
