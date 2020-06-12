package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.RoleLicenseBean;
import com.hnf.honeycomb.remote.user.RoleLicenseMapper;
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
 * @date 2019/7/12 11:15
 */
@Repository
@EnableCaching
public class RoleLicenseMapperImpl implements RoleLicenseMapper {

    @Override
    @CacheEvict(value = "roleLicenseRemote", allEntries = true)
    public void add(Map map) {
        userCall("roleLicense/add", map, String.class);
    }

    @Override
    @Cacheable(value = "roleLicenseRemote", keyGenerator = "keyGenerator")
    public RoleLicenseBean findByRoleId(Long roleId) {
        return userCall("roleLicense/findByRoleId"
                , ofMap("roleId", roleId)
                , RoleLicenseBean.class
        );
    }

    @Override
    @Cacheable(value = "roleLicenseRemote", keyGenerator = "keyGenerator")
    public List<RoleLicenseBean> findByLicenseId(Integer licenseId) {
        return userCall("roleLicense/findByLicenseId"
                , ofMap("licenseId", licenseId)
                , new ParameterizedTypeReference<List<RoleLicenseBean>>() {
                }
        );
    }

    @Override
    @CacheEvict(value = "roleLicenseRemote", allEntries = true)
    public void deleteByRoleId(Long roleId) {
        userCall("roleLicense/deleteByRoleId"
                , ofMap("roleId", roleId)
                , String.class
        );
    }
}
