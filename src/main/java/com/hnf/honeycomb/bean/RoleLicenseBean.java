package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author admin
 */
public class RoleLicenseBean implements Serializable {
    private static final long serialVersionUID = -205034799093733119L;


    private Long roleId;

    private Integer licenseId;


    public RoleLicenseBean() {
        super();
        // TODO Auto-generated constructor stub
    }

    public RoleLicenseBean(Long roleId, Integer licenseId) {
        super();
        this.roleId = roleId;
        this.licenseId = licenseId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    @Override
    public String toString() {
        return "RoleLicenseBean [roleId=" + roleId + ", licenseId=" + licenseId + "]";
    }


}
