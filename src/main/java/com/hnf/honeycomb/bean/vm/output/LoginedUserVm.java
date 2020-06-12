package com.hnf.honeycomb.bean.vm.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hnf.honeycomb.bean.CaseTypeBean;
import com.hnf.honeycomb.bean.UserLicenseBean;
import lombok.Data;

import java.util.List;

/**
 * @author admin
 */
@Data
public class LoginedUserVm {

    private String softDogNumber;
    private Integer roleId;
    private String roleName;
    private Integer fkDepartmentType;
    private String phoneNumber;
    private String remark;
    private String token;
    @JsonProperty("idNumber")
    private String idNumber;
    private Long lastActivityTime;
    private String vPhone;
    @JsonProperty("user_id")
    private Integer userId;
    private String policeNumber;
    private String departmentName;
    private String nickname;
    private Integer activate;
    private String username;
    private String departmentCode;
    private List<CaseTypeBean> caseTypeBeans;
    private List<UserLicenseBean> userLicenseBeans;

    public String getSoftDogNumber() {
        return softDogNumber;
    }

    public LoginedUserVm setSoftDogNumber(String softDogNumber) {
        this.softDogNumber = softDogNumber;
        return this;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public LoginedUserVm setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public LoginedUserVm setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public Integer getFkDepartmentType() {
        return fkDepartmentType;
    }

    public LoginedUserVm setFkDepartmentType(Integer fkDepartmentType) {
        this.fkDepartmentType = fkDepartmentType;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LoginedUserVm setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public LoginedUserVm setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getToken() {
        return token;
    }

    public LoginedUserVm setToken(String token) {
        this.token = token;
        return this;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public LoginedUserVm setIdNumber(String idNumber) {
        this.idNumber = idNumber;
        return this;
    }

    public Long getLastActivityTime() {
        return lastActivityTime;
    }

    public LoginedUserVm setLastActivityTime(Long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
        return this;
    }

    public String getvPhone() {
        return vPhone;
    }

    public LoginedUserVm setvPhone(String vPhone) {
        this.vPhone = vPhone;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public LoginedUserVm setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public LoginedUserVm setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public LoginedUserVm setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public LoginedUserVm setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Integer getActivate() {
        return activate;
    }

    public LoginedUserVm setActivate(Integer activate) {
        this.activate = activate;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public LoginedUserVm setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public LoginedUserVm setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public List<CaseTypeBean> getCaseTypeBeans() {
        return caseTypeBeans;
    }

    public LoginedUserVm setCaseTypeBeans(List<CaseTypeBean> caseTypeBeans) {
        this.caseTypeBeans = caseTypeBeans;
        return this;
    }

    public List<UserLicenseBean> getUserLicenseBeans() {
        return userLicenseBeans;
    }

    public LoginedUserVm setUserLicenseBeans(List<UserLicenseBean> userLicenseBeans) {
        this.userLicenseBeans = userLicenseBeans;
        return this;
    }
}
