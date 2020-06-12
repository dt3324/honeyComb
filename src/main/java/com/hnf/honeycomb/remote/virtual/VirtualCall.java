package com.hnf.honeycomb.remote.virtual;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/16 15:39
 */
public interface VirtualCall {
    /**
     * 通过对应的条件查询出其所有的基本信息
     *
     * @param userId    用户ID
     * @param searchNum 对应的搜索账号
     * @param type      搜索的类型
     * @return
     */
    List<Map<String, Object>> findPersonExtendsInfoBySearchNum(Integer userId, String searchNum, String type);


    List<Document> getQQRemark(String searchNum);

    /**
     * 查询电话号码的备注
     *
     * @param searchNum
     * @return
     */
    List<Document> getPhoneRemark(String searchNum);

    /**
     * 查询WX的备注
     *
     * @param searchNum
     * @return
     */
    List<Document> getWXRemark(String searchNum);


    List<Document> getQQFlockRemark(String searchNum);

    List<Document> findQqMsg(String searchNum);

    List<Document> findWxMsg(String searchNum);

    List<Document> findPhoneMsg(String searchNum);

    List<Document> findPapersMsg(String searchNum);

//    List<Document> queryPersonByVirtual(String searchNum);

    List<Document> queryPhoneToVirtual(String phoneNum);

    List<Document> getPhoneRemarkNew(String searchNum);
}
