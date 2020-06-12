package com.hnf.honeycomb.bean.vm.input;

/**
 * @author admin
 */
public class DepartmentCreatingVm {
    private Long managerRoleId;
    private String departmentName;
    private String departmentCode;
    private String parentCode;

    public Long getManagerRoleId() {
        return managerRoleId;
    }

    public void setManagerRoleId(Long managerRoleId) {
        this.managerRoleId = managerRoleId;
    }


    public DepartmentCreatingVm setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public DepartmentCreatingVm setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public DepartmentCreatingVm setParentCode(String parentCode) {
        this.parentCode = parentCode;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getParentCode() {
        return parentCode;
    }

    @Override
    public String toString() {
        return "DepartmentCreatingVm{" +
                "managerRoleId=" + managerRoleId +
                ", departmentName='" + departmentName + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", parentCode='" + parentCode + '\'' +
                '}';
    }
}
