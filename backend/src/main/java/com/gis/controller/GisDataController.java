package com.gis.controller;

import com.gis.entity.GisData;
import com.gis.service.GisDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gis")
public class GisDataController {

    @Autowired
    private GisDataService gisDataService;

    @GetMapping
    public ResponseEntity<List<GisData>> getAllGisData() {
        List<GisData> gisDataList = gisDataService.getAllGisData();
        return new ResponseEntity<>(gisDataList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GisData> getGisDataById(@PathVariable @NonNull Long id) {
        return gisDataService.getGisDataById(id)
                .map(gisData -> new ResponseEntity<>(gisData, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<GisData> createGisData(@RequestBody @NonNull GisData gisData) {
        GisData savedGisData = gisDataService.saveGisData(gisData);
        return new ResponseEntity<>(savedGisData, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GisData> updateGisData(@PathVariable @NonNull Long id, @RequestBody @NonNull GisData gisData) {
        return gisDataService.getGisDataById(id)
                .map(existingGisData -> {
                    gisData.setId(id);
                    GisData updatedGisData = gisDataService.saveGisData(gisData);
                    return new ResponseEntity<>(updatedGisData, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGisData(@PathVariable @NonNull Long id) {
        if (gisDataService.getGisDataById(id).isPresent()) {
            gisDataService.deleteGisData(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 新增端点：获取警务点数据
    @GetMapping("/police")
    public ResponseEntity<List<GisData>> getPolicePoints() {
        List<GisData> policePoints = gisDataService.getPolicePoints();
        return new ResponseEntity<>(policePoints, HttpStatus.OK);
    }

    // 新增端点：获取监控点数据
    @GetMapping("/monitor")
    public ResponseEntity<List<GisData>> getMonitorPoints() {
        List<GisData> monitorPoints = gisDataService.getMonitorPoints();
        return new ResponseEntity<>(monitorPoints, HttpStatus.OK);
    }

    // 新增端点：获取警情信息数据
    @GetMapping("/alarm")
    public ResponseEntity<List<GisData>> getAlarmPoints() {
        List<GisData> alarmPoints = gisDataService.getAlarmPoints();
        return new ResponseEntity<>(alarmPoints, HttpStatus.OK);
    }

    // 新增端点：获取地址数据
    @GetMapping("/address")
    public ResponseEntity<List<GisData>> getAddressPoints() {
        List<GisData> addressPoints = gisDataService.getAddressPoints();
        return new ResponseEntity<>(addressPoints, HttpStatus.OK);
    }
    
    // 新增端点：按类型获取数据
    @GetMapping("/type/{type}")
    public ResponseEntity<List<GisData>> getByType(@PathVariable @NonNull String type) {
        List<GisData> dataList = gisDataService.getByType(type);
        return new ResponseEntity<>(dataList, HttpStatus.OK);
    }
    
    // 新增端点：搜索功能
    @GetMapping("/search")
    public ResponseEntity<List<GisData>> search(@RequestParam @NonNull String keyword) {
        List<GisData> result = gisDataService.search(keyword);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
