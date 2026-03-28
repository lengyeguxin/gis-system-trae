package com.gis.service;

import com.gis.entity.GisData;
import com.gis.repository.GisDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GisDataService {

    @Autowired
    private GisDataRepository gisDataRepository;

    public List<GisData> getAllGisData() {
        // 暂时返回模拟数据
        List<GisData> allData = new ArrayList<>();
        allData.addAll(getPolicePoints());
        allData.addAll(getMonitorPoints());
        allData.addAll(getAlarmPoints());
        allData.addAll(getAddressPoints());
        return allData;
    }

    public Optional<GisData> getGisDataById(@NonNull Long id) {
        // 暂时返回模拟数据
        List<GisData> allData = getAllGisData();
        for (GisData data : allData) {
            if (data.getId().equals(id)) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    public GisData saveGisData(@NonNull GisData gisData) {
        // 暂时返回模拟数据
        return gisData;
    }

    public void deleteGisData(@NonNull Long id) {
        // 暂时不做任何操作
    }

    // 新增方法：获取警务点数据
    public List<GisData> getPolicePoints() {
        // 暂时返回模拟数据
        List<GisData> policePoints = new ArrayList<>();
        policePoints.add(new GisData(1L, "派出所1", "北京市公安局东城分局", 39.90923, 116.397428, "police"));
        policePoints.add(new GisData(2L, "派出所2", "北京市公安局西城分局", 39.91923, 116.407428, "police"));
        return policePoints;
    }

    // 新增方法：获取监控点数据
    public List<GisData> getMonitorPoints() {
        // 暂时返回模拟数据
        List<GisData> monitorPoints = new ArrayList<>();
        monitorPoints.add(new GisData(1L, "监控点1", "路口监控", 39.90923, 116.417428, "monitor"));
        monitorPoints.add(new GisData(2L, "监控点2", "小区监控", 39.92923, 116.397428, "monitor"));
        return monitorPoints;
    }

    // 新增方法：获取警情信息数据
    public List<GisData> getAlarmPoints() {
        // 暂时返回模拟数据
        List<GisData> alarmPoints = new ArrayList<>();
        alarmPoints.add(new GisData(1L, "警情1", "交通事故", 39.91923, 116.417428, "alarm"));
        alarmPoints.add(new GisData(2L, "警情2", "纠纷", 39.92923, 116.407428, "alarm"));
        return alarmPoints;
    }

    // 新增方法：获取地址数据
    public List<GisData> getAddressPoints() {
        // 暂时返回模拟数据
        List<GisData> addressPoints = new ArrayList<>();
        addressPoints.add(new GisData(1L, "地址1", "北京市东城区", 39.90923, 116.397428, "address"));
        addressPoints.add(new GisData(2L, "地址2", "北京市西城区", 39.91923, 116.407428, "address"));
        return addressPoints;
    }
    
    // 按类型获取数据
    public List<GisData> getByType(String type) {
        // 暂时返回模拟数据
        List<GisData> dataList = new ArrayList<>();
        switch (type) {
            case "police":
                return getPolicePoints();
            case "monitor":
                return getMonitorPoints();
            case "alarm":
                return getAlarmPoints();
            case "address":
                return getAddressPoints();
            default:
                return dataList;
        }
    }
    
    // 搜索功能
    public List<GisData> search(String keyword) {
        // 暂时返回模拟数据
        List<GisData> allData = new ArrayList<>();
        allData.addAll(getPolicePoints());
        allData.addAll(getMonitorPoints());
        allData.addAll(getAlarmPoints());
        allData.addAll(getAddressPoints());
        
        List<GisData> result = new ArrayList<>();
        for (GisData data : allData) {
            if (data.getName().contains(keyword) || data.getDescription().contains(keyword)) {
                result.add(data);
            }
        }
        return result;
    }
}
