package com.hnf.honeycomb.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 新建的用于user的实现类
 *
 * @author yy
 */
public class User implements Serializable {
    private static final long serialVersionUID = 6054021737269483424L;

    @JsonProperty("user_id")
    private Integer userId;
    private String username;
    private String password;
    private String nickname;
    private Integer roleId;
    private String roleName;
    private String token;
    private String policeNumber;
    private String createUser;
    private ResidentIdentityCardBean IDnumber;
    private String remark;
    private String phoneNumber;
    private String phone;
    private Integer activate;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Integer departmentType;
    private List<String> canCheckDepartCode;
    private List<DepartmentBean> canCheckDepart;
    private List<Map<String, Object>> canOperation;
    private List<String> softDogNum;
    private String softDogNumber;
    private List<CaseTypeBean> caseTypeBeans;
    private List<UserLicenseBean> userLicenseBeans;

    public List<CaseTypeBean> getCaseTypeBeans() {
        return caseTypeBeans;
    }

    public User setCaseTypeBeans(List<CaseTypeBean> caseTypeBeans) {
        this.caseTypeBeans = caseTypeBeans;
        return this;
    }

    public String getCreateUser() {
        return createUser;
    }

    public User setCreateUser(String createUser) {
        this.createUser = createUser;
        return this;
    }

    /**
     * /判断是否为普通用户,1代表不是,2代表是
     */
    private Integer isNormal;

    public Integer getIsNormal() {
        return isNormal;
    }

    public void setIsNormal(Integer isNormal) {
        this.isNormal = isNormal;
    }

    public List<Map<String, Object>> getCanOperation() {
        return canOperation;
    }

    public void setCanOperation(List<Map<String, Object>> canOperation) {
        this.canOperation = canOperation;
    }

    private List<Object> role;

    public List<Object> getRole() {
        return role;
    }

    public void setRole(List<Object> role) {
        this.role = role;
    }

    public List<String> getSoftDogNum() {
        return softDogNum;
    }

    public void setSoftDogNum(List<String> softDogNum) {
        this.softDogNum = softDogNum;
    }

    public User(Integer userId, String username, String password, String nickname, Integer roleId, String roleName,
                String token, String policenumber, String IDnumberRaw, String remark, String phoneNumber, String phone,
                Integer activate, Long departmentId,String createUser) {
        super();
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.roleId = roleId;
        this.roleName = roleName;
        this.token = token;
        this.policeNumber = policenumber;
        this.IDnumber = ResidentIdentityCardBean.ofRaw(IDnumberRaw);
        this.remark = remark;
        this.phoneNumber = phoneNumber;
        this.phone = phone;
        this.activate = activate;
        this.departmentId = departmentId;
        this.createUser = createUser;
    }

    public List<String> getCanCheckDepartCode() {
        return canCheckDepartCode;
    }

    public void setCanCheckDepartCode(List<String> canCheckDepartCode) {
        this.canCheckDepartCode = canCheckDepartCode;
    }

    public List<DepartmentBean> getCanCheckDepart() {
        return canCheckDepart;
    }

    public void setCanCheckDepart(List<DepartmentBean> canCheckDepart) {
        this.canCheckDepart = canCheckDepart;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public void setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
    }

    public ResidentIdentityCardBean getIDnumber() {
        return IDnumber;
    }

    public void setIDnumber(ResidentIdentityCardBean IDnumber) {
        this.IDnumber = IDnumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getActivate() {
        return activate;
    }

    public void setActivate(Integer activate) {
        this.activate = activate;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Integer getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(Integer departmentType) {
        this.departmentType = departmentType;
    }

    public String getSoftDogNumber() {
        return softDogNumber;
    }

    public void setSoftDogNumber(String softDogNumber) {
        this.softDogNumber = softDogNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    public List<UserLicenseBean> getUserLicenseBeans() {
        return userLicenseBeans;
    }

    public User setUserLicenseBeans(List<UserLicenseBean> userLicenseBeans) {
        this.userLicenseBeans = userLicenseBeans;
        return this;
    }

    public boolean thisUserIsActivated() {
        return Integer.valueOf(1).equals(activate);
    }
}
