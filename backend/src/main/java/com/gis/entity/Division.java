package com.gis.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;

@Entity(name = "regions")
public class Division {

    @Id
    private Long id;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    private String name;
    
    private String pinyin;
    
    @Column(name = "pinyin_prefix")
    private String pinyinPrefix;
    
    private Integer level;

    // 无参构造函数
    public Division() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinyinPrefix() {
        return pinyinPrefix;
    }

    public void setPinyinPrefix(String pinyinPrefix) {
        this.pinyinPrefix = pinyinPrefix;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}