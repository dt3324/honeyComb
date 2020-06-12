package com.hnf.honeycomb.mapper;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.FetchLogBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用于加密狗号的的查询即统计修改的相关数据库修改
 *
 * @author yy
 */
public interface SoftDogMapper {

    /**
     * 通过对应的条件查询对应加密狗的所有信息,条件可能包括部门编号,已经采集过得,加密狗号
     *
     * @param param 参数条件
     * @return
     */
    List<Map<String, Object>> findSoftDogDetailBySomeCondition(Map<String, Object> param);

    List<Map<String, Object>> findSoftDogDetailBySomeConditionDist(Map<String, Object> param);

    List<Map<String, Object>> findSoftDogDetailBySomeConditionDistAndPNisNull(Map<String, Object> param);

    int findSoftDogDetailCount(Map<String, Object> param);

    List<String> findFetchDetailBySomeCondition(Map<String,Object> param);

    List<Map<String,Object>> findPoliceNumberListByDepartmentCode(Map<String,Object> param);

    List<Map<String, Object>> findChildrenListByDepartmentCode(Map<String,Object> param);

    List<Map<String,String>> findChildDepartmentByCode(Map<String,Object> param);

    List<String> findDeparts(Map<String, Object> policeNumber);

    List<Map<String, Object>> findDepartByPoilceNumIsNull(Map<String, Object> para);
}
