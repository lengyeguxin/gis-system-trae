package com.gis.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(name = "real_name")
    private String realName;
    @Column(name = "certificate_dn")
    private String certificateDn;
    private String role;
    private String password;
    @Column(name = "last_login_time")
    private Timestamp lastLoginTime;
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Timestamp createTime;
    @UpdateTimestamp
    @Column(name = "update_time")
    private Timestamp updateTime;

    // 无参构造函数
    public User() {
    }

    // 带参数的构造函数
    public User(String username, String password, String role, String realName, String certificateDn) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.realName = realName;
        this.certificateDn = certificateDn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCertificateDn() {
        return certificateDn;
    }

    public void setCertificateDn(String certificateDn) {
        this.certificateDn = certificateDn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
