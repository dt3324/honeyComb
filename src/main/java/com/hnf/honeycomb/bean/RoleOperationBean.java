package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author admin
 */
public class RoleOperationBean implements Serializable {

    private static final long serialVersionUID = 8804571264613900046L;
    private Long roleId;

    private Integer operationId;


    public RoleOperationBean() {
        super();
        // TODO Auto-generated constructor stub
    }

    public RoleOperationBean(Long roleId, Integer operationId) {
        super();
        this.roleId = roleId;
        this.operationId = operationId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getOperationId() {
        return operationId;
    }

    public void setOperationId(Integer operationId) {
        this.operationId = operationId;
    }

    @Override
    public String toString() {
        return "OperationRoleBean [roleId=" + roleId + ", operationId=" + operationId + "]";
    }
}
