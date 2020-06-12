package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.RoleLicenseBean;

import java.util.List;
import java.util.Map;

/**
 * 角色查看数据权限持久层
 *
 * @author zhouhong
 */
public interface RoleLicenseMapper {

    /**
     * 给新增角色添加查看数据权限
     *
     * @param map
     */
    void add(Map map);

    /**
     * 通过角色ID查询角色的查看数据权限
     *
     * @param roleId 角色ID
     * @return
     */
    RoleLicenseBean findByRoleId(Long roleId);

    /**
     * 通过数据查看权限ID查询角色的查看数据权限
     *
     * @param licenseId 数据查看权限ID
     * @return
     */
    List<RoleLicenseBean> findByLicenseId(Integer licenseId);

    /**
     * 通过角色ID删除对应角色的查看数据权限
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(Long roleId);
}
