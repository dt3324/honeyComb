package com.hnf.honeycomb.bean;

import com.hnf.honeycomb.bean.enumerations.DepartmentType;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author hnf
 */
public class DepartmentBean implements Serializable {

    private static final long serialVersionUID = -205034799093733111L;

    private Long departmentId;

    private String departmentName;

    private String departmentCode;

    private DepartmentType departmentType;


    public DepartmentBean(String departmentName, String departmentCode, DepartmentType departmentType) {
        super();
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.departmentType = departmentType;
    }

    public DepartmentBean(Long departmentId, String departmentName, String departmentCode, DepartmentType departmentType) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.departmentType = departmentType;
    }

    public DepartmentBean() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DepartmentBean that = (DepartmentBean) o;
        return Objects.equals(departmentId, that.departmentId) &&
                Objects.equals(departmentName, that.departmentName) &&
                Objects.equals(departmentCode, that.departmentCode) &&
                departmentType == that.departmentType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"departmentId\":")
                .append(departmentId);
        sb.append(",\"departmentName\":\"")
                .append(departmentName).append('\"');
        sb.append(",\"departmentCode\":\"")
                .append(departmentCode).append('\"');
        sb.append(",\"departmentType\":")
                .append(departmentType);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, departmentName, departmentCode, departmentType);
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public DepartmentBean setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public DepartmentBean setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public DepartmentBean setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public DepartmentType getDepartmentType() {
        return departmentType;
    }

    public DepartmentBean setDepartmentType(DepartmentType departmentType) {
        this.departmentType = departmentType;
        return this;
    }

}
