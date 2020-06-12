package com.hnf.honeycomb.bean.vm.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author admin
 */
public class UserUpdateVm {
    private Integer userId;
    private String password;
    private String nickname;
    private Integer roleId;
    private String departmentCode;
    private String remark;
    @JsonProperty("IDnumber")
    private String IDnumber;
    private String phonenumber;
    private String vphone;
    private Integer softDog;
    private List<Integer> caseTypeIds;

    public UserUpdateVm() {
    }

    public UserUpdateVm(Integer userId, String password, String nickname, Integer roleId, String departmentCode, String remark, String IDnumber, String phonenumber, String vphone, Integer softDog) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.roleId = roleId;
        this.departmentCode = departmentCode;
        this.remark = remark;
        this.IDnumber = IDnumber;
        this.phonenumber = phonenumber;
        this.vphone = vphone;
        this.softDog = softDog;
    }

    public String getIDnumber() {
        return IDnumber;
    }

    public void setIDnumber(String IDnumber) {
        this.IDnumber = IDnumber;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getRemark() {
        return remark;
    }


    public String getPhonenumber() {
        return phonenumber;
    }

    public String getVphone() {
        return vphone;
    }

    public Integer getSoftDog() {
        return softDog;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setVphone(String vphone) {
        this.vphone = vphone;
    }

    public void setSoftDog(Integer softDog) {
        this.softDog = softDog;
    }

    public List<Integer> getCaseTypeIds() {
        return caseTypeIds;
    }

    public UserUpdateVm setCaseTypeIds(List<Integer> caseTypeIds) {
        this.caseTypeIds = caseTypeIds;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"userId\":")
                .append(userId);
        sb.append(",\"password\":\"")
                .append(password).append('\"');
        sb.append(",\"nickname\":\"")
                .append(nickname).append('\"');
        sb.append(",\"roleId\":")
                .append(roleId);
        sb.append(",\"departmentCode\":\"")
                .append(departmentCode).append('\"');
        sb.append(",\"remark\":\"")
                .append(remark).append('\"');
        sb.append(",\"IDnumber\":\"")
                .append(IDnumber).append('\"');
        sb.append(",\"phonenumber\":\"")
                .append(phonenumber).append('\"');
        sb.append(",\"vphone\":\"")
                .append(vphone).append('\"');
        sb.append(",\"softDog\":")
                .append(softDog);
        sb.append(",\"caseTypeIds\":")
                .append(caseTypeIds);
        sb.append('}');
        return sb.toString();
    }
}
