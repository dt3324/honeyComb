package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;

/**
 * @author hnf
 */
public interface ImpactCaseService {

    /**
     * 通过案件名模糊查询案件
     *
     * @param caseName
     * @param departmentCode
     * @return List<Document>
     */
    List<Document> findCaseByCaseName( String departmentCode,String caseName);

    /**
     * 通过案件名查询关联的设备名
     *
     * @param query
     * @return List<Document>
     * @throws
     * @Title: findDeviceByCaseName
     */
    List<Document> findDeviceByCaseName(String query);

    /**
     * 通过人员姓名模糊匹配人员
     *
     * @param unitCode       用户所在部门Code
     * @param nameOrIdNumber 姓名或证件号
     * @return
     */
    List<Document> findPersonByPersonName(String unitCode, String nameOrIdNumber);


    /**
     * 通过人员姓名模糊匹配人员
     *
     * @param unitCode       用户所在部门Code
     * @param nameOrIdNumber 姓名或证件号
     * @return
     */
    List<Document> findPersonByPhone(String unitCode, String nameOrIdNumber);


}
