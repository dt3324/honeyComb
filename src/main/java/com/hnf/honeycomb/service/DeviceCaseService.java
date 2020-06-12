package com.hnf.honeycomb.service;

import java.util.Map;

/**
 * 案件业务层接口
 *
 * @author zhouhong
 */
public interface DeviceCaseService {

    /**
     * 通过对应的条件查询对应的案件信息
     *
     * @param page           查询的页数
     * @param caseName       模糊匹配的案件名称
     * @param departmentCode 部门Code
     * @return 返回对应的信息, 包含案件总数
     */
    Map<String, Object> findCaseByUnitTypeOrDog(Integer page, String caseName, String departmentCode);

    /**
     * 查询案件相关的人员和设备
     *
     * @param policeNumber 人员证件号
     * @param deviceUnique 设备唯一标识
     * @return
     */
    Map queryByUnique(String policeNumber, String deviceUnique);

}
