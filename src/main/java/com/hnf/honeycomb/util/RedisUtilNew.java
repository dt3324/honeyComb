package com.hnf.honeycomb.util;


import java.util.List;
import java.util.Set;

/**
 * @author hnf
 */
public interface RedisUtilNew {
    // 通讯录备注key
    String PHONE_REMARK = "phone_remark";
    // wx好友备注Key
    String WXFRIEND_REMARK = "wxfriend_remark";
    //wx群备注key
    String WXGROUP_REMARK = "wxgroup_remark";
    // qq好友备注key
    String QQFRIEND_REMARK = "qqfriend_remark";
    // qq群备注key
    String QQGROUP_REMARK = "qqgruop_remark";
    // 电话号码查询虚拟身份账号
    String VIRTUAL_IDENTITY_PHONE = "virtual_identity_phone";
    // qq号查询虚拟身份
    String VIRTUAL_IDENTITY_QQ = "virtual_identity_qq";
    // wx号查询虚拟身份
    String VIRTUAL_IDENTITY_WX = "virtual_identity_wx";
    // 身份证号查询虚拟身份信息
    String VIRTUAL_IDENTITY_IDCARD = "virtual_identity_idcard";
    // 设备关系碰撞key
    String IMPACT_DEVICE = "impact_device";
    // 预警QQ的key
    String ALARM_QQ_TYPE = "alarm_qq_type";
    // 预警QQ的key
    String ALARM_WX_TYPE = "alarm_wx_type";
    // 预警QQ的key
    String ALARM_PHONE_TYPE = "alarm_phone_type";
    // 预警QQ的key
    String ALARM_IDNUMBER_TYPE = "alarm_idnumber_type";
    // 登录的key
    String USER_LOGIN_CHECK = "user_login_check";
    /**
     * 用户功能列表
     */
    String USER_OPERATION_LIST = "userOperationList";
    /**
     * 用户可操作单位代码
     */
    String USER_CHECK_UNIT = "userCheckUnit";

    String TEMP_LICENSE = "-unitCode-policeNumber-";
    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(final String key, Object value);

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(final String key, Object value, Long expireTime);

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    void remove(final String... keys);

    /**
     * 批量删除key
     *
     * @param pattern
     */
    void removePattern(final String pattern);

    /**
     * 删除对应的value
     *
     * @param key
     */
    void remove(final String key);

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */

    boolean exists(final String key);

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */

    Object get(final String key);
    /**
     * 根据某个表达式获取集合
     *
     * @param pattern
     * @return
     */
    List getList(final String pattern);

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */

    void hmSet(String key, Object hashKey, Object value);

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
    Object hmGet(String key, Object hashKey);

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    void lPush(String k, Object v);

    /**
     * 列表获取
     *
     * @param k
     * @param l
     * @param l1
     * @return
     */

    List<Object> lRange(String k, long l, long l1);

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */

    void add(String key, Object value);

    /**
     * 集合获取
     *
     * @param key
     * @return
     */

    Set<Object> setMembers(String key);

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */

    void zAdd(String key, Object value, double scoure);

    /**
     * 有序集合获取
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */

    Set<Object> rangeByScore(String key, double scoure, double scoure1);
    /**
     * 获取redis中的key
     *
     * @param pattern
     * @return
     */
    Set<String> keys(String pattern);
}
