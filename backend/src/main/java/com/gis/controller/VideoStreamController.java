package com.gis.controller;

import com.gis.service.VideoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = {"http://localhost:8082", "http://127.0.0.1:8082"})
public class VideoStreamController {

    @Autowired
    private VideoStreamService videoStreamService;

    @PostMapping("/stream/{cameraId}")
    public ResponseEntity<Map<String, Object>> startStream(@PathVariable Long cameraId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String streamUrl = videoStreamService.startStream(cameraId);
            response.put("success", true);
            response.put("streamUrl", streamUrl);
            response.put("message", "Stream started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/stream/{cameraId}")
    public ResponseEntity<Map<String, Object>> stopStream(@PathVariable Long cameraId) {
        Map<String, Object> response = new HashMap<>();
        videoStreamService.stopStream(cameraId);
        response.put("success", true);
        response.put("message", "Stream stopped successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{cameraId}")
    public ResponseEntity<Map<String, Object>> getStreamStatus(@PathVariable Long cameraId) {
        Map<String, Object> response = new HashMap<>();
        boolean isActive = videoStreamService.isStreamActive(cameraId);
        response.put("active", isActive);
        if (isActive) {
            response.put("streamUrl", videoStreamService.getStreamUrl(cameraId));
        }
        return ResponseEntity.ok(response);
    }
}