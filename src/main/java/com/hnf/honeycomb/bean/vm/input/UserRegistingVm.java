package com.hnf.honeycomb.bean.vm.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author admin
 */
public class UserRegistingVm {

    private String name;

    private String password;

    private String nick;

    private String confirm;

    private Integer roleid;

    private String token;

    private String departmentCode;

    private String policeNumber;

    private String createUser;

    @JsonProperty("IDnumber")
    private String IDnumber;

    private String remark;

    private String phonenumber;

    private String vphone;

    private Integer softDogNumber;

    private Integer activate;

    private Long managerRoleId;

    private List<Integer> caseTypeIds;

    public Long getManagerRoleId() {
        return managerRoleId;
    }

    public void setManagerRoleId(Long managerRoleId) {
        this.managerRoleId = managerRoleId;
    }

    public UserRegistingVm() {
    }

    public String getCreateUser() {
        return createUser;
    }

    public UserRegistingVm setCreateUser(String createUser) {
        this.createUser = createUser;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public Integer getRoleid() {
        return roleid;
    }

    public void setRoleid(Integer roleid) {
        this.roleid = roleid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public void setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
    }

    public String getIDnumber() {
        return IDnumber;
    }

    public void setIDnumber(String idNumber) {
        this.IDnumber = idNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getVphone() {
        return vphone;
    }

    public void setVphone(String vphone) {
        this.vphone = vphone;
    }

    public Integer getSoftDogNumber() {
        return softDogNumber;
    }

    public void setSoftDogNumber(Integer softDogNumber) {
        this.softDogNumber = softDogNumber;
    }

    public Integer getActivate() {
        return activate;
    }

    public List<Integer> getCaseTypeIds() {
        return caseTypeIds;
    }

    public UserRegistingVm setCaseTypeIds(List<Integer> caseTypeIds) {
        this.caseTypeIds = caseTypeIds;
        return this;
    }

    public void setActivate(Integer activate) {
        this.activate = activate;
    }
}
