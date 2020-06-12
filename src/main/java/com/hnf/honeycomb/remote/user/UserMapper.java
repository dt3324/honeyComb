package com.hnf.honeycomb.remote.user;

import com.hnf.honeycomb.bean.AddUserBean;
import com.hnf.honeycomb.bean.User;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhong
 * @ClassName UserMapper
 * @Description: TODO 用户管理的持久层接口
 * @date 2018年6月25日 上午10：38：10
 */
public interface UserMapper {

    /**
     * 通过警号查找人员信息
     *
     * @param name 警号
     * @return
     */
    User findUserByPoliceNumber(String name);

    /**
     * 冻结或者激活账号
     *
     * @param map
     */
    void updateActivate(Map<String, Object> map);

    /**
     * 更新当前警号最后活动时间
     *
     * @param map lastActivityTime  需要更新的时间命名必须一致
     * @param map
     * @param map policeNumber 警号命名必须一致 存入map中
     */
    void updateLastActiveyTimeByPoliceNum(Map<String, Object> map);

    /**
     * 用户新增
     *
     * @param user 用户信息
     */
    Integer saveUser(User user);

    /**
     * 修改用户表的单位代码
     *
     * @param map
     */
    void updateUserUnitType(Map<String, Object> map);

    /**
     * 修改用户表的部门代码
     *
     * @param map
     */
    void updateUserDepartType(Map<String, Object> map);

    /**
     * 以管理员身份修改用户
     *
     * @param map
     */
    void updateOne(Map<String, Object> map);

    /**
     * 通过单位类型和单位编码查找用户
     *
     * @param map 封装单位类型和单位编码的MAP
     * @return
     */
    List<User> findByDepartmentId(Map<String, Object> map);

    /**
     * 统计用户总数
     *
     * @param map 条件
     * @return
     */
    int findCount(Map<String, Object> map);

    /**
     * 查询所有用户
     *
     * @param map 条件
     * @return
     */
    List<User> findAll(Map<String, Object> map);


    /**
     * 通过警号查询用户
     *
     * @param policeNumber 警号
     * @return
     */
    User findUserInfoByPoliceNumber(String policeNumber);

    /**
     * 通过姓名查询用户
     *
     * @param name 姓名
     * @return
     */
    List<User> findByName(String name);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return
     */
    User findById(Integer userId);


    /**
     * 删除用户
     *
     * @param policenumber 警号
     * @return
     */
    void delete(String policenumber);

    /**
     * 修改用户
     *
     * @param map
     */
    void updateToken(Map<String, Object> map);

    /**
     * 修改用户
     *
     * @param map userId as 被修改的用户；policeNumber as 要修改为的警号
     */
    void updatePoliceNumberByPID(Map<String, Object> map);

    /**
     * 修改用户
     *
     * @param map
     */
    void updateUserDIdAndRIdByPoliceNumber(Map<String, Object> map);

    /**
     * 查询用户基本信息，包含角色ID、单位、部门代码和加密狗号
     *
     * @param userId 用户ID
     * @return
     */
    Map<String, Object> findUserBaseInfoByUserId(Integer userId);


    /**
     * 修改密码
     *
     * @param map
     */
    void updatePassword(Map<String, Object> map);

    /**
     * 通过身份证号查找用户
     *
     * @param idNumber 身份证号
     * @return
     */
    User findByidNumber(String idNumber);

    /**
     * 通过身份证号查找用户
     *
     * @param map 身份证号和用户ID
     * @return
     */
    User findByidNumberAndId(Map map);


    /**
     * 根据角色ID查询用户
     *
     * @param roleId 角色ID
     * @return
     */
    List<User> findByRoleId(Long roleId);

    /**
     * 批量添加用户
     *
     * @param listUsers 用户集合
     */
    void addUsersOfBatch(List<AddUserBean> listUsers);

    int getUserCreateUserCount(String createUser);
}
