package com.hnf.honeycomb.bean.vm.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hnf.honeycomb.bean.CaseTypeBean;
import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.UserLicenseBean;

import java.util.List;

/**
 * @author admin
 */
public class QueriedUserVm {

    private String softDog;
    private Integer roleId;
    private String roleName;
    private Integer fkDepartmentType;
    private String phoneNumber;
    private String remark;
    private String token;
    @JsonProperty("IDnumber")
    private String idNumber;
    private Long lastActivityTime;
    private String vPhone;
    @JsonProperty("userid")
    private Integer userId;
    private String policeNumber;
    private String departmentName;
    private String nickname;
    private Integer activate;
    private String username;
    private String departmentCode;
    private List<DepartmentBean> regions;
    private List<CaseTypeBean> caseTypeBeans;
    private List<UserLicenseBean> userLicenseBeans;


    public Integer getRoleId() {
        return roleId;
    }

    public QueriedUserVm setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public QueriedUserVm setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public Integer getFkDepartmentType() {
        return fkDepartmentType;
    }

    public QueriedUserVm setFkDepartmentType(Integer fkDepartmentType) {
        this.fkDepartmentType = fkDepartmentType;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public QueriedUserVm setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public QueriedUserVm setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public QueriedUserVm setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getToken() {
        return token;
    }

    public QueriedUserVm setToken(String token) {
        this.token = token;
        return this;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public QueriedUserVm setIdNumber(String idNumber) {
        this.idNumber = idNumber;
        return this;
    }

    public Long getLastActivityTime() {
        return lastActivityTime;
    }

    public QueriedUserVm setLastActivityTime(Long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
        return this;
    }

    public String getvPhone() {
        return vPhone;
    }

    public QueriedUserVm setvPhone(String vPhone) {
        this.vPhone = vPhone;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public QueriedUserVm setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public QueriedUserVm setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
        return this;
    }


    public String getNickname() {
        return nickname;
    }

    public QueriedUserVm setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Integer getActivate() {
        return activate;
    }

    public QueriedUserVm setActivate(Integer activate) {
        this.activate = activate;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public QueriedUserVm setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public QueriedUserVm setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public List<DepartmentBean> getRegions() {
        return regions;
    }

    public QueriedUserVm setRegions(List<DepartmentBean> regions) {
        this.regions = regions;
        return this;
    }

    public String getSoftDog() {
        return softDog;
    }

    public QueriedUserVm setSoftDog(String softDog) {
        this.softDog = softDog;
        return this;
    }

    public List<CaseTypeBean> getCaseTypeBeans() {
        return caseTypeBeans;
    }

    public QueriedUserVm setCaseTypeBeans(List<CaseTypeBean> caseTypeBeans) {
        this.caseTypeBeans = caseTypeBeans;
        return this;
    }

    public List<UserLicenseBean> getUserLicenseBeans() {
        return userLicenseBeans;
    }

    public QueriedUserVm setUserLicenseBeans(List<UserLicenseBean> userLicenseBeans) {
        this.userLicenseBeans = userLicenseBeans;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"softDog\":\"")
                .append(softDog).append('\"');
        sb.append(",\"roleId\":")
                .append(roleId);
        sb.append(",\"roleName\":\"")
                .append(roleName).append('\"');
        sb.append(",\"fkDepartmentType\":")
                .append(fkDepartmentType);
        sb.append(",\"phoneNumber\":\"")
                .append(phoneNumber).append('\"');
        sb.append(",\"remark\":\"")
                .append(remark).append('\"');
        sb.append(",\"token\":\"")
                .append(token).append('\"');
        sb.append(",\"idNumber\":\"")
                .append(idNumber).append('\"');
        sb.append(",\"lastActivityTime\":")
                .append(lastActivityTime);
        sb.append(",\"vPhone\":\"")
                .append(vPhone).append('\"');
        sb.append(",\"userId\":")
                .append(userId);
        sb.append(",\"policeNumber\":\"")
                .append(policeNumber).append('\"');
        sb.append(",\"departmentName\":\"")
                .append(departmentName).append('\"');
        sb.append(",\"nickname\":\"")
                .append(nickname).append('\"');
        sb.append(",\"activate\":")
                .append(activate);
        sb.append(",\"username\":\"")
                .append(username).append('\"');
        sb.append(",\"departmentCode\":\"")
                .append(departmentCode).append('\"');
        sb.append(",\"regions\":")
                .append(regions);
        sb.append(",\"caseTypeBeans\":")
                .append(caseTypeBeans);
        sb.append(",\"userLicenseBeans\":")
                .append(userLicenseBeans);
        sb.append('}');
        return sb.toString();
    }
}
