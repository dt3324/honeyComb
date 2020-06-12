package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.RoleBean;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhong
 * @ClassName RoleMapper
 * @Description: TODO 角色管理的持久层接口
 * @date 2018年6月25日 下午15:10:10
 */
public interface RoleMapper {

    /**
     * 查询角色
     *
     * @param roleId
     * @return
     */
    RoleBean findByRoleId(long roleId);

    /**
     * 查询角色总数
     *
     * @param unitId 用户ID
     * @return
     */
    Integer findCount(Long unitId);

    /**
     * 查询所有角色列表
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> findAll(Map map);

    /**
     * 新增角色
     *
     * @param map
     */
    void add(Map map);

    /**
     * 查询角色查看数据权限
     *
     * @param roleId 角色ID
     * @return
     */
    List<String> findCanCheckDepartCodeByRoleId(Integer roleId);
    /**
     * 通过角色名和用户ID查询角色
     *
     * @param map
     * @return
     */
    RoleBean findByNameUnitId(Map map);

    /**
     * 更新角色名称
     *
     * @param map
     */
    void update(Map map);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void delete(Long roleId);

}
