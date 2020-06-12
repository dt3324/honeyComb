package com.hnf.honeycomb.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/17 11:46
 */
@Data
public class UserLicenseBean implements Serializable {
    private static final long serialVersionUID = 7688313691048061289L;
    private Integer id;
    private Long userId;
    private String policeNumber;
    private String departmentCode;
    private String departmentName;
    private Long expireTime;

    public String getPoliceNumber() {
        return policeNumber;
    }

    public UserLicenseBean setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public UserLicenseBean setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public UserLicenseBean setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public UserLicenseBean setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public UserLicenseBean setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public UserLicenseBean setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

}
