package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * ES相关设备数据的查询方法
 *
 * @author yy
 */
public interface EsDeviceService {
    /**
     * 通过对应的多个deviceUnique查询对应的设备名称
     *
     * @param deviceUniqes
     * @return
     */
    Map<String, String> findDeviceUnique2DeviceName(List<String> deviceUniqes);

    /**
     * 通过对应的deviceUnique查询对应的设备信息
     *
     * @param deviceUnqiue 对应的deviceUnqiue
     * @return 返回设备的相关信息
     */
    List<Document> findDeviceInfoByDeviceUnqiue(String deviceUnqiue);

    /**
     * 通过设备唯一标识查询对应的人员名称即电话号码
     *
     * @param deviceUnqiues 对应的设备唯一标识集合
     * @return 返回设备唯一标识与人员姓名及电话的对应关系
     */
    Map<String, String[]> findDeviceUnique2PersonNameAndPhone(List<String> deviceUnqiues);

    /**
     * 通过对应的设备唯一标识以及电话号码查询对应的通讯录名称
     *
     * @param deviceUnique 对应的设备唯一标识
     * @param phone        对应的电话
     * @return 返回对应人员的名称
     */
    String findContactNameByDeviceUniqueAndPhone(String deviceUnique, String phone);

    /**
     * 查询qq账号与qq昵称的对应关系
     *
     * @param qqNumber 对应的qq账号
     * @return 账号与昵称的对应关系
     */
    Map<String, String> findQQNumber2QQNickByQQNums(List<String> qqNumber);

    /**
     * 查询wx账号与微信昵称的对应关系
     *
     * @param wxNumbers wx账号集合
     * @return 对应关系
     */
    Map<String, String> findWXNumber2WXNickByWXNums(List<String> wxNumbers);

    /**
     * 通过wx群号查询群号与昵称的对应关系
     *
     * @param wxChats wx群号
     * @return 对应关系
     */
    Map<String, String> findWXChatNumber2WXChatNickByWXChats(List<String> wxChats);

    /**
     * 通过qq群号查询群号与群昵称的对应关系
     *
     * @param qqTroops 群号集合
     * @return 对应关系
     */
    Map<String, String> findQQTroopNumber2QQTroopNumberByQQTroops(List<String> qqTroops);

    /**
     * 通过案件唯一标识查询案件相关信息以及其对应的设备及人员信息
     *
     * @param caseUnique 案件唯一标识
     * @return 返回相关联的所有信息
     */
    Map<String, Object> findRelationDeviceAndPersonInfoByCaseUnqiue(String caseUnique);

    /**
     * 通过人员的唯一标识查询相关联的案件以及人员设备信息
     *
     * @param personUnique 设备唯一标识
     * @return 返回相关结果
     */
    Map<String, Object> findRelationDeviceAndCaseInfoByPersonUnqiue(String personUnique);

    /**
     * 通过设备唯一标识查询相关联的所有信息
     *
     * @param deviceUnique 设备唯一标识
     * @return 返回相关联的所有信息
     */
    Map<String, Object> findRelationPersonAndCaseInfoByDeviceUnqiue(String deviceUnique);

    /**
     * 通过多个设备唯一标识查询设备相关信息
     *
     * @param deviceUniques 多个设备唯一标识
     * @return 返回设备的相关信息
     */
    List<Document> findDeviceInfoByDeviceUniques(List<String> deviceUniques);

    /**
     * 通过案件唯一标识查询多个案件的相关信息
     *
     * @param caseUniques 案件唯一标识
     * @return 返回案件的相关信息
     */
    List<Document> findCaseInfoByCaseUniques(List<String> caseUniques);

    /**
     * 通过人员的多个唯一标识查询人员相关信息（唯一标识为人员身份证号）
     *
     * @param personUniques 多个人员身份证号
     * @return 返回人员相关信息
     */
    List<Document> findPersonInfoByPersonUniques(List<String> personUniques);

    /**
     * 通过设备以及电话号码及对应消息的唯一标识查询对应的信息
     *
     * @param deviceUnique 设备唯一标识
     * @param phone        对应的电话号码
     * @param unique       对应的设备唯一标识
     * @return 返回上下文
     */
    List<Document> findMessageContextByOneMsgInfo(
            String deviceUnique, String phone, String unique);

    /**
     * 通过设备唯一标识查询对应的人员信息
     *
     * @param deviceUnique 设备唯一标识
     * @return 返回对应的人员信息
     */
    List<Document> findPersonInfoByDeviceUnique(String deviceUnique);

    /**
     * 通过多个qq账号查询对应的qq信息
     *
     * @param qqNumbers
     * @return
     */
    List<Document> findQQUserInfoByQQNumbers(List<String> qqNumber);

    /**
     * 通过单个QQ账号查询对应的qq用户消息
     *
     * @param qqNumber 单个QQ账号
     * @return
     */
    List<Document> findQQUserInfoByQQNumber(String qqNumber);


    /**
     * 通过多个wx账号查询对应的wx信息
     *
     * @param wxNumbers
     * @return
     */
    List<Document> findWXUserInfoByWXNumbers(List<String> wxNumber);

    /**
     * 通过单个wx账号查询对应的wx用户消息
     *
     * @param wxNumber 单个wx账号
     * @return
     */
    List<Document> findWXUserInfoByWXNumber(String wxNumber);

    /**
     * 通过多个qq群号查询对应的qq群信息
     *
     * @param qqTroopNumber
     * @return
     */
    List<Document> findQQTroopInfoByQQTroopNumbers(List<String> qqTroopNumber);

    /**
     * 通过单个QQ群号查询对应的qq群消息
     *
     * @param qqTroopNumber 单个QQ账号
     * @return
     */
    Map<String, Object> findQQTroopInfoByQQTroopNumber(String qqTroopNumber);

    /**
     * 通过多个wx群号查询对应的wx群信息
     *
     * @param wxTroopNumber
     * @return
     */
    List<Document> findWXTroopInfoByWXTroopNumbers(List<String> wxTroopNumber);

    /**
     * 通过单个WX群号查询对应的WX群消息
     *
     * @param wxTroopNumber 单个WX账号
     * @return
     */
    Map<String, Object> findWXTroopInfoByWXTroopNumber(String wxTroopNumber);

    /**
     * 通过两个好友账号查询对应的聊天信息上下文
     *
     * @param qqSelfUin   对应的qq好友账号
     * @param qqFriendUin 对应的qq账号
     * @param unique      对应消息的唯一标识
     * @return 返回上下文
     */
    List<Document> findQQFriendMsgContextByOneMsgInfo(
            String qqSelfUin, String qqFriendUin, String unique);

    /**
     * 查询wx上下文
     *
     * @param wxUserName   wx账号
     * @param wxFriendName wx好友账号
     * @param unique       唯一标识
     * @return 返回结果
     */
    List<Document> findWXFriendMsgContextByOneMsgInfo(
            String wxUserName, String wxFriendName, String unique);

    /**
     * 通过对应的qq群号以及对应信息的唯一标识查看上下文
     *
     * @param troopUin 对应的群号
     * @param unique   对应的设备唯一标识
     * @return 返回上下文
     */
    List<Document> findQQTroopMsgContextByOneMsgInfo(
            String troopUin, String unique);

    /**
     * 查询wx群聊天上下文
     *
     * @param wxTroopUin 群号
     * @param unique     聊天信息的唯一标识
     * @return 返回结果
     */
    List<Document> findWXChatroomMsgContextByOneMsgInfo(
            String wxTroopUin, String unique);

    /**
     * 对对应的qq账号,wx账号,电话号码,身份证号进行对应的模糊匹配
     *
     * @param search     搜索的账号
     * @param searchType 搜索的类型
     * @return
     */
    List<Map<String, Object>> findNumInfo(String search, String searchType);

    /**
     * 通过QQ或者微信号查询对应人员的设备和案件信息
     *
     * @param uin 账号
     * @return
     */
    Map findInfoByQqOrWx(String uin);
}
