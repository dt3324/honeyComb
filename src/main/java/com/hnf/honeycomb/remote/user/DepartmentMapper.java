package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.DepartmentBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhong
 * @ClassName UserMapper
 * @Description: TODO 部门管理的持久层接口
 * @date 2018年6月26日 上午10：38：10
 */
public interface DepartmentMapper {

    /**
     * 新增部门
     *
     * @param map
     * @return
     */
    void add(Map map);

    /**
     * 删除部门
     *
     * @param departmentId 部门ID
     * @return
     */
    void delete(String departmentId);

    /**
     * 通过部门ID查找部门
     *
     * @param departmentId 部门ID
     * @return
     */
    DepartmentBean findByDepartmentId(Long departmentId);

    /**
     * 修改部门
     *
     * @param map
     * @return
     */
    void update(Map map);

    /**
     * 查询此单位下对应的非此部门id的单位类型是否存在
     *
     * @param map 封装部门信息的map
     * @return
     */
    DepartmentBean findDepartmentIsNotSelf(Map map);

    /**
     * 通过部门名称和单位编码查询部门
     *
     * @param map `departmentName` as 部门名称,`departmentId` as 部门Id
     * @return
     */
    DepartmentBean findByName(Map map);

    /**
     * 通过部门类型编码查询部门信息
     *
     * @param departType 部门类型编码
     * @return
     */
    DepartmentBean findByDtype(Long departType);

    /**
     * @param departmentCode
     * @return
     */
    DepartmentBean findByDepartmentCode(String departmentCode);

    /**
     * 通过部门ID查找部门
     *
     * @param map `departmentCode` as 部门code,`departmentType` as 一组要查询的下一级的departmentType(List)
     * @return
     */
    List<DepartmentBean> findChildDepartmentByCodeAndTypes(Map<String, Object> map);

    /**
     * @param departmentCode
     * @return
     */
    List<DepartmentBean> findDepartmentCountByCode(@Param("departmentCode") String departmentCode);

    /**
     * @param map `unitType` as 单位类型，`departmentType` as 部门类型
     * @return List of DepartmentBeans, 符合检索条件的部门
     */
    List<DepartmentBean> findByUnitTypeOne(Map<String, Object> map);


    /**
     * 通过单位编码查询单位信息
     *
     * @param departmentCode 单位编码
     * @return
     */
    DepartmentBean findByDeCode(String departmentCode);

    /**
     * 通过ID和CODE查询当前单位列表
     *
     * @param beanCode `departmentCode` as 需要匹配的单位code, `departmentId` as 需要排除的单位Id
     * @return 符合条件的单位
     */
    List<DepartmentBean> findNoDepartmentIdCode(Map<String, Object> beanCode);


    /**
     * 批量新增单位
     *
     * @param department 单位信息
     */
    void insertBatch(List<Object> department);

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

    List<String> findDepartmentCodeListByCode(String oldDepartmentCode);
}
