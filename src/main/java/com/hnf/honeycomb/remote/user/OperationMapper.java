package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.OperationBean;

import java.util.List;

/**
 * 可操作功能持久层
 *
 * @author zhouhong
 */
public interface OperationMapper {

    /**
     * 查询所有操作列表
     *
     * @return
     */
    List<OperationBean> find();

    /**
     * 查询角色的所有功能
     *
     * @param roleId
     * @return
     */
    List<OperationBean> findByRoleid(Long roleId);

}
