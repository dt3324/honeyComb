package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.config.NoSqlOperation;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import com.hnf.honeycomb.service.EsInsertLogs;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lsj
 * @date 2018/10/8
 * 日志插入实现类
 */
@Service
public class EsInsertLogsImpl implements EsInsertLogs {
    @Resource
    private NoSqlOperation noSqlOperation;
    @Resource
    private BusinessUserMapper businessUserMapper;

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
}
