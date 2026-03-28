package com.gis.service;

import com.gis.entity.GisData;
import com.gis.entity.PolicePoint;
import com.gis.entity.Camera;
import com.gis.entity.Alarm;
import com.gis.entity.Address;
import com.gis.repository.GisDataRepository;
import com.gis.repository.PolicePointRepository;
import com.gis.repository.CameraRepository;
import com.gis.repository.AlarmRepository;
import com.gis.repository.AddressRepository;
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
    
    @Autowired
    private PolicePointRepository policePointRepository;
    
    @Autowired
    private CameraRepository cameraRepository;
    
    @Autowired
    private AlarmRepository alarmRepository;
    
    @Autowired
    private AddressRepository addressRepository;

    public List<GisData> getAllGisData() {
        List<GisData> allData = new ArrayList<>();
        allData.addAll(getPolicePoints());
        allData.addAll(getMonitorPoints());
        allData.addAll(getAlarmPoints());
        allData.addAll(getAddressPoints());
        return allData;
    }

    public Optional<GisData> getGisDataById(@NonNull Long id) {
        return gisDataRepository.findById(id);
    }

    public GisData saveGisData(@NonNull GisData gisData) {
        return gisDataRepository.save(gisData);
    }

    public void deleteGisData(@NonNull Long id) {
        gisDataRepository.deleteById(id);
    }

    // 新增方法：获取警务点数据
    public List<GisData> getPolicePoints() {
        List<GisData> policePoints = new ArrayList<>();
        List<PolicePoint> points = policePointRepository.findAll();
        for (PolicePoint point : points) {
            GisData data = new GisData();
            data.setId(point.getId());
            data.setName(point.getName());
            data.setDescription(point.getType() + " - " + point.getResponsibility_unit());
            data.setLatitude(point.getLat());
            data.setLongitude(point.getLon());
            data.setType("police");
            policePoints.add(data);
        }
        return policePoints;
    }

    // 新增方法：获取监控点数据
    public List<GisData> getMonitorPoints() {
        List<GisData> monitorPoints = new ArrayList<>();
        List<Camera> cameras = cameraRepository.findAll();
        for (Camera camera : cameras) {
            GisData data = new GisData();
            data.setId(camera.getId());
            data.setName(camera.getName());
            data.setDescription(camera.getCamera_type() + " - " + camera.getDistrict());
            data.setLatitude(camera.getLat());
            data.setLongitude(camera.getLon());
            data.setType("monitor");
            monitorPoints.add(data);
        }
        return monitorPoints;
    }

    // 新增方法：获取警情信息数据
    public List<GisData> getAlarmPoints() {
        List<GisData> alarmPoints = new ArrayList<>();
        List<Alarm> alarms = alarmRepository.findAll();
        for (Alarm alarm : alarms) {
            GisData data = new GisData();
            data.setId(alarm.getId());
            data.setName(alarm.getAlarm_id());
            data.setDescription(alarm.getAlarm_type() + " - " + alarm.getStatus());
            data.setLatitude(alarm.getLat());
            data.setLongitude(alarm.getLon());
            data.setType("alarm");
            alarmPoints.add(data);
        }
        return alarmPoints;
    }

    // 新增方法：获取地址数据
    public List<GisData> getAddressPoints() {
        List<GisData> addressPoints = new ArrayList<>();
        List<Address> addresses = addressRepository.findAll();
        for (Address address : addresses) {
            GisData data = new GisData();
            data.setId(address.getId());
            data.setName(address.getAddress_full());
            data.setDescription(address.getStreet() + " " + address.getHouse_number());
            data.setLatitude(address.getLat());
            data.setLongitude(address.getLon());
            data.setType("address");
            addressPoints.add(data);
        }
        return addressPoints;
    }
    
    // 按类型获取数据
    public List<GisData> getByType(String type) {
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
                return new ArrayList<>();
        }
    }
    
    // 搜索功能
    public List<GisData> search(String keyword) {
        List<GisData> allData = getAllGisData();
        List<GisData> result = new ArrayList<>();
        for (GisData data : allData) {
            if (data.getName().contains(keyword) || data.getDescription().contains(keyword)) {
                result.add(data);
            }
        }
        return result;
    }
}
