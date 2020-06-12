package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.CaseTypeBean;
import com.hnf.honeycomb.bean.UserLicenseBean;

import java.util.List;
import java.util.Map;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/17 14:57
 */
public interface UserAuthMapper {

    /**
     * 添加用户案件标签库权限
     *
     * @param userId
     * @param caseTypeIds
     */
    void addUserCaseType(Integer userId, List<Integer> caseTypeIds);

    /**
     * // 删除用户专项案件标签库
     *
     * @param userId
     */
    void deleteUserCaseType(Integer userId);

    /**
     * 查询用户具备的专项标签库类型
     *
     * @param userId
     * @return
     */
    List<CaseTypeBean> findCaseTypeByUserId(Integer userId);

    /**
     * 查询用户具备的专项标签库类型Ids
     *
     * @param userId
     * @return
     */
    List<Integer> findCaseTypeIdsByUserId(Integer userId);

    /**
     * 修改用户具备的专项标签库类型
     *
     * @param userId
     * @param caseTypeIds
     */
    void updateUserCaseType(Integer userId, List<Integer> caseTypeIds);

    /**
     * 添加用户临时权限
     *
     * @param userLicenseBean
     */
    void addUserLicense(Map<String, Object> userLicenseBean);

    /**
     * 修改
     *
     * @param userLicenseBean
     */
    void updateUserLicense(UserLicenseBean userLicenseBean);

    /**
     * 删除
     *
     * @param id
     */
    void deleteUserLicense(Integer id);
    /**
     * 删除
     *
     * @param id
     */
    void deleteUserLicenseByUserId(Integer id);

    /**
     * 查询
     *
     * @param userId
     * @return
     */
    List<UserLicenseBean> findUserLicensesByUserId(Long userId);

    /**
     * 查询
     *
     * @param departmentCode
     * @return
     */
    List<UserLicenseBean> findUserLicensesByUserUnitCode(String departmentCode);

    /**
     * 查询
     *
     * @param id
     * @return
     */
    UserLicenseBean findUserLicensesById(Integer id);
}
