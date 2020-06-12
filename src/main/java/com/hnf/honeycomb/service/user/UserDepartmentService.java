package com.hnf.honeycomb.service.user;


import com.hnf.honeycomb.bean.DepartmentBean;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
public interface UserDepartmentService {
    /**
     * 通过部门ID获取部门详情
     *
     * @param departmentId
     * @return
     */
    DepartmentBean findByDepartmentId(Long departmentId);

    /**
     * 通过部门ID删除部门
     *
     * @param departmentId
     * @return
     */
    Map<String, Object> delete(Long departmentId);

    /**
     * 通过部门名称查询单位
     *
     * @param name     部门名称
     * @param unitType 单位编码
     * @return
     */
    DepartmentBean findByName(String name, Long unitType);

    /**
     * 通过部门的单位代码获取对应的单位全名
     *
     * @param departmentCode
     * @return
     */
    String findWholeDepartmentNameByDepartCode(String departmentCode);

    /**
     * 获取父级单位
     *
     * @param @param departmentCode 单位代码
     * @return List
     * @throws
     * @Title: findParentDepart
     * @Description: TODO 查询该单位的全部上级单位
     */
    List<DepartmentBean> findParentDepart(String departmentCode);

    /**
     * 添加部门
     *
     * @param departmentName
     * @param departmentCode
     * @param parentDepartmentCode
     * @return
     */
    Map<String, Object> add(String departmentName, String departmentCode, String parentDepartmentCode);

    /**
     * 修改部门
     *
     * @param departmentId
     * @param departmentName
     * @param newDepartmentCode
     * @return
     */
    Map<String, Object> update(Long departmentId, String departmentName, String newDepartmentCode);

    /**
     * 通过
     *
     * @param parentDepartmentCode
     * @return
     */
    Map<String, Object> delete(String parentDepartmentCode);

    /**
     * 通过code获取部门详情
     *
     * @param departmentCode
     * @return
     */
    DepartmentBean findByCode(String departmentCode);

    /**
     * 获取所有下级单位
     *
     * @param departmentCoede
     * @return
     */
    List<DepartmentBean> findChildDepartmentByCode(String departmentCoede);

    /**
     * 解析文件，获取文件路径,并批量新增单位
     *
     * @param file 文件
     * @return
     */
    String insertBatch(File file);

    /**
     * 查询省列表
     *
     * @return
     */
    List<DepartmentBean> findProvince();

    /**
     * 根据父code跟关键词进行查询
     * @param map departmentCode
     * @param map keyWord 查询的关键词
     * @return
     */
    List<DepartmentBean> findListByCodeAndWord(Map<String, String> map);
    /**
     * 通过code获取部门名称
     *
     * @param departmentCode
     * @return
     */
    String findByDepartmentCode(String departmentCode);
}
