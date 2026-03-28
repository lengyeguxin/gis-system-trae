package com.gis.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity(name = "t_police_point")
public class PolicePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String address;
    private double lon;
    private double lat;
    private String contact_person;
    private String contact_phone;
    private String responsibility_unit;
    private String description;
    private Integer icon_id;
    private String create_by;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

    // 无参构造函数
    public PolicePoint() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getContact_person() {
        return contact_person;
    }

    public void setContact_person(String contact_person) {
        this.contact_person = contact_person;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getResponsibility_unit() {
        return responsibility_unit;
    }

    public void setResponsibility_unit(String responsibility_unit) {
        this.responsibility_unit = responsibility_unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(Integer icon_id) {
        this.icon_id = icon_id;
    }

    public String getCreate_by() {
        return create_by;
    }

    public void setCreate_by(String create_by) {
        this.create_by = create_by;
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
