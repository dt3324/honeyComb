package com.hnf.honeycomb.dao;


import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.util.Map;

/**
 * @author hnf
 */
public interface ElasticSearchDao {
    /**
     * 修改es中的数据
     * @param indexName index
     * @param typeName type
     * @param id 文档的唯一标识
     * @param map 更改的数据
     */
    void esUpdate(String indexName, String typeName,String id, Map<String,Object> map);

    /**
     * 搜索wx群消息记录
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchWxChatRoomMsg(String indexName, String typeName, QueryBuilder mmqb,
                                   RangeQueryBuilder rqb, HighlightBuilder hb, Integer from, Integer size);

    /**
     * 用于搜索对应的通话记录
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchRecord(String indexName, String typeName, QueryBuilder mmqb,
                            RangeQueryBuilder rqb, HighlightBuilder hb, Integer from, Integer size);

    /**
     * 用于搜索对应的qq群聊天信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchQqTroopMsg(String indexName, String typeName,
                                QueryBuilder mmqb, RangeQueryBuilder rqb,
                                HighlightBuilder hb, Integer from, Integer size);

    /**
     * 搜索对应的qq好友聊天信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchQqMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                           HighlightBuilder hb, Integer from, Integer size);

    /**
     * 搜索对应的wx好友聊天信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchWxMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                           HighlightBuilder hb, Integer from, Integer size);

    /**
     * 搜索对应的短消息记录
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                         HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计对应短消息条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的好友聊天条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countWxMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的qq好友聊天条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countQqMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的wx群聊天信息的条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countWxTroopMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的qq群聊天信息的条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countQqTroopMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的通话记录的信息条数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countRecordCall(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 统计对应的人员信息的总数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countPerson(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 查询对应的人员信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchPerson(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                            HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计对应的人员信息的总数
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countCase(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 查询对应的人员信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchCase(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                          HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计对应的设备信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countDevice(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 查询对应的设备
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchDevice(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                            HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计qq用户的信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countQQUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * es查询QQ用户信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchQQUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                            HighlightBuilder hb, Integer from, Integer size);

    /**
     * 查询QQ群数量
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countQQTroop(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 查询QQ群数据
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchQQTroop(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                             HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计wx用户
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countWXUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 搜索wx用户
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchWXUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                            HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计wx群
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countWXChatroom(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     *     /**    SearchHits searchWxChatroom(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchWxChatroom(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                HighlightBuilder hb, Integer from, Integer size);

    /**
     * 统计通讯录信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @return 返回查询结果
     */
    Long countContactPhoneNum(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb);

    /**
     * 查询通讯录基本信息
     * @param indexName index
     * @param typeName type
     * @param mmqb 查询条件
     * @param rqb 时间条件
     * @param hb 高亮字段
     * @param from 开始
     * @param size 查询条数
     * @return 返回查询结果
     */
    SearchHits searchContactPhoneNum(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                     HighlightBuilder hb, Integer from, Integer size);

    /**
     * 根据条件查询
     * @param indexName index
     * @param typeName type
     * @param query 查询条件
     * @return 返回结果
     */
    SearchHits searchPre(String indexName, String typeName, PrefixQueryBuilder query);


}
