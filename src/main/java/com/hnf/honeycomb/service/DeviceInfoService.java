package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 设备信息里面详情的查看方法
 *
 * @author yy
 */
public interface DeviceInfoService {

    /**
     * 通过设备DeviceUnique查询对应的全部人员
     *
     * @param deviceUnique 设备唯一标识
     * @return 查询结果
     */
    List<Document> findRelationPersonsByDeviceUnique(String deviceUnique);

    /**
     * 通过设备唯一标识查询相关联的案件信息
     *
     * @param deviceUnique 设备唯一标识
     * @return 查询结果
     */
    List<Document> findRelationCasesByDeviceUnique(String deviceUnique);

    /**
     * 通过设备唯一标识查询设备的相关信息
     *
     * @param deviceUnique 设备唯一标识
     * @return 查询结果
     */
    List<Document> findDeviceInfoByDeviceUnique(String deviceUnique);

    /**
     * 通过设备网唯一标识查询对应的通讯录详情
     *
     * @param deviceUnique 设备唯一标识
     * @param page         页码数
     * @return 查询结果
     */
    Object findContactInfoByDeviceUnique(String deviceUnique, Integer page);

    /**
     * 对设备下的聊天详情进行对应的统计
     *
     * @param deviceUnique 设备唯一标识
     * @return 查询结果
     */
    Object findMsgCountByDeviceUnique(String deviceUnique, Long startTime, Long endTime
            , Integer timeSelectType, String searchContent, String startDate, String endDate);

    /**
     * 通过设备唯一标识查询对应的通讯录统计详情
     *
     * @param deviceUnique 对应的设备唯一标识
     * @return 查询结果
     */
    Object findRecordCallByDeviceUnique(String deviceUnique, Long startTime, Long endTime
            , Integer timeSelectType, String startDate, String endDate);

    /**
     * 通过设备唯一标识与电话号码查询对应的通讯录详情
     *
     * @param deviceUnique 设备唯一标识
     * @param phone        通讯录详情
     * @return 查询结果
     */
    List<Document> findContactDetailByDeviceUniqueAndPhone(String deviceUnique, String phone);

    /**
     * 通过设备唯一标识以及分页条件查询短消息详情
     *
     * @param deviceUnique 对应的设备唯一标识
     * @param phone        对应的电话号码
     * @param page         对应的页数
     * @param pageSize     对应的页码数
     * @return 查询结果
     */
    List<Document> findOne2OneMsgByDeviceUniqueAndPhone(
            String deviceUnique, String phone, Integer page, Integer pageSize
            , Long startTime, Long endTime, Integer timeSelectType, String searchContent
            , String startDate, String endDate);

    /**
     * 通过设备唯一标识以及对应的查询条件查询对应的通话记录详情
     *
     * @param deviceUnique 对应的设备唯一标识
     * @param phone        对应的电话号码
     * @param page         页数
     * @param pageSize     每页大小
     * @return 查询结果
     */
    List<Document> findOne2OneRecordByDeviceUniqueAndPhone(
            String deviceUnique, String phone, Integer page, Integer pageSize
            , Long startTime, Long endTime, Integer timeSelectType, String startDate, String endDate);

    /**
     * 通过qq账号，查询qq具体信息以及对应的qq好友聊天详情以及qq群详情
     *
     * @param qqUin 对应的qq号
     * @return 查询结果
     */
    Map<String, Object> findQQUserDetailByQQUin(String qqUin, Long startTime, Long endTime
            , Integer timeSelectType, String searchContent, String startDate, String endDate);

    /**
     * 通过wx账号，查询wx具体信息以及对应的wx好友聊天详情以及wx群详情
     *
     * @param wxUin 对应的wx号
     * @return 查询结果
     */
    Map<String, Object> findWXUserDetailByWXUin(String wxUin, Long startTime, Long endTime
            , Integer timeSelectType, String searchContent, String startDate, String endDate);

    /**
     * 查询两个wx好友之间的具体聊天信息
     *
     * @param selfWxUserName   自己的wx账号
     * @param friendWxUserName 好友的账号
     * @param page             页数
     * @param pageSize         页码
     * @return 查询结果
     */
    Map<String, Object> findOne2OneWXFriendMsgByTwoWXUserName(
            String selfWxUserName, String friendWxUserName, Integer page, Integer pageSize
            , Long startTime, Long endTime, Integer timeSelectType, String searchContent
            , String startDate, String endDate);

    /**
     * 查询两个好友之间的qq聊天信息
     *
     * @param selfQqUin   自己的qq账号
     * @param friendQqUin 好友的qq账号
     * @param page        页数
     * @param pageSize    页码
     * @return 查询结果
     */
    Map<String, Object> findOne2OneQQFriendMsgByTwoQQUin(
            String selfQqUin, String friendQqUin, Integer page, Integer pageSize
            , Long startTime, Long endTime, Integer timeSelectType, String searchContent
            , String startDate, String endDate);

    /**
     * 通过对应的wx群号查询对应的wx聊天详情
     *
     * @param wxTroopUin 对应的wx群账号
     * @param page       页数
     * @param pageSize   每页大小
     * @return 查询结果
     */
    Map<String, Object> findWXTroopUinMsgByWXTroopUin(
            String wxTroopUin, Integer page, Integer pageSize, Long startTime, Long endTime
            , Integer timeSelectType, String searchContent, String startDate, String endDate);

    /**
     * 通过对应的qq群号查询对应的qq聊天详情
     *
     * @param qqTroopUin 对应的qq群账号
     * @param page       页数
     * @param pageSize   每页大小
     * @return 查询结果
     */
    Map<String, Object> findQQTroopUinMsgByQQTroopUin(
            String qqTroopUin, Integer page, Integer pageSize, Long startTime, Long endTime
            , Integer timeSelectType, String searchContent, String startDate, String endDate);


    /**
     * 统计设备下面账号的聊天信息
     *
     * @param deviceUnique 设备唯一标识
     * @return 查询结果
     */
    List<Document> findCountBydeviceUnique(String deviceUnique);
}
