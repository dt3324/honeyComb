package com.hnf.honeycomb.service.user;


import com.hnf.honeycomb.bean.OperationBean;

import java.util.List;
import java.util.Map;

/**
 * 角色业务接口层
 *
 * @author zhouhong
 */
public interface RoleService {

    /**
     * 查询所有角色
     *
     * @param page   页码数
     * @param unitId 用户ID
     * @return
     */
    Map<String, Object> findAll(Integer page, Long unitId, Integer pageSize);

    /**
     * 新增角色
     *
     * @param roleName                 角色名
     * @param roleAbleFunId            可操作功能
     * @param userId                   用户ID
     * @return
     */
    void add(String roleName, String roleAbleFunId, Long userId);

    /**
     * 查询所有操作列表
     *
     * @return
     */
    List<OperationBean> findFun();

    /**
     * 修改角色
     *
     * @param roleId                   角色ID
     * @param roleName                 角色名
     * @param roleAbleFunId            可操作功能
     */
    void update(Long roleId, String roleName, String roleAbleFunId);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void delete(Long roleId);

    /**
     * 根据角色ID查询用户查看数据权限
     *
     * @param roleId 角色ID
     * @return
     */
    String findCanChectDepartCode(Long roleId);

}
