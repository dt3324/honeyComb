package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author hnf
 */
public class UserBean implements Serializable {
    private static final long serialVersionUID = 8439984956590439171L;
    private Integer userid;
    private String username;
    private String password;
    private String nickname;
    private Integer roleId;
    private String token;
    private Long departmentType;
    private Long unitType;
    private String policeNumber;
    private String idNumber;
    private String remark;
    private String phoneNumber;
    private String vPhone;
    private Integer activate;
    private Integer departmentId;
    private String unitName;
    private String departmentName;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"userid\":")
                .append(userid);
        sb.append(",\"username\":\"")
                .append(username).append('\"');
        sb.append(",\"password\":\"")
                .append(password).append('\"');
        sb.append(",\"nickname\":\"")
                .append(nickname).append('\"');
        sb.append(",\"roleId\":")
                .append(roleId);
        sb.append(",\"token\":\"")
                .append(token).append('\"');
        sb.append(",\"departmentType\":")
                .append(departmentType);
        sb.append(",\"unitType\":")
                .append(unitType);
        sb.append(",\"policeNumber\":\"")
                .append(policeNumber).append('\"');
        sb.append(",\"idNumber\":\"")
                .append(idNumber).append('\"');
        sb.append(",\"remark\":\"")
                .append(remark).append('\"');
        sb.append(",\"phoneNumber\":\"")
                .append(phoneNumber).append('\"');
        sb.append(",\"vPhone\":\"")
                .append(vPhone).append('\"');
        sb.append(",\"activate\":")
                .append(activate);
        sb.append(",\"departmentId\":")
                .append(departmentId);
        sb.append(",\"unitName\":\"")
                .append(unitName).append('\"');
        sb.append(",\"departmentName\":\"")
                .append(departmentName).append('\"');
        sb.append('}');
        return sb.toString();
    }

    public UserBean() {
    }

    public Integer getUserid() {
        return userid;
    }

    public UserBean setUserid(Integer userid) {
        this.userid = userid;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserBean setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserBean setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public UserBean setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public UserBean setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getToken() {
        return token;
    }

    public UserBean setToken(String token) {
        this.token = token;
        return this;
    }

    public Long getDepartmentType() {
        return departmentType;
    }

    public UserBean setDepartmentType(Long departmentType) {
        this.departmentType = departmentType;
        return this;
    }

    public Long getUnitType() {
        return unitType;
    }

    public UserBean setUnitType(Long unitType) {
        this.unitType = unitType;
        return this;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public UserBean setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
        return this;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public UserBean setIdNumber(String idNumber) {
        this.idNumber = idNumber;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public UserBean setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public UserBean setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getvPhone() {
        return vPhone;
    }

    public UserBean setvPhone(String vPhone) {
        this.vPhone = vPhone;
        return this;
    }

    public Integer getActivate() {
        return activate;
    }

    public UserBean setActivate(Integer activate) {
        this.activate = activate;
        return this;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public UserBean setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public String getUnitName() {
        return unitName;
    }

    public UserBean setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public UserBean setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }
}
