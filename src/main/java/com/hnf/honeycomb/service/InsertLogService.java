package com.hnf.honeycomb.service;

/**
 * 用于插入对应各种操作日志文件的操作
 *
 * @author yy
 */
public interface InsertLogService {
    /**
     * 用于插入对应时空查询的日志文件
     *
     * @param id        用户的id
     * @param place     搜索的ip
     * @param searchNum 搜索的账号
     * @param type      搜索的账号类型
     */
    void insertGeoLog(Integer id, String place, String searchNum, String type);

    /**
     * 用于插入对应关系碰撞的日志文件
     *
     * @param id        用户的id
     * @param place     搜索的ip
     * @param searchNum 搜索的账号
     * @param type      搜索的账号类型
     */
    void insertRelationLog(Integer id, String place, String searchNum, String type);

    /**
     * 用于插入一键搜的日志文件
     *
     * @param id            用户的id
     * @param place         搜索的ip
     * @param searchNum     搜索的账号
     * @param searchContent 搜索内容
     */
    void insertSearchLog(Integer id, String place, String searchContent);

    /**
     * 插入备注日志
     *
     * @param id
     * @param place
     * @param searchNum
     * @param type
     */
    void insertRemarkLog(Integer id, String place, String searchNum, String type);

}
