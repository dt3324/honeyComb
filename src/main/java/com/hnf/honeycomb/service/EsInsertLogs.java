package com.hnf.honeycomb.service;

/**
 * @author lsj
 * @date 2018/10/8
 * 插入日志接口
 */
public interface EsInsertLogs {
    /**
     * 插入日志信息
     *
     * @param userId        用户id
     * @param place         ip地址
     * @param searchContent 搜索内容
     */
    void insertSearchLog(Integer userId, String place, String searchContent);
}
