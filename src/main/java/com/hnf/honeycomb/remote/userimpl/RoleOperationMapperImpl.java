package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.RoleOperationBean;
import com.hnf.honeycomb.remote.user.RoleOperationMapper;
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
 * @date 2019/7/12 11:18
 */
@Repository
@EnableCaching
public class RoleOperationMapperImpl implements RoleOperationMapper {


    @Override
    @CacheEvict(value = "roleOperationRemote", allEntries = true)
    public void add(Map<String, Object> param) {
        userCall("roleOperation/add"
                , param
                , String.class
        );
    }

    @Override
    @Cacheable(value = "roleOperationRemote", keyGenerator = "keyGenerator")
    public List<RoleOperationBean> find(Map<String, Object> param) {
        return userCall("roleOperation/find"
                , param
                , new ParameterizedTypeReference<List<RoleOperationBean>>() {
                }
        );
    }

    @Override
    @CacheEvict(value = "roleOperationRemote", allEntries = true)
    public void deleteByRoleId(String roleId) {
        userCall("roleOperation/deleteByRoleId"
                , ofMap("roleId", roleId)
                , String.class
        );
    }

    @Override
    @Cacheable(value = "roleOperationRemote", keyGenerator = "keyGenerator")
    public List<RoleOperationBean> findOperation(Long roleId) {
        return userCall("roleOperation/findOperation"
                , ofMap("roleId", roleId)
                , new ParameterizedTypeReference<List<RoleOperationBean>>() {
                }

        );
    }
}
