package com.gis.controller;

import com.gis.entity.Address;
import com.gis.service.AddressService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "http://localhost:8082")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable @NonNull Long id) {
        return addressService.getAddressById(id)
                .map(address -> new ResponseEntity<>(address, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody @NonNull Address address) {
        Address savedAddress = addressService.saveAddress(address);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable @NonNull Long id, @RequestBody @NonNull Address address) {
        return addressService.getAddressById(id)
                .map(existingAddress -> {
                    address.setId(id);
                    Address updatedAddress = addressService.saveAddress(address);
                    return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable @NonNull Long id) {
        if (addressService.getAddressById(id).isPresent()) {
            addressService.deleteAddress(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/district/{district}")
    public ResponseEntity<List<Address>> getAddressesByDistrict(@PathVariable @NonNull String district) {
        List<Address> addresses = addressService.getAddressesByDistrict(district);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Address>> searchAddresses(
            @RequestParam @NonNull String keyword,
            @RequestParam(required = false, defaultValue = "fuzzy") String mode) {
        List<Address> addresses = addressService.searchAddresses(keyword, mode);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("地址库导入模板.xlsx", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("地址信息");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"标准地址*", "行政区划代码", "行政区划名称", "街道代码", "街道名称", "门牌号", "经度*", "纬度*", "来源", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("北京市东城区东直门外大街42号");
            exampleRow.createCell(1).setCellValue("110101");
            exampleRow.createCell(2).setCellValue("东城区");
            exampleRow.createCell(3).setCellValue("110101001");
            exampleRow.createCell(4).setCellValue("东直门外大街");
            exampleRow.createCell(5).setCellValue("42号");
            exampleRow.createCell(6).setCellValue(116.4166);
            exampleRow.createCell(7).setCellValue(39.9288);
            exampleRow.createCell(8).setCellValue("manual");
            exampleRow.createCell(9).setCellValue("示例地址");

            Row noteRow = sheet.createRow(3);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：来源可选manual/import/interface；带*号为必填项");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importAddresses(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int importedCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String addressFull = getCellValue(row.getCell(0));
                String adminCode = getCellValue(row.getCell(1));
                String adminName = getCellValue(row.getCell(2));
                String streetCode = getCellValue(row.getCell(3));
                String street = getCellValue(row.getCell(4));
                String houseNumber = getCellValue(row.getCell(5));
                String lonStr = getCellValue(row.getCell(6));
                String latStr = getCellValue(row.getCell(7));
                String source = getCellValue(row.getCell(8));
                String remark = getCellValue(row.getCell(9));

                if (addressFull.isEmpty() || lonStr.isEmpty() || latStr.isEmpty()) {
                    continue;
                }

                try {
                    Address address = new Address();
                    address.setAddress_full(addressFull);
                    address.setAdmin_code(adminCode);
                    address.setAdmin_name(adminName);
                    address.setStreet_code(streetCode);
                    address.setStreet(street);
                    address.setHouse_number(houseNumber);
                    address.setLon(Double.parseDouble(lonStr));
                    address.setLat(Double.parseDouble(latStr));
                    address.setSource(source.isEmpty() ? "import" : source);
                    address.setRemark(remark);
                    address.setStatus(1);

                    addressService.saveAddress(address);
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
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
