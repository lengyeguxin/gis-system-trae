package com.gis.controller;

import com.gis.entity.Alarm;
import com.gis.repository.AlarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alarm")
@CrossOrigin(origins = "http://localhost:8082")
public class AlarmController {

    @Autowired
    private AlarmRepository alarmRepository;

    // 获取所有警情
    @GetMapping
    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    // 根据ID获取警情
    @GetMapping("/{id}")
    public Optional<Alarm> getAlarmById(@PathVariable @NonNull Long id) {
        return alarmRepository.findById(id);
    }

    // 添加警情
    @PostMapping
    public Alarm addAlarm(@RequestBody @NonNull Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    // 更新警情
    @PutMapping("/{id}")
    public Alarm updateAlarm(@PathVariable @NonNull Long id, @RequestBody @NonNull Alarm alarm) {
        alarm.setId(id);
        return alarmRepository.save(alarm);
    }

    // 删除警情
    @DeleteMapping("/{id}")
    public void deleteAlarm(@PathVariable @NonNull Long id) {
        alarmRepository.deleteById(id);
    }
}
