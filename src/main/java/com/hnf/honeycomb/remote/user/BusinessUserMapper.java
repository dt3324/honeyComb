package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.UserBean;

import java.util.Map;

/**
 * 用户管理持久层
 *
 * @author zhouhong
 */
public interface BusinessUserMapper {

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return
     */
    Map findById(Integer userId);


    /**
     * 通过警号查询用户
     *
     * @param policeNumber 警号
     * @return
     */
    UserBean findByPoliceNumber(String policeNumber);

}
