package com.gis.service;

import com.gis.entity.GisData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressDeduplicationService {

    // 地址去重
    public List<GisData> deduplicateAddresses(List<GisData> addresses) {
        Map<String, GisData> uniqueAddresses = new HashMap<>();
        
        for (GisData address : addresses) {
            // 生成地址唯一标识
            String key = generateAddressKey(address);
            
            // 如果地址已存在，保留更完整的条目
            if (uniqueAddresses.containsKey(key)) {
                GisData existingAddress = uniqueAddresses.get(key);
                if (isMoreComplete(address, existingAddress)) {
                    uniqueAddresses.put(key, address);
                }
            } else {
                uniqueAddresses.put(key, address);
            }
        }
        
        return new ArrayList<>(uniqueAddresses.values());
    }
    
    // 生成地址唯一标识
    private String generateAddressKey(GisData address) {
        // 基于地址名称和坐标生成唯一标识
        // 坐标保留6位小数，减少精度误差
        String lat = String.format("%.6f", address.getLatitude());
        String lon = String.format("%.6f", address.getLongitude());
        return address.getName() + "_" + lat + "_" + lon;
    }
    
    // 判断地址是否更完整
    private boolean isMoreComplete(GisData newAddress, GisData existingAddress) {
        // 比较地址完整性
        int newScore = calculateCompletenessScore(newAddress);
        int existingScore = calculateCompletenessScore(existingAddress);
        
        return newScore > existingScore;
    }
    
    // 计算地址完整性分数
    private int calculateCompletenessScore(GisData address) {
        int score = 0;
        
        if (address.getName() != null && !address.getName().trim().isEmpty()) {
            score += 1;
        }
        
        if (address.getDescription() != null && !address.getDescription().trim().isEmpty()) {
            score += 1;
        }
        
        if (address.getLatitude() != 0 && address.getLongitude() != 0) {
            score += 1;
        }
        
        if (address.getType() != null && !address.getType().trim().isEmpty()) {
            score += 1;
        }
        
        return score;
    }
    
    // 识别重复地址
    public List<List<GisData>> identifyDuplicateAddresses(List<GisData> addresses) {
        Map<String, List<GisData>> duplicateMap = new HashMap<>();
        
        for (GisData address : addresses) {
            String key = generateAddressKey(address);
            
            if (!duplicateMap.containsKey(key)) {
                duplicateMap.put(key, new ArrayList<>());
            }
            duplicateMap.get(key).add(address);
        }
        
        // 筛选出有重复的地址组
        List<List<GisData>> duplicates = new ArrayList<>();
        for (List<GisData> group : duplicateMap.values()) {
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }
        
        return duplicates;
    }
}
