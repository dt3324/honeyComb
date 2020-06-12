package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author admin
 */
public class LicenseBean implements Serializable {
    private static final long serialVersionUID = -205034799093733115L;


    private Integer licenseId;

    private String licenseName;

    private String licenseDeptCode;

    public LicenseBean() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LicenseBean(String licenseDeptCode) {
        super();
        this.licenseDeptCode = licenseDeptCode;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseDeptCode() {
        return licenseDeptCode;
    }

    public void setLicenseDeptCode(String licenseDeptCode) {
        this.licenseDeptCode = licenseDeptCode;
    }

    @Override
    public String toString() {
        return "LicenseBean [licenseId=" + licenseId + ", licenseName=" + licenseName + ", licenseDeptCode="
                + licenseDeptCode + "]";
    }


}
