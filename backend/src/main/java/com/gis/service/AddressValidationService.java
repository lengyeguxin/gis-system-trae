package com.gis.service;

import com.gis.entity.GisData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressValidationService {

    // 验证地址数据
    public List<String> validateAddress(GisData address) {
        List<String> errors = new ArrayList<>();
        
        // 验证地址名称
        if (address.getName() == null || address.getName().trim().isEmpty()) {
            errors.add("地址名称不能为空");
        }
        
        // 验证经纬度
        if (address.getLatitude() == 0 || address.getLongitude() == 0) {
            errors.add("经纬度不能为空");
        } else {
            // 验证纬度范围（-90到90）
            if (address.getLatitude() < -90 || address.getLatitude() > 90) {
                errors.add("纬度必须在-90到90之间");
            }
            // 验证经度范围（-180到180）
            if (address.getLongitude() < -180 || address.getLongitude() > 180) {
                errors.add("经度必须在-180到180之间");
            }
        }
        
        // 验证类型
        if (address.getType() == null || address.getType().trim().isEmpty()) {
            errors.add("类型不能为空");
        } else if (!address.getType().matches("police|monitor|alarm|address")) {
            errors.add("类型必须为 police、monitor、alarm 或 address");
        }
        
        // 验证地址编码（如果有）
        // 这里可以添加具体的编码规则验证逻辑
        
        return errors;
    }
    
    // 坐标交叉验证
    public boolean validateCoordinateCross(double latitude, double longitude) {
        // 这里可以实现坐标交叉验证逻辑
        // 例如：验证坐标是否在有效范围内，是否与其他地址坐标冲突等
        return true;
    }
    
    // 地址有效性判定
    public boolean validateAddressValidity(GisData address) {
        // 这里可以实现地址有效性判定逻辑
        // 例如：验证地址是否存在，是否符合标准格式等
        return address.getName() != null && !address.getName().trim().isEmpty() &&
               address.getLatitude() != 0 && address.getLongitude() != 0 &&
               address.getType() != null && !address.getType().trim().isEmpty();
    }
    
    // 批量验证地址数据
    public List<String> validateAddresses(List<GisData> addresses) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < addresses.size(); i++) {
            GisData address = addresses.get(i);
            int index = i + 1;
            
            List<String> addressErrors = validateAddress(address);
            if (!addressErrors.isEmpty()) {
                errors.add("第" + index + "条数据：" + String.join(", ", addressErrors));
            }
        }
        
        return errors;
    }
}
