package com.gis.service;

import com.gis.entity.Camera;
import com.gis.repository.CameraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VideoStreamService {

    private static final Logger logger = LoggerFactory.getLogger(VideoStreamService.class);
    
    private static final int SEGMENT_DURATION = 2;
    private static final int SEGMENT_COUNT = 5;

    @Value("${app.hls.dir:/opt/gis/hls}")
    private String hlsDir;

    private final Map<Long, Process> activeProcesses = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    
    @Autowired
    private CameraRepository cameraRepository;

    public String startStream(Long cameraId) throws Exception {
        synchronized (lock) {
            if (activeProcesses.containsKey(cameraId)) {
                logger.info("Stream already running for camera: {}", cameraId);
                return getStreamUrl(cameraId);
            }
            return doStartStream(cameraId);
        }
    }
    
    private String doStartStream(Long cameraId) throws Exception {
        Camera camera = cameraRepository.findById(cameraId)
                .orElseThrow(() -> new RuntimeException("监控点不存在: " + cameraId));
        
        String rtspUrl = camera.getRtsp_url();
        if (rtspUrl == null || rtspUrl.isEmpty()) {
            throw new RuntimeException("该监控点未配置 RTSP 地址");
        }

        if (!isFfmpegAvailable()) {
            throw new RuntimeException("服务器未安装 ffmpeg，无法播放视频流。请联系管理员安装 ffmpeg。");
        }

        File hlsDirectory = new File(hlsDir);
        if (!hlsDirectory.exists()) {
            hlsDirectory.mkdirs();
        }

        String outputDir = hlsDir + "/" + cameraId;
        File cameraDir = new File(outputDir);
        if (!cameraDir.exists()) {
            cameraDir.mkdirs();
        }

        String playlistPath = outputDir + "/stream.m3u8";

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-rtsp_transport", "tcp",
            "-i", rtspUrl,
            "-c:v", "libx264",
            "-preset", "ultrafast",
            "-tune", "zerolatency",
            "-g", String.valueOf(SEGMENT_DURATION * 25),
            "-sc_threshold", "0",
            "-f", "hls",
            "-hls_time", String.valueOf(SEGMENT_DURATION),
            "-hls_list_size", String.valueOf(SEGMENT_COUNT),
            "-hls_flags", "delete_segments+append_list",
            "-hls_segment_filename", outputDir + "/segment_%03d.ts",
            playlistPath
        );

        pb.redirectErrorStream(true);
        
        logger.info("Starting ffmpeg for camera {}: {}", cameraId, rtspUrl);
        
        Process process = pb.start();
        activeProcesses.put(cameraId, process);

        StringBuilder errorOutput = new StringBuilder();
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("FFmpeg [{}]: {}", cameraId, line);
                    errorOutput.append(line).append("\n");
                }
            } catch (IOException e) {
                logger.error("Error reading ffmpeg output for camera {}: {}", cameraId, e.getMessage());
            }
        }, "ffmpeg-log-" + cameraId).start();

        Thread cleanupThread = new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                logger.info("FFmpeg process ended for camera {} with exit code: {}", cameraId, exitCode);
                activeProcesses.remove(cameraId);
                cleanupHlsFiles(cameraId);
            } catch (InterruptedException e) {
                logger.warn("FFmpeg process interrupted for camera {}", cameraId);
            }
        }, "ffmpeg-cleanup-" + cameraId);
        cleanupThread.setDaemon(true);
        cleanupThread.start();

        Thread.sleep(5000);

        File playlistFile = new File(playlistPath);
        if (!playlistFile.exists()) {
            stopStream(cameraId);
            String errorMsg = errorOutput.toString();
            logger.error("Failed to create HLS stream for camera {}: {}", cameraId, errorMsg);
            throw new RuntimeException("Failed to create HLS stream for camera: " + cameraId + ". Error: " + (errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg));
        }

        return getStreamUrl(cameraId);
    }

    public void stopStream(Long cameraId) {
        Process process = activeProcesses.get(cameraId);
        if (process != null) {
            logger.info("Stopping stream for camera: {}", cameraId);
            process.destroy();
            try {
                process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
            activeProcesses.remove(cameraId);
            cleanupHlsFiles(cameraId);
        }
    }

    public void stopAllStreams() {
        activeProcesses.keySet().forEach(this::stopStream);
    }

    public String getStreamUrl(Long cameraId) {
        return "/hls/" + cameraId + "/stream.m3u8";
    }

    public boolean isStreamActive(Long cameraId) {
        Process process = activeProcesses.get(cameraId);
        return process != null && process.isAlive();
    }

    private boolean isFfmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return process.exitValue() == 0;
        } catch (Exception e) {
            logger.warn("ffmpeg not available: {}", e.getMessage());
            return false;
        }
    }

    private void cleanupHlsFiles(Long cameraId) {
        File cameraDir = new File(hlsDir + "/" + cameraId);
        if (cameraDir.exists()) {
            File[] files = cameraDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            cameraDir.delete();
            logger.info("Cleaned up HLS files for camera: {}", cameraId);
        }
    }
}