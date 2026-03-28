package com.gis.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity(name = "t_camera")
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String camera_no;
    private String camera_type;
    private String address;
    private String ip_address;
    private String rtsp_url;
    private double lon;
    private double lat;
    private Boolean online_status;
    private String responsibility_unit;
    private Integer icon_id;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    // 无参构造函数
    public Camera() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCamera_no() {
        return camera_no;
    }

    public void setCamera_no(String camera_no) {
        this.camera_no = camera_no;
    }

    public String getCamera_type() {
        return camera_type;
    }

    public void setCamera_type(String camera_type) {
        this.camera_type = camera_type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getRtsp_url() {
        return rtsp_url;
    }

    public void setRtsp_url(String rtsp_url) {
        this.rtsp_url = rtsp_url;
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

    public Boolean getOnline_status() {
        return online_status;
    }

    public void setOnline_status(Boolean online_status) {
        this.online_status = online_status;
    }

    public String getResponsibility_unit() {
        return responsibility_unit;
    }

    public void setResponsibility_unit(String responsibility_unit) {
        this.responsibility_unit = responsibility_unit;
    }

    public Integer getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(Integer icon_id) {
        this.icon_id = icon_id;
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

    @PrePersist
    protected void onCreate() {
        create_time = LocalDateTime.now();
        update_time = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        update_time = LocalDateTime.now();
    }
}
