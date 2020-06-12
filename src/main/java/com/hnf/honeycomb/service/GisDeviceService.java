package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author hnf
 */
public interface GisDeviceService {

    /**
     * 通过对应的QQ账号查询对应的所属设备的IMEI及MAC
     *
     * @param num  对应的账号
     * @param type 对应的类型
     * @return 返回imei及mac
     */
    Map<String, HashSet<String>> findImeiAndMacByNumAndType(String num, String type);

    /**
     * 通过设备deviceUnique查询对应的imei及mac
     *
     * @param deviceUnique 设备唯一标识
     * @return 对应的imei及mac
     */
    Map<String, HashSet<String>> findImeiAndMacByDeviceUnqiue(List<Document> deviceUnique);

    /**
     * 通过对应的账号以及对应的消息类型对数据进行模糊匹配
     *
     * @param number 对应的账号
     * @param type   对应的类型
     * @return
     */
    HashSet<String> findLikeNumberInfo(String number, String type);

    void test();
}
