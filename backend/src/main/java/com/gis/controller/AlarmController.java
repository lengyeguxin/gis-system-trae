package com.gis.controller;

import com.gis.entity.Alarm;
import com.gis.repository.AlarmRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private AlarmRepository alarmRepository;

    @GetMapping
    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAllOrderByAlarmTimeDesc();
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
        if (updates.containsKey("alarm_id")) {
            alarm.setAlarm_id((String) updates.get("alarm_id"));
        }
        if (updates.containsKey("alarm_phone")) {
            alarm.setAlarm_phone((String) updates.get("alarm_phone"));
        }
        if (updates.containsKey("alarm_time")) {
            String timeStr = (String) updates.get("alarm_time");
            String normalized = timeStr.replace("T", " ");
            alarm.setAlarm_time(LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updates.containsKey("alarm_location")) {
            alarm.setAlarm_location((String) updates.get("alarm_location"));
        }
        if (updates.containsKey("case_description")) {
            alarm.setCase_description((String) updates.get("case_description"));
        }
        if (updates.containsKey("alarm_type")) {
            alarm.setAlarm_type((String) updates.get("alarm_type"));
        }
        if (updates.containsKey("alarm_level")) {
            Object levelObj = updates.get("alarm_level");
            if (levelObj instanceof Integer) {
                alarm.setAlarm_level((Integer) levelObj);
            } else if (levelObj instanceof String) {
                alarm.setAlarm_level(Integer.parseInt((String) levelObj));
            }
        }
        if (updates.containsKey("lon")) {
            Object lonObj = updates.get("lon");
            if (lonObj instanceof Number) {
                alarm.setLon(((Number) lonObj).doubleValue());
            }
        }
        if (updates.containsKey("lat")) {
            Object latObj = updates.get("lat");
            if (latObj instanceof Number) {
                alarm.setLat(((Number) latObj).doubleValue());
            }
        }
        
        Alarm savedAlarm = alarmRepository.save(alarm);
        return ResponseEntity.ok(savedAlarm);
    }

    @DeleteMapping("/{id}")
    public void deleteAlarm(@PathVariable @NonNull Long id) {
        alarmRepository.deleteById(id);
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("警情导入模板.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("警情信息");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"警情编号*", "警情地点*", "经度*", "纬度*", "报警时间*", "报案人电话", "警情类型*", "级别*", "边界范围(公里)", "案件描述*"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("AJ202404010001");
            exampleRow.createCell(1).setCellValue("北京市东城区某某路口");
            exampleRow.createCell(2).setCellValue(116.407428);
            exampleRow.createCell(3).setCellValue(39.90923);
            exampleRow.createCell(4).setCellValue("2024-04-01 10:00:00");
            exampleRow.createCell(5).setCellValue("13800138000");
            exampleRow.createCell(6).setCellValue("交通事故");
            exampleRow.createCell(7).setCellValue(1);
            exampleRow.createCell(8).setCellValue(1);
            exampleRow.createCell(9).setCellValue("发生交通事故，两车相撞");

            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：级别填写1(高)、2(中)、3(低)；边界范围为数字，默认1公里；带*号为必填项");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importAlarms(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int importedCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String alarmId = getCellValue(row.getCell(0));
                String alarmLocation = getCellValue(row.getCell(1));
                String lonStr = getCellValue(row.getCell(2));
                String latStr = getCellValue(row.getCell(3));
                String alarmTimeStr = getCellValue(row.getCell(4));
                String alarmPhone = getCellValue(row.getCell(5));
                String alarmType = getCellValue(row.getCell(6));
                String levelStr = getCellValue(row.getCell(7));
                String boundaryRangeStr = getCellValue(row.getCell(8));
                String caseDescription = getCellValue(row.getCell(9));

                if (alarmId.isEmpty() || alarmLocation.isEmpty() || lonStr.isEmpty() || latStr.isEmpty()) {
                    continue;
                }

                try {
                    Alarm alarm = new Alarm();
                    alarm.setAlarm_id(alarmId);
                    alarm.setAlarm_location(alarmLocation);
                    alarm.setLon(Double.parseDouble(lonStr));
                    alarm.setLat(Double.parseDouble(latStr));
                    
                    if (!alarmTimeStr.isEmpty()) {
                        alarm.setAlarm_time(LocalDateTime.parse(alarmTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        alarm.setAlarm_time(LocalDateTime.now());
                    }
                    
                    alarm.setAlarm_phone(alarmPhone);
                    alarm.setAlarm_type(alarmType.isEmpty() ? "其他" : alarmType);
                    alarm.setAlarm_level(levelStr.isEmpty() ? 2 : Integer.parseInt(levelStr));
                    alarm.setBoundary_range(boundaryRangeStr.isEmpty() ? 1 : Integer.parseInt(boundaryRangeStr));
                    alarm.setCase_description(caseDescription);
                    alarm.setStatus(0);

                    alarmRepository.save(alarm);
                    importedCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行数据错误: " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("importedCount", importedCount);
            result.put("errors", errors);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
