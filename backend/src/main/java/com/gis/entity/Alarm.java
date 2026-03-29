package com.gis.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity(name = "t_alarm")
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String alarm_id;
    private String alarm_phone;
    private LocalDateTime alarm_time;
    private String alarm_location;
    private String case_description;
    private String handling_result;
    private double lon;
    private double lat;
    private String alarm_type;
    private Integer alarm_level;
    private String status;
    private Integer police_point_id;
    private Integer camera_id;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    // 无参构造函数
    public Alarm() {
    }

    // 自动填充时间
    @PrePersist
    protected void onCreate() {
        create_time = LocalDateTime.now();
        update_time = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        update_time = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlarm_id() {
        return alarm_id;
    }

    public void setAlarm_id(String alarm_id) {
        this.alarm_id = alarm_id;
    }

    public String getAlarm_phone() {
        return alarm_phone;
    }

    public void setAlarm_phone(String alarm_phone) {
        this.alarm_phone = alarm_phone;
    }

    public LocalDateTime getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(LocalDateTime alarm_time) {
        this.alarm_time = alarm_time;
    }

    public String getAlarm_location() {
        return alarm_location;
    }

    public void setAlarm_location(String alarm_location) {
        this.alarm_location = alarm_location;
    }

    public String getCase_description() {
        return case_description;
    }

    public void setCase_description(String case_description) {
        this.case_description = case_description;
    }

    public String getHandling_result() {
        return handling_result;
    }

    public void setHandling_result(String handling_result) {
        this.handling_result = handling_result;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getAlarm_type() {
        return alarm_type;
    }

    public void setAlarm_type(String alarm_type) {
        this.alarm_type = alarm_type;
    }

    public Integer getAlarm_level() {
        return alarm_level;
    }

    public void setAlarm_level(Integer alarm_level) {
        this.alarm_level = alarm_level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPolice_point_id() {
        return police_point_id;
    }

    public void setPolice_point_id(Integer police_point_id) {
        this.police_point_id = police_point_id;
    }

    public Integer getCamera_id() {
        return camera_id;
    }

    public void setCamera_id(Integer camera_id) {
        this.camera_id = camera_id;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }
}
