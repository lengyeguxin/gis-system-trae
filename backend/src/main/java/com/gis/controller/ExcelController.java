package com.gis.controller;

import com.gis.entity.GisData;
import com.gis.service.ExcelService;
import com.gis.service.GisDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private GisDataService gisDataService;
    
    // 下载Excel模板
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            excelService.downloadTemplate(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 批量导入数据
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestParam @NonNull MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析Excel文件
            List<GisData> dataList = excelService.parseExcel(file.getInputStream());
            
            // 数据格式校验
            List<String> errors = excelService.validateData(dataList);
            
            if (!errors.isEmpty()) {
                result.put("success", false);
                result.put("message", "数据格式验证失败");
                result.put("errors", errors);
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }
            
            // 保存数据
            int savedCount = 0;
            for (GisData data : dataList) {
                if (data != null) {
                    gisDataService.saveGisData(data);
                    savedCount++;
                }
            }
            int importedCount = savedCount;
            
            result.put("success", true);
            result.put("message", "数据导入成功");
            result.put("importedCount", importedCount);
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件解析失败");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
