package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.config.NoSqlOperation;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import com.hnf.honeycomb.service.GisInsertLogs;
import com.hnf.honeycomb.util.UtilMain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 插入日志实现类
 */
@Service
public class GisInsertLogsImpl implements GisInsertLogs {
    @Resource
    private NoSqlOperation noSqlOperation;
    @Resource
    private BusinessUserMapper businessUserMapper;

    @Override
    public void insertSearchLog(String userId, String place, String searchNum, String type) {
        Map userBean = null;
        if (userId != null) {
            userBean = businessUserMapper.findById(Integer.valueOf(userId.trim()));
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
            case "wx":
                searchType = "wx";
                break;
            case "deviceUnique":
                searchType = "deviceUnique";
                break;
            default:
                searchType = "unknown";
                break;
        }
        if (!searchNum.isEmpty() && userBean != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("policeNumber", userBean.get("policenumber"));
            map.put("searchContent", searchNum);
            map.put("searchDate", new Date());
            map.put("searchIp", place);
            map.put("searchType", searchType);
            map.put("searchAll", searchNum + " " + userBean.get("policenumber"));
            map.put("searchUnitType", userBean.get("department_code"));
            noSqlOperation.insertDocumentSpace(map);
        }
    }
}
