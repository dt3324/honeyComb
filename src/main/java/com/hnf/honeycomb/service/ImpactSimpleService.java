package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;


/**
 * 单一关系碰撞业务接口
 *
 * @author lsj
 */
public interface ImpactSimpleService {
    /**
     * 根据指定类型进行关系碰撞
     *
     * @param deviceUniques 设备唯一标识
     * @param
     * @return 碰撞关系
     */
    Map<String, Object> impactByQQNumbers(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 根据指定类型进行关系碰撞
     *
     * @param department 部门编号
     * @return 碰撞关系
     */
    Map<String, Object> impactByDepartment(String department,String type, Integer userId, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);


    /**
     * 根据指定类型进行关系碰撞
     *
     * @param deviceUniques 设备唯一标识
     * @param
     * @return 碰撞关系
     */
    Map<String, Object> impactByWXNumbers(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 碰撞直接关系
     * 根据指定类型进行关系碰撞
     *
     * @param deviceUniques 设备唯一标识
     * @param
     * @return 碰撞关系
     */
    Map<String, Object> impactByPhoneNumbers(String deviceUniques, Integer userId, String place,
                                             Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 碰撞qq群关系
     *
     * @param deviceUniques 设备唯一码
     * @param userId        用户id
     * @param searchNum     搜索号码
     * @param time          时间
     * @param place         ip地址
     * @param startTime     搜索开始时间
     * @param endTime       搜索结束时间
     * @param countLimit    限制条件
     * @return 碰撞关系
     */
    Map<String, Object> impactByQQtroop(
            String deviceUniques, Integer userId
            , String searchNum, String time, String place, Long startTime, Long endTime, Integer countLimit
            , Integer timeSelectType);

    /**
     * 碰撞微信群关系
     *
     * @param deviceUniques 设备唯一码
     * @param userId
     * @param searchNum
     * @param time
     * @param place
     * @return 碰撞关系
     */
    Map<String, Object> impactByWXtroop(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 碰撞非通联关系
     *
     * @param deviceUniques
     * @param userId
     * @param searchNum
     * @param time
     * @param place
     * @return
     */
    Map<String, Object> impactByPhoneIsNotFriend(
            String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 通过电话碰撞是通讯录好友的关系
     *
     * @param deviceUniques 设备号
     * @param userId
     * @param searchNum
     * @param time
     * @param place
     * @return
     */
    Map<String, Object> impactByPhoneAndIsFriend(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);
    /**
     * 将所选的设备列表保存至redis
     * @param policeNumber 警号
     * @param deviceUnique 设备唯一标识
     * @param casename 案件名称
     */

    /**
     * 通过对应的节点相关信息查询节点的相关数据
     *
     * @param nodeType      对应的节点类型1代表为设备 2代表为好友节点集合
     * @param deviceUniques 对应的设备唯一标识集合 若nodeType为2则为多个 以,隔开
     * @param searchNum     共同好友账号,若nodeType为1 则此项为null
     * @param searchType    搜索的类型
     * @return
     */
    Object findNodeMsgByNodeDetail(Integer nodeType, String deviceUniques, String searchNum
            , Integer searchType, Long startTime, Long endTime, Integer timeSelectType);

    /**
     * 短消息直接关系碰撞
     *
     * @param deviceUniques  设备唯一标识“，” 隔开的字符串
     * @param userId         用户id
     * @param place          请求登陆的ip 地址
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param countLimit     联系次数限制
     * @param timeSelectType 快捷时间段选项
     * @return
     */
    Map<String, Object> impactByMessageStraight(String deviceUniques, Integer userId, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType);

    /**
     * 保存关系碰撞业务
     *
     * @param userId       对应的用户id
     * @param type         对应关系碰撞类型
     * @param deviceUnique 对应的关系碰撞设备
     * @param time         对应的关系碰撞时间
     * @param project      对应的碰撞业务名
     * @param explain      对应的碰撞业务说明
     */
    Long insertImpactHistory(Integer userId, String type, String deviceUnique, String time, String project,
                             String explain);

    /**
     * 通过对应的用户ID以及各种条件查询对应的方法
     *
     * @param userId     对应的用户ID
     * @param project    对应搜索的碰撞业务名
     * @param deviceName 对应的搜索设备名
     * @param page       页数
     * @param pageSize   页码
     * @return 返回对应的结果
     */
    List<Document> findImpactHistoryByUserId(Integer userId, String project, String deviceName, Integer page, Integer pageSize);

    /**
     * 通过用户ID查询该用户保存的
     *
     * @param userId
     * @return List<Document>
     * @throws
     * @Title: findImpactProjectName
     * @Description: TODO(describe)
     */
    List<Document> findImpactProjectName(Integer userId);


    /**
     * 通过unique 查询我的业务
     *
     * @param impactUnique
     * @return Document
     * @throws
     * @Title: findImpactHistoryByUnique
     * @Description: TODO(describe)
     */
    Document findImpactHistoryByUnique(String impactUnique);

    /**
     * 修改对应的碰撞历史
     *
     * @param unique       碰撞业务唯一标识
     * @param project      对应的碰撞业务名
     * @param explain      对应的碰撞业务说明
     * @param deviceUnique 对应的关系碰撞设备
     * @return 修改返回值
     */
    Integer updateImpactHistory(String unique, String explain, String project, String deviceUnique);

    /**
     * 删除对应的碰撞历史
     *
     * @param unique 碰撞业务唯一标识
     */
    Integer deleteImpactHistory(String unique);

    /**
     * 查询账号和对应好友之间的通联信息详情
     *
     * @param type           查询类型，通话记录，QQ（群）聊天信息，微信（群）聊天信息，短消息
     * @param pageNumber     页码数
     * @param pageSize
     * @param uin            自己账号
     * @param fuin           好友账号
     * @param startDate      开始时间
     * @param endDate        结束时间
     * @param searchContent  搜索内容
     * @param timeSelectType
     * @return
     */
    Map findMsgDetails(Integer type, Integer pageNumber, Integer pageSize, String uin, String fuin, String startDate, String endDate, String searchContent, Integer timeSelectType) throws Exception;

    /**
     * 查询账号对应的好友列表
     *
     * @param type          账号类型
     * @param uin           账号
     * @param startDate     开始时间
     * @param endDate       结束时间
     * @param searchContent 搜索内容
     * @return
     */
    Map findFriendList(Integer type, String uin, String startDate, String endDate, String searchContent) throws Exception;

    /**
     * 通过对应的用户ID以及各种条件查询对应的方法
     *
     * @param userId     对应的用户ID
     * @param project    对应搜索的碰撞业务名
     * @param deviceName 对应的搜索设备名
     * @param page       页数
     * @param pageSize   页码
     * @return 返回对应的结果
     */
    List<Document> findImpactHistory(Integer userId, String project, String deviceName, Integer page, Integer pageSize);


    /**
     * 添加碰撞设备存入REDIS（新）
     *
     * @param policeNumber 警号
     * @param deviceUnique 设备唯一标识
     * @param personName   人员名称
     * @param idNumber     身份证号
     * @return
     */
    String impactAddDevices(String policeNumber, String deviceUnique, String personName, String idNumber,String departmentName);


    /**
     * 查询REDIS中的碰撞设备信息
     *
     * @param policeNumber 警号
     * @return
     */
    List<Object> impactfindDevices(String policeNumber);

    /**
     * 删除某人下面的碰撞设备信息
     *
     * @param policeNumber 警号
     * @param personName   人员名称
     * @param idNumber     身份证号
     * @return
     */
    String impactdeleteDeviceByName(String policeNumber, String personName, String idNumber,String departmentName);

    /**
     * 删除所有的碰撞设备列表，即清空设备购物车
     *
     * @param policeNumber
     * @return
     */
    String impactdeleteDevices(String policeNumber);

    /**
     * 通过案件或者人员查询设备
     *
     * @param caseuniquemark 案件唯一标识
     * @param usernumber     身份证号
     * @param departmentCode    部门代码
     * @return
     */
    List findDevice(String caseuniquemark, String usernumber,String departmentCode);

    /**
     * 通过各种条件查询设备碰撞历史业务
     *
     * @param userId     人员ID
     * @param project    业务名
     * @param personName 人员姓名
     * @param page       页码数
     * @param pageSize   每页显示数目
     * @return
     */
    List<Document> findDeviceImpactHistory(Integer userId, String project, String personName, Integer page, Integer pageSize);


    /**
     * 保存关系碰撞业务(新)
     *
     * @param userId   对应的用户id
     * @param type     对应关系碰撞类型
     * @param idNumber 人员证件号
     * @param time     对应的关系碰撞时间
     * @param project  对应的碰撞业务名
     * @param explain  对应的碰撞业务说明
     */
    Long insertImpactHistoryNew(Integer userId, String type, List<String> idNumber, String time, String project,
                                String explain);


    /**
     * 通过对应的用户ID以及各种条件查询对应的方法（新）
     *
     * @param userId     对应的用户ID
     * @param project    对应搜索的碰撞业务名
     * @param personName 对应的搜索人名
     * @param page       页数
     * @param pageSize   页码
     * @return 返回对应的结果
     */
    List<Document> findImpactHistoryNew(Integer userId, String project, String personName, Integer page, Integer pageSize);

    /**
     * 通过案件添加碰撞设备存入REDIS（新）
     *
     * @param policeNumber   警号
     * @param caseuniquemark 案件唯一标识
     */
    String impactAddDevicesByCase(String policeNumber, String caseuniquemark,String departmentName);


}
