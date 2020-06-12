package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.DepartmentBean;

/**
 * 部门持久层
 *
 * @author zhouhong
 */
public interface BusinessDepartmentMapper {

    /**
     * 通过部门代码获取对应的单位名称
     *
     * @param departNum 部门代码
     * @return
     */
    String findDepartNameByDepartNum(String departNum);

    /**
     * 通过单位编码查询单位信息
     *
     * @param departmentCode 单位编码
     * @return
     */
    DepartmentBean findByCode(String departmentCode);
}
