package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 用于备注的service接口
 *
 * @author yy
 */
public interface VirtualRemarkService {
    /**
     * @param userId
     * @param searchNum
     * @param type
     * @param place
     * @return
     */
    Map<String, Object> findRemark(Integer userId, String searchNum, String type, String place);

    /**
     * 通过对应的条件查询出其所有的基本信息
     *
     * @param userId    用户ID
     * @param searchNum 对应的搜索账号
     * @param type      搜索的类型
     * @param place     对应的IP地址
     * @return
     */
    List<Map<String, Object>> findPersonExtendsInfoBySearchNum(Integer userId, String searchNum, String type, String place);

    /**
     * 通过用户账号查询对应的人员基本信息
     *
     * @param num
     * @return
     */
    Document findPersonBaseInfoByUNumber(String num);
}
