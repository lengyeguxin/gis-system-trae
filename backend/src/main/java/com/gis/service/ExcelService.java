package com.gis.service;

import com.gis.entity.GisData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    // 下载Excel模板
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("地址数据模板");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("地址名称");
        headerRow.createCell(1).setCellValue("地址描述");
        headerRow.createCell(2).setCellValue("纬度");
        headerRow.createCell(3).setCellValue("经度");
        headerRow.createCell(4).setCellValue("类型");
        
        // 设置列宽
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 40 * 256);
        sheet.setColumnWidth(2, 15 * 256);
        sheet.setColumnWidth(3, 15 * 256);
        sheet.setColumnWidth(4, 15 * 256);
        
        // 示例数据
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("北京市东城区");
        exampleRow.createCell(1).setCellValue("北京市东城区政府");
        exampleRow.createCell(2).setCellValue(39.9288);
        exampleRow.createCell(3).setCellValue(116.4166);
        exampleRow.createCell(4).setCellValue("address");
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=address_template.xlsx");
        
        // 输出到响应流
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }
    
    // 解析Excel文件
    public List<GisData> parseExcel(InputStream inputStream) throws IOException {
        List<GisData> dataList = new ArrayList<>();
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        // 遍历行（跳过标题行）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            // 读取单元格数据
            String name = getCellValue(row.getCell(0));
            String description = getCellValue(row.getCell(1));
            double latitude = getCellNumericValue(row.getCell(2));
            double longitude = getCellNumericValue(row.getCell(3));
            String type = getCellValue(row.getCell(4));
            
            // 创建GisData对象
            GisData data = new GisData(null, name, description, latitude, longitude, type);
            dataList.add(data);
        }
        
        workbook.close();
        return dataList;
    }
    
    // 获取单元格字符串值
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    // 获取单元格数值
    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
    
    // 数据格式校验
    public List<String> validateData(List<GisData> dataList) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i++) {
            GisData data = dataList.get(i);
            int rowNum = i + 2; // 行号（从2开始，因为第一行是标题）
            
            // 验证地址名称
            if (data.getName() == null || data.getName().trim().isEmpty()) {
                errors.add("第" + rowNum + "行：地址名称不能为空");
            }
            
            // 验证经纬度
            if (data.getLatitude() == 0 || data.getLongitude() == 0) {
                errors.add("第" + rowNum + "行：经纬度不能为空");
            }
            
            // 验证类型
            if (data.getType() == null || data.getType().trim().isEmpty()) {
                errors.add("第" + rowNum + "行：类型不能为空");
            } else if (!data.getType().matches("police|monitor|alarm|address")) {
                errors.add("第" + rowNum + "行：类型必须为 police、monitor、alarm 或 address");
            }
        }
        
        return errors;
    }
}
