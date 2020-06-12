package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.remote.user.BusinessDepartmentMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.userCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 11:04
 */
@Repository
@EnableCaching
public class BusinessDepartmentMapperImpl implements BusinessDepartmentMapper {


    @Override
    @Cacheable(value = "department",keyGenerator = "keyGenerator")
    public String findDepartNameByDepartNum(String departNum) {
        return userCall("department/" + "findDepartNameByDepartNum"
                , ofMap("departNum", departNum)
                , String.class
        );
    }

    @Override
    @Cacheable(value = "department",keyGenerator = "keyGenerator")
    public DepartmentBean findByCode(String departmentCode) {
        return userCall("department/" + "findByCode"
                , ofMap("departmentCode", departmentCode)
                , DepartmentBean.class
        );
    }
}
