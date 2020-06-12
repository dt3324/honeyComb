package com.hnf.honeycomb.service;

import com.hnf.honeycomb.bean.AllQueryBean;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author hnf
 */
public interface ElasticSearchService {

    /**
     * 修改es中数据
     * @param type
     * @param search
     * @param list
     */
    void prepareUpdate(String type,String search,List<Map<String,String>> list);
    /**
     * 通过搜索内容搜寻对应的wx群消息记录
     *
     * @param search   搜索的条件
     * @param page     页数
     * @param pageSize 每页大小
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 返回对应搜索的结果
     */
    Object searchWxChatRoomMsg(
            String search, Integer page, Integer pageSize, String timeFrom, String timeEnd);

    /**
     * 统计对应的wx群消息的总量
     *
     * @param search   对应搜索的聊天内容
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 符合搜索条件的条数
     */
    Long countWxChatRoomMsg(String search, String timeFrom, String timeEnd);

    /**
     * 通过对应的搜索条件搜索对应的通话记录的信息
     *
     * @param search   搜索的条件
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @param page     页数
     * @param pageSize 页码
     * @return 返回通话记录的信息
     */
    @Cacheable(value = "searchRecord", keyGenerator = "keyGenerator")
    List<AllQueryBean> searchRecordService(
            String search, String timeFrom, String timeEnd, Integer page, Integer pageSize);

    /**
     * 统计对应通话记录符合搜索条件的总数
     *
     * @param search   搜索的条件
     * @param timeFrom 搜索的开始时间
     * @param timeEnd  搜索的结束时间
     * @return 返回符合消息的总数
     */
    Long countRecord(String search, String timeFrom, String timeEnd);

    /**
     * 一键搜中对于qq群消息记录的搜索service层
     *
     * @param search   搜索的条件
     * @param timeFrom 搜索的开始时间
     * @param timeEnd  搜索的结束时间
     * @param page     页数
     * @param pageSize 每页大小
     * @return 返回qq群消息记录
     */
    Object searchQqTroopMsgService(
            String search, String timeFrom, String timeEnd, Integer page, Integer pageSize);

    /**
     * 统计对应qq群聊天信息的总数
     *
     * @param search   对应的搜索条件
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 返回符合消息的条数
     */
    Long countQqTroopMsg(String search, String timeFrom, String timeEnd);

    /**
     * 对wx好友聊天记录进行修改
     *
     * @param search   搜索的文字信息
     * @param timeFrom 搜索的开始时间
     * @param timeEnd  搜索的结束时间
     * @param page     页数
     * @param pageSize 每页的大小
     * @return 返回搜索的结果
     */
    Object searchQqMsgService(
            String search, String timeFrom, String timeEnd, Integer page, Integer pageSize);

    /**
     * 统计对应的qq好友聊天信息的总数
     *
     * @param search   对应的搜索条件
     * @param timeFrom 对应的搜索开始时间
     * @param timeEnd  对应的搜索结束时间
     * @return 返回消息的总数
     */
    Long countQqMsg(String search, String timeFrom, String timeEnd);

    /**
     * 用于搜索对应wx好友聊天信息中的符合信息的条数
     *
     * @param search   搜索的对应条件
     * @param page     搜索的页数
     * @param pageSize 搜索每页的大小
     * @param timeFrom 搜索的开始时间
     * @param timeEnd  搜索的结束时间
     * @return 返回最后的结果
     */
    Object searchWxMsgService(
            String search, Integer page, Integer pageSize, String timeFrom, String timeEnd);

    /**
     * 统计对应的wx聊天信息的总数
     *
     * @param search   搜索的条件
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 返回符合条件的总数
     */
    Long countWxMsg(String search, String timeFrom, String timeEnd);

    /**
     * 用于搜索对应短信息中的符合信息的条数
     *
     * @param search   搜索的对应条件
     * @param page     搜索的页数
     * @param pageSize 搜索每页的大小
     * @param timeFrom 搜索的开始时间
     * @param timeEnd  搜索的结束时间
     * @return 返回最后的结果
     */
    List<AllQueryBean> searchMsgService(
            String search, Integer page, Integer pageSize, String timeFrom, String timeEnd);

    /**
     * 统计对应短信息的消息总数
     *
     * @param search   搜索对应的条件
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 返回消息总数
     */
    Long countMsg(String search, String timeFrom, String timeEnd);

    /**
     * 用于一键搜索人员对应数据的搜索方法
     *
     * @param search   搜索对应的条件
     * @param page     页数
     * @param pageSize 页码
     * @return 返回查询后的总数
     */
    List<Map<String, Object>> findPerson(String search, Integer page, Integer pageSize);

    /**
     * 用于一键搜中es对应人员的搜索方法
     *
     * @param search 搜索的内容
     * @return 返回符合条件的总数
     */
    Long countPerson(String search);

    /**
     * 通过对应的搜索条件查询对应的案件
     *
     * @param search   搜索条件
     * @param fromTime 搜索案件开始的时间
     * @param endTime  搜索案件结束的时间
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findCase(
            String search, String fromTime, String endTime, Integer page, Integer pageSize);

    /**
     * 统计es中案件信息
     *
     * @param search   搜索的条件
     * @param fromTime 查询开始的时间
     * @param endTime  查询结束的时间
     * @return 总数
     * @
     */
    Long countCase(String search, String fromTime, String endTime);

    /**
     * 通过对应的搜索条件查询对应的设备
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findDevice(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中设备信息
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countDevice(String search);

    /**
     * 通过对应的搜索条件查询对应的qq用户
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findQQUser(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中qq用户信息
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countQQUser(String search);

    /**
     * 通过对应的搜索条件查询对应的qq群基本信息
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findQQTroop(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中qq群基本信息
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countQQTroop(String search);

    /**
     * 通过对应的搜索条件查询对应的wx用户
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findWXUser(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中wx用户
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countWXUser(String search);

    /**
     * 通过对应的搜索条件查询对应的wx用户
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findWXChatroom(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中wx用户
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countWXChatroom(String search);

    /**
     * 通过对应的搜索条件查询对应的wx用户
     *
     * @param search   搜索条件
     * @param page     页数
     * @param pageSize 每页大小
     * @return
     * @
     */
    List<Map<String, Object>> findContactPhoneNum(
            String search, Integer page, Integer pageSize);

    /**
     * 统计es中wx用户
     *
     * @param search   搜索的条件
     * @return 总数
     * @
     */
    Long countContactPhoneNum(String search);

    /**
     * 根据type对对应的结果进行搜索
     *
     * @param search   对应的搜索条件
     * @param page     对应的页数
     * @param pageSize 对应的每页条数
     * @param timeFrom 对应的搜索开始时间
     * @param timeEnd  对应的搜索结束时间
     * @param type     对应的消息类型
     * @return 返回搜索结果
     */
    Object findSearchResultBySearchInfo(
            String search, Integer page, Integer pageSize,
            String timeFrom, String timeEnd, String type);

    /**
     * 对对应的ES中的数据进行前缀查询
     *
     * @param type   对应的搜索类型
     * @param search 对应的搜索号码
     * @return 返回对应的字符串集合
     */
    HashSet<String> searchPreNumber(String type, String search);
}
