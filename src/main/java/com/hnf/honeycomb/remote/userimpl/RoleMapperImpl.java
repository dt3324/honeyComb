package com.hnf.honeycomb.remote.userimpl;

import com.hnf.honeycomb.bean.RoleBean;
import com.hnf.honeycomb.remote.user.RoleMapper;
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
public class RoleMapperImpl implements RoleMapper {

    @Override
    @Cacheable(value = "roleRemote", keyGenerator = "keyGenerator")
    public RoleBean findByRoleId(long roleId) {
        return userCall("role/findByRoleId"
                , ofMap("releId", roleId)
                , RoleBean.class
        );
    }
    @Override
    public List<String> findCanCheckDepartCodeByRoleId(Integer roleId) {
        return userCall("role/findCanCheckDepartCodeByRoleId"
                , ofMap("roleId", roleId)
                , new ParameterizedTypeReference<List<String>>() {
                }
        );
    }
    @Override
    @Cacheable(value = "roleRemote", keyGenerator = "keyGenerator")
    public Integer findCount(Long unitId) {
        return userCall("role/findCount"
                , ofMap("unitId", unitId)
                , Integer.class
        );
    }

    @Override
    @Cacheable(value = "roleRemote", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findAll(Map map) {
        return userCall("role/findAll"
                , map
                , new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }
        );
    }

    @Override
    @CacheEvict(value = "roleRemote", allEntries = true)
    public void add(Map map) {
        userCall("role/add"
                , map
                , String.class
        );
    }

    @Override
    @Cacheable(value = "roleRemote", keyGenerator = "keyGenerator")
    public RoleBean findByNameUnitId(Map map) {
        return userCall("role/findByNameUnitId"
                , map
                , RoleBean.class
        );
    }

    @Override
    @CacheEvict(value = "roleRemote", allEntries = true)
    public void update(Map map) {
        userCall("role/update"
                , map
                , String.class
        );
    }

    @Override
    @CacheEvict(value = "roleRemote", allEntries = true)
    public void delete(Long roleId) {
        userCall("role/delete"
                , ofMap("roleId", roleId)
                , String.class
        );
    }
}
