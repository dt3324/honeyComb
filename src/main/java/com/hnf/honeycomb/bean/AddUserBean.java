package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * Excel 批量添加用户Bean
 *
 * @author lsj
 * @date 2018/917
 */
public class AddUserBean implements Serializable {
    private static final long serialVersionUID = -205034799093733113L;

    private String policeNum;
    private String name;
    private String idNum;
    private String phoneNum;
    private Integer roleId;
    private Long departmentId;
    private String dogNum;
    private String password;
    private Integer active;

    public AddUserBean(String policeNum, String name, String idNum, String phoneNum, Integer roleId, Long departmentId, String dogNum, String password, Integer active) {
        this.policeNum = policeNum;
        this.name = name;
        this.idNum = idNum;
        this.phoneNum = phoneNum;
        this.roleId = roleId;
        this.departmentId = departmentId;
        this.dogNum = dogNum;
        this.password = password;
        this.active = active;
    }

    @Override
    public String toString() {
        return "AddUserBean{" +
                "policeNum='" + policeNum + '\'' +
                ", name='" + name + '\'' +
                ", idNum='" + idNum + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", roleId=" + roleId +
                ", departmentId=" + departmentId +
                ", dogNum='" + dogNum + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                '}';
    }

    public String getPoliceNum() {
        return policeNum;
    }

    public void setPoliceNum(String policeNum) {
        this.policeNum = policeNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDogNum() {
        return dogNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public void setDogNum(String dogNum) {
        this.dogNum = dogNum;
    }
}
