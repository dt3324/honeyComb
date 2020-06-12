package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.RoleOperationBean;

import java.util.List;
import java.util.Map;

/**
 * 角色管理的持久层接口
 *
 * @author zhouhong
 * @since v1 2018年6月25日 下午15:10:10
 */
public interface RoleOperationMapper {

    /**
     * 通过角色ID查找角色信息
     *
     * @return RoleBean
     */
    void add(Map<String, Object> param);

    /**
     * 根据角色Id和OperationId锁定操作授权关联
     *
     * @param param `roleId` as 被授权角色（必填），`operationId` as 所授权操作（选填）
     */
    List<RoleOperationBean> find(Map<String, Object> param);

    /***
     * 取消指定角色对所有操作的关联
     * @param roleId 角色ID
     * @return
     */
    void deleteByRoleId(String roleId);

    /**
     * 返回用户可操作功能列表
     *
     * @param roleId
     * @return
     */
    List<RoleOperationBean> findOperation(Long roleId);

}
