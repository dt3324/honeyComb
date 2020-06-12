package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.CaseTypeBean;
import com.hnf.honeycomb.bean.UserLicenseBean;
import com.hnf.honeycomb.remote.user.UserAuthMapper;
import com.hnf.honeycomb.util.BuilderMap;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/17 14:58
 */
@Repository
@EnableCaching
public class UserAuthMapperImpl implements UserAuthMapper {
    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void addUserCaseType(Integer userId, List<Integer> caseTypeIds) {
        userCall("caseType/addUserCaseType"
                , BuilderMap.of(String.class, Object.class).put("userId", userId).put("caseTypeIds", caseTypeIds).get()
                , String.class);
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void deleteUserCaseType(Integer userId) {
        userCall("caseType/deleteUserCaseType"
                , BuilderMap.of("userId", userId).get()
                , String.class);
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<CaseTypeBean> findCaseTypeByUserId(Integer userId) {
        return userCall("caseType/findCaseTypeByUserId"
                , BuilderMap.of("userId", userId).get()
                , new ParameterizedTypeReference<List<CaseTypeBean>>() {
                });
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<Integer> findCaseTypeIdsByUserId(Integer userId) {
        return userCall("caseType/findCaseTypeIdsByUserId"
                , BuilderMap.of("userId", userId).get()
                , new ParameterizedTypeReference<List<Integer>>() {
                });
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateUserCaseType(Integer userId, List<Integer> caseTypeIds) {
        userCall("caseType/updateUserCaseType"
                , BuilderMap.of(String.class, Object.class).put("userId", userId).put("caseTypeIds", caseTypeIds).get()
                , String.class);
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void addUserLicense(Map<String, Object> userLicenseBean) {
        userCall("userLicense/addUserLicense", userLicenseBean, String.class);
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void updateUserLicense(UserLicenseBean userLicenseBean) {
        userCall("userLicense/updateUserLicense", userLicenseBean, String.class);
    }

    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void deleteUserLicense(Integer id) {
        userCall("userLicense/deleteUserLicense", BuilderMap.of("id", id).get(), String.class);
    }
    @Override
    @CacheEvict(value = "userRemote", allEntries = true)
    public void deleteUserLicenseByUserId(Integer id) {
        userCall("userLicense/deleteUserLicenseByUserId", BuilderMap.of("id", id).get(), String.class);
    }

    @Override
    public List<UserLicenseBean> findUserLicensesByUserId(Long userId) {
        return userCall("userLicense/findUserLicensesByUserId"
                , BuilderMap.of("userId", userId).get()
                , new ParameterizedTypeReference<List<UserLicenseBean>>() {
                });
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public List<UserLicenseBean> findUserLicensesByUserUnitCode(String departmentCode) {
        return userCall("userLicense/findUserLicensesByUserUnitCode"
                , BuilderMap.of("departmentCode", departmentCode).get()
                , new ParameterizedTypeReference<List<UserLicenseBean>>() {
                });
    }

    @Override
    @Cacheable(value = "userRemote", keyGenerator = "keyGenerator")
    public UserLicenseBean findUserLicensesById(Integer id) {
        return userCall("userLicense/findUserLicensesById"
                , BuilderMap.of("id", id).get()
                , new ParameterizedTypeReference<UserLicenseBean>() {
                });
    }
}
