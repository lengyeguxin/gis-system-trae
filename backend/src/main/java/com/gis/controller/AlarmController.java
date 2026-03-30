package com.gis.controller;

import com.gis.entity.Alarm;
import com.gis.repository.AlarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/alarm")
@CrossOrigin(origins = "http://localhost:8082")
public class AlarmController {

    @Autowired
    private AlarmRepository alarmRepository;

    @GetMapping
    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Alarm> getAlarmById(@PathVariable @NonNull Long id) {
        return alarmRepository.findById(id);
    }

    @PostMapping
    public Alarm addAlarm(@RequestBody @NonNull Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alarm> updateAlarm(@PathVariable @NonNull Long id, @RequestBody Map<String, Object> updates) {
        Optional<Alarm> existingAlarm = alarmRepository.findById(id);
        if (!existingAlarm.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Alarm alarm = existingAlarm.get();
        if (updates.containsKey("handling_result")) {
            alarm.setHandling_result((String) updates.get("handling_result"));
        }
        if (updates.containsKey("status")) {
            Object statusObj = updates.get("status");
            if (statusObj instanceof Integer) {
                alarm.setStatus((Integer) statusObj);
            } else if (statusObj instanceof String) {
                alarm.setStatus(Integer.parseInt((String) statusObj));
            }
        }
        
        Alarm savedAlarm = alarmRepository.save(alarm);
        return ResponseEntity.ok(savedAlarm);
    }

    @DeleteMapping("/{id}")
    public void deleteAlarm(@PathVariable @NonNull Long id) {
        alarmRepository.deleteById(id);
    }
}
