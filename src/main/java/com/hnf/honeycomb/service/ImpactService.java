package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 用于关系碰撞的相关方法的service类
 *
 * @author yy
 */
public interface ImpactService {

    /**
     * 保存关系碰撞的历史信息
     *
     * @param userId    对应的用户id
     * @param type      对应的保存类型
     * @param searchNum 对应的关系碰撞号码
     * @param time      对应的关系碰撞时间
     * @param project   对应的保存名字
     * @param explain   对应的保存说明
     */
    Integer insertImpactHistory(Integer userId, String type, String searchNum, String time, String project, String explain);

    /**
     * 通过对应的用户ID以及各种条件查询对应的方法
     *
     * @param userId    对应的用户ID
     * @param project   对应搜索的类型名
     * @param searchNum 对应的搜索账号
     * @param page      页数
     * @param pageSize  页码
     * @return 返回对应的结果
     */
    List<Document> findImpactHistoryByUserId(Integer userId, String project, String searchNum, Integer page, Integer pageSize);

    /**
     * 修改对应的碰撞历史
     *
     * @param field
     */
    Integer updateImpactHistory(String unique, String explain, String project, String searchNum);

    /**
     * 删除对应的碰撞历史
     *
     * @param field
     */
    Integer deleteImpactHistory(String field);

    /**
     * 精确查询对应的数据
     *
     * @param userId    用户id
     * @param searchNum 对应的账号
     * @return
     */
    List<Document> findImpactHistoryByUIdAndSearchNum(Integer userId, String searchNum);

    /**
     * 通过对应的碰撞类型以及对应的碰撞账号对对应的搜索账号进行对应的关系碰撞
     *
     * @param searchType   对应的账号搜索类型
     * @param userId       对应搜索的用户ID
     * @param searchNumber 对应的搜索账号
     * @return
     */
    Map<String, Object> impactByNumbersAndSearchType(String searchType, Integer userId, String searchNumber, String place);

    /**
     * 关系碰撞搜索功能中搜索号码信息
     *
     * @param searchType
     * @param searchNumber
     * @return List<Map < String, Object>>
     * @throws
     */
    List<Map<String, Object>> findNumInfo(String searchType, String searchNumber);
}
