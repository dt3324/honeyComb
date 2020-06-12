package com.hnf.honeycomb.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author admin
 */
public class RoleBean implements Serializable {
    private static final long serialVersionUID = -205034799093733118L;

    private Integer roleId;

    private String roleName;

    private Long userId;

    private List<OperationBean> operations;

    public RoleBean() {
    }

    public RoleBean(String roleName, Long unitId) {
        this.roleName = roleName;
        this.userId = unitId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public RoleBean setRoleId(Integer roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public RoleBean setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OperationBean> getOperations() {
        return operations;
    }

    public RoleBean setOperations(List<OperationBean> operations) {
        this.operations = operations;
        return this;
    }
}
