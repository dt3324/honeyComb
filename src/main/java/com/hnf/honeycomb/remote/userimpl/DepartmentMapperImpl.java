package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:13
 */
@Repository
@EnableCaching
public class DepartmentMapperImpl implements DepartmentMapper {


    @Override
    @CacheEvict(value = "department", allEntries = true)
    public void add(Map map) {
        userCall("department/add"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "department", allEntries = true)
    public void delete(String departmentId) {
        userCall("department/delete"
                , ofMap("departmentId", departmentId)
                , String.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findByDepartmentId(Long departmentId) {
        return userCall("department/findByDepartmentId"
                , ofMap("departmentId", departmentId)
                , DepartmentBean.class
        );
    }

    @Override
    @CacheEvict(value = "department", allEntries = true)
    public void update(Map map) {
        userCall("department/update"
                , map
                , String.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findDepartmentIsNotSelf(Map map) {
        return userCall("department/findDepartmentIsNotSelf"
                , map
                , DepartmentBean.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findByName(Map map) {
        return userCall("department/findByName"
                , map
                , DepartmentBean.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findByDtype(Long departType) {
        return userCall("department/findByDtype"
                , ofMap("departType", departType)
                , DepartmentBean.class
        );
    }

    @Override
//    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findByDepartmentCode(String departmentCode) {
        return userCall("department/findByDepartmentCode"
                , ofMap("departmentCode", departmentCode)
                , DepartmentBean.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public List<DepartmentBean> findChildDepartmentByCodeAndTypes(Map<String, Object> map) {
        return userCall("department/findChildDepartmentByCodeAndTypes"
                , map
                , new ParameterizedTypeReference<List<DepartmentBean>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public List<DepartmentBean> findDepartmentCountByCode(String departmentCode) {
        return userCall("department/findDepartmentCountByCode"
                , ofMap("departmentCode", departmentCode)
                , new ParameterizedTypeReference<List<DepartmentBean>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public List<DepartmentBean> findByUnitTypeOne(Map<String, Object> map) {
        return userCall("department/findByUnitTypeOne"
                , map
                , new ParameterizedTypeReference<List<DepartmentBean>>() {
                }
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public DepartmentBean findByDeCode(String departmentCode) {
        return userCall("department/findByDeCode"
                , ofMap("departmentCode", departmentCode)
                , DepartmentBean.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public List<DepartmentBean> findNoDepartmentIdCode(Map<String, Object> map) {
        return userCall("department/findNoDepartmentIdCode"
                , map
                , new ParameterizedTypeReference<List<DepartmentBean>>() {}
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public void insertBatch(List<Object> department) {
        userCall("department/findNoDepartmentIdCode"
                , department
                , String.class
        );
    }

    @Override
    @Cacheable(value = "department", keyGenerator = "keyGenerator")
    public List<DepartmentBean> findProvince() {
        return userCall("department/findProvince"
                , null
                , new ParameterizedTypeReference<List<DepartmentBean>>() {}
        );
    }

    @Override
    public List<DepartmentBean> findListByCodeAndWord(Map<String, String> map) {
        return userCall("department/findListByCodeAndWord"
                , map
                , new ParameterizedTypeReference<List<DepartmentBean>>() {}
        );
    }
    @Override
    public List<String> findDepartmentCodeListByCode(String oldDepartmentCode) {
        return userCall("department/findDepartmentCodeListByCode"
                , ofMap("oldDepartmentCode", oldDepartmentCode)
                , new ParameterizedTypeReference<List<String>>() {}
        );
    }
}
