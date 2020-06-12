package com.hnf.honeycomb.serviceimpl;


import com.hnf.honeycomb.config.NoSqlOperation;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import com.hnf.honeycomb.service.InsertLogService;
import com.hnf.honeycomb.util.UtilMain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hnf
 */
@Service("insertLogService")
public class InsertLogServiceImpl implements InsertLogService {
    @Resource
    private NoSqlOperation noSqlOperation;
    @Resource
    private BusinessUserMapper businessUserMapper;


    // 用于插入对应时空的Log日志
    @Override
    public void insertGeoLog(Integer id, String place, String searchNum, String type) {
        Map userMap = null;
        if (id != null) {
            userMap = businessUserMapper.findById(id);
        }
        String searchType = "";
        switch (type) {
            case UtilMain.IMEI:
                searchType = "imei";
                break;
            case UtilMain.MAC:
                searchType = "mac";
                break;
            case "qq":
                searchType = "qq";
                break;
            case "phone":
                searchType = "telephone";
                break;
            case "weixin":
                searchType = "wx";
                break;
            case "deviceUnique":
                searchType = "deviceUnique";
                break;
            default:
                searchType = "unknown";
                break;
        }
        if (!searchNum.isEmpty() && userMap != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", userMap.get("policenumber"));
//			map.put("policeNumber", userBean.getPolicenumber());
            map.put("searchContent", searchNum);
            map.put("searchDate", new Date());
            map.put("searchIp", place);
            map.put("searchType", searchType);
            map.put("searchAll", searchNum + " " + userMap.get("policenumber"));
            map.put("searchUnitType", userMap.get("department_code"));
            noSqlOperation.insertDocumentSpace(map);
        }
    }

    @Override
    public void insertRelationLog(Integer id, String place, String searchNum, String type) {
        Map user = null;
        if (id != null) {
            user = businessUserMapper.findById(id);
        }
        String searchType = "";
        switch (type) {
            case "qq":
                searchType = "qq";
                break;
            case "phone":
                searchType = "phone";
                break;
            case "wx":
                searchType = "wx";
                break;
            case "papersNum":
                searchType = "证件号";
                break;
            case "qqtroop":
                searchType = "qq群";
                break;
            case "wxtroop":
                searchType = "微信群";
                break;
            default:
                searchType = "未知";
                break;
        }
        if (searchNum != null && !searchNum.isEmpty() && user != null) {

            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", user.get("policenumber"));
            map.put("searchContent", searchNum);
            map.put("searchDate", new Date());
            map.put("searchIp", place);
            map.put("searchType", searchType);
            map.put("searchAll", searchNum + " " + user.get("policenumber"));
            map.put("searchUnitType", user.get("department_code"));
            noSqlOperation.insertDocumentRelation(map);
        }

    }

    @Override
    public void insertSearchLog(Integer id, String place, String searchContent) {
        Map userBean = null;
        if (id != null) {
            userBean = businessUserMapper.findById(id);
        }

        if (!searchContent.isEmpty() && userBean != null) {

            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", userBean.get("policenumber"));
            map.put("searchContent", searchContent);
            map.put("searchDate", new Date());
            map.put("searchIp", place);
            map.put("searchAll", searchContent + " " + userBean.get("policenumber"));
            map.put("searchUnitType", userBean.get("department_code"));
            try {
                noSqlOperation.insertDocumentSearch(map);
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

    }

    /**
     * 插入备注日志
     */
    @Override
    public void insertRemarkLog(Integer id, String searchNum, String type, String place) {
        Map bean = null;
        if (id != null) {
            bean = businessUserMapper.findById(id);
        }
        String searchType = "";
        switch (type) {
            case "qq":
                searchType = "qq";
                break;
            case "phone":
                searchType = "telephone";
                break;
            case "wx":
                searchType = "wx";
                break;
            case "papers":
                searchType = "papers";
                break;
            default:
                searchType = "unknown";
                break;
        }
        if (!searchNum.isEmpty() && bean != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", bean.get("policenumber"));
            map.put("searchDate", new Date());
            map.put("searchIp", place);
            map.put("searchContent", searchNum);
            map.put("searchType", searchType);
            map.put("searchAll", searchNum + " " + bean.get("policenumber"));
            map.put("searchUnitType", bean.get("department_code"));
            noSqlOperation.insertDocumentQqRemark(map);
        }
    }
}
