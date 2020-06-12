package com.hnf.honeycomb.remote.virtualimpl;

import com.hnf.honeycomb.remote.virtual.VirtualCall;
import com.hnf.honeycomb.util.BuilderMap;
import org.bson.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.CollectionUtils.ofMap;
import static com.hnf.honeycomb.util.HttpUtil.virtualCall;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/16 15:42
 */
@Repository
public class VirtualCallImpl implements VirtualCall {
    @Override
    public List<Map<String, Object>> findPersonExtendsInfoBySearchNum(Integer userId, String searchNum, String type) {
        return virtualCall("findDoc/findPersonExtendsInfoBySearchNum"
                , BuilderMap.of("searchNum", searchNum).put("type", type).get()
                , new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
    }

    @Override
    public List<Document> getQQRemark(String searchNum) {
        return virtualCall("findDoc/getQQRemark"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> getPhoneRemark(String searchNum) {
        return virtualCall("findDoc/getPhoneRemark"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> getPhoneRemarkNew(String searchNum) {
        return virtualCall("findDoc/getPhoneRemarkNew"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> getWXRemark(String searchNum) {
        return virtualCall("findDoc/getWXRemark"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> getQQFlockRemark(String searchNum) {
        return virtualCall("findDoc/getQQFlockRemark"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> findQqMsg(String searchNum) {
        return virtualCall("findDoc/findQqMsg"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> findWxMsg(String searchNum) {
        return virtualCall("findDoc/findWxMsg"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> findPhoneMsg(String searchNum) {
        return virtualCall("findDoc/findPhoneMsg"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

    @Override
    public List<Document> findPapersMsg(String searchNum) {
        return virtualCall("findDoc/findPapersMsg"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

//    @Override
//    public List<Document> queryPersonByVirtual(String searchNum) {
//        return virtualCall("findDoc/queryPersonByVirtual"
//                , ofMap("searchNum", searchNum)
//                , new ParameterizedTypeReference<List<Document>>() {
//                });
//    }

    @Override
    public List<Document> queryPhoneToVirtual(String searchNum) {
        return virtualCall("findDoc/queryPhoneToVirtual"
                , ofMap("searchNum", searchNum)
                , new ParameterizedTypeReference<List<Document>>() {
                });
    }

}
