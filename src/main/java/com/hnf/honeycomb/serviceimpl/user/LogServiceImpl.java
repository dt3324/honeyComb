package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.dao.BaseDao;
import com.hnf.honeycomb.dao.UserMongoDao;
import com.hnf.honeycomb.service.DeviceService;
import com.hnf.honeycomb.service.user.LogService;
import com.hnf.honeycomb.util.BuilderMap;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 审计管理业务层接口实现
 *
 * @author zhouhong
 */
@Service("logService")
public class LogServiceImpl implements LogService {

    @Resource
    private UserMongoDao userMongoDao;

    @Autowired
    private BaseDao baseDao;


    @Override
    public Map findAllLog(String searchContent, String startDate, String endDate, String departmentCode) throws Exception {
        Pattern searchPattern = null;
        Pattern unitPattern = Pattern.compile("^.*" + StringUtils.getOldDepartmentCode(departmentCode) + ".*$", Pattern.CASE_INSENSITIVE);
        if (!StringUtils.isEmpty(searchContent)) {
            searchPattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
        }
        //返回的数据
        Map allInfo = new HashMap<>();

        //装登录日志的对象
        Map loginInfo = new HashMap<>();

        //装所有返回数据的集合
        Map array = new HashMap<>();

        //装所有数据的map对象
        Map allObject = new HashMap<>();
        BasicDBObject timeQuery = new BasicDBObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date sDate, eDate;
        //判断时间
        if (!StringUtils.isEmpty(startDate)) {
            sDate = formatter.parse(startDate);
            timeQuery.append("$gte", sDate);
        }
        if (!StringUtils.isEmpty(endDate)) {
            eDate = formatter.parse(endDate);
            eDate.setDate(eDate.getDate() + 1);
            timeQuery.append("$lte", eDate);
        }
        //登录日志
        BasicDBObject loginQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            loginQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            loginQuery.append("loginTime", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            loginQuery.append("loginUnitType", unitPattern);
        }
        Long loginCount = userMongoDao.count("logData", "userlogin", loginQuery);
        if (loginCount != 0) {
            loginInfo.put("text", "用户登录日志" + "(" + loginCount + ")");
            loginInfo.put("type", "01");
            array.put("loginInfo", loginInfo);
        }

        //装注销日志的对象
        Map logoutInfo = new HashMap<>();
        BasicDBObject logoutQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            logoutQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            logoutQuery.append("logoutTime", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            logoutQuery.append("logoutUnitType", unitPattern);
        }
        Long logoutCount = userMongoDao.count("logData", "userlogout", logoutQuery);
        if (logoutCount != 0) {
            logoutInfo.put("text", "用户注销日志" + "(" + logoutCount + ")");
            logoutInfo.put("type", "02");
            array.put("logoutInfo", logoutInfo);
        }

        //时空伴随数据
        Map spaceInfo = new HashMap<>();
        BasicDBObject spaceQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            spaceQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            spaceQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            spaceQuery.append("searchUnitType", unitPattern);
        }
        Long spaceCount = userMongoDao.count("logData", "space", spaceQuery);
        if (spaceCount != 0) {
            spaceInfo.put("text", "时空分析日志" + "(" + spaceCount + ")");
            spaceInfo.put("type", "03");
            array.put("spaceInfo", spaceInfo);
        }

        //一键搜日志
        Map searchInfo = new HashMap<>();
        BasicDBObject searchQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            searchQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            searchQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            searchQuery.append("searchUnitType", unitPattern);
        }
        Long searchCount = userMongoDao.count("logData", "search", searchQuery);
        if (searchCount != 0) {
            searchInfo.put("text", "一键搜日志" + "(" + searchCount + ")");
            searchInfo.put("type", "04");
            array.put("searchInfo", searchInfo);
        }

        //关联关系日志
        Map relationInfo = new HashMap<>();
        BasicDBObject relationQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            relationQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            relationQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            relationQuery.append("searchUnitType", unitPattern);
        }
        Long relationCount = userMongoDao.count("logData", "relation", relationQuery);
        if (relationCount != 0) {
            relationInfo.put("text", "关联关系日志" + "(" + relationCount + ")");
            relationInfo.put("type", "05");
            array.put("relationInfo", relationInfo);
        }

        //QQ备注日志
        Map qQremarkInfo = new HashMap<>();
        BasicDBObject remarkQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            remarkQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            remarkQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            remarkQuery.append("searchUnitType", unitPattern);
        }
        Long qQremarkCount = userMongoDao.count("logData", "qqremark", remarkQuery);
        if (qQremarkCount != 0) {
            qQremarkInfo.put("text", "QQ备注日志" + "(" + qQremarkCount + ")");
            qQremarkInfo.put("type", "06");
            array.put("QQremarkInfo", qQremarkInfo);
        }

        //手机画像日志
        Map mobilePortraitsInfo = new HashMap<>();
        BasicDBObject mobileQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            mobileQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            mobileQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            mobileQuery.append("searchUnitType", unitPattern);
        }
        Long mobilePortraitsCount = userMongoDao.count("logData", "mobileportraits", mobileQuery);
        if (mobilePortraitsCount != 0) {
            mobilePortraitsInfo.put("text", "手机画像日志" + "(" + mobilePortraitsCount + ")");
            mobilePortraitsInfo.put("type", "07");
            array.put("mobilePortraitsInfo", mobilePortraitsInfo);
        }

        //设备数据查看日志
        Map deviceInfo = new HashMap<>();
        BasicDBObject deviceQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            deviceQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            deviceQuery.append("searchDate", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            deviceQuery.append("searchUnitType", unitPattern);
        }
        Long deviceCount = userMongoDao.count("logData", "deviceinfo", deviceQuery);
        if (deviceCount != 0) {
            deviceInfo.put("text", "设备数据查看日志" + "(" + deviceCount + ")");
            deviceInfo.put("type", "08");
            array.put("deviceInfo", deviceInfo);
        }

        //用户管理日志
        BasicDBObject userQuery = new BasicDBObject();
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            userQuery.append("policeNumber", searchPattern);
        }
        //判断时间
        if (!timeQuery.isEmpty()) {
            userQuery.append("operateTime", timeQuery);
        }
        //判断单位
        if (!StringUtils.isEmpty(departmentCode)) {
            userQuery.append("operateUnit", unitPattern);
        }
        Long userManageCount = userMongoDao.count("logData", "userModify", userQuery);
        if (userManageCount > 0) {
            array.put("userModifyInfo", BuilderMap.of("text", "用户管理日志(" + userManageCount + ")").put("type", "09").get());
        }
        allObject.put("nodes", array);
        allObject.put("text", "日志信息");

        allInfo.put("allObject", allObject);
        return allInfo;
    }

    @Override
    public Map<String, Object> findLogByType(Integer type, Integer pageNumber, String searchContent, String startDate,
                                             String endDate, String departmentCode) throws Exception {
        Pattern unitPattern = Pattern.compile("^.*" + StringUtils.getOldDepartmentCode(departmentCode) + ".*$", Pattern.CASE_INSENSITIVE);
        Map<String, Object> map = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        BasicDBObject timeQuery = new BasicDBObject();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date sDate, eDate;
        //判断时间
        if (!StringUtils.isEmpty(startDate)) {
            sDate = formatter.parse(startDate);
            timeQuery.append("$gte", sDate);
        }
        if (!StringUtils.isEmpty(endDate)) {
            eDate = formatter.parse(endDate);
            eDate.setDate(eDate.getDate() + 1);
            timeQuery.append("$lte", eDate);
        }
        //判断搜索内容
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("policeNumber", pattern);
        }
        switch (type) {
            //登录日志
            case 1:
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("loginUnitType", unitPattern);
                }
                if (!timeQuery.isEmpty()) {
                    query.append("loginTime", timeQuery);
                }
                Long loginCount = userMongoDao.count("logData", "userlogin", query);
                List<DBObject> loginList = userMongoDao.find("logData", "userlogin", query, pageNumber, PAGESIZE, new BasicDBObject("loginTime", -1));
                map.put("count", loginCount);
                map.put("list", loginList);
                break;
            //注销日志
            case 2:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("logoutTime", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("logoutUnitType", unitPattern);
                }
                Long logoutCount = userMongoDao.count("logData", "userlogout", query);
                List<DBObject> logoutList = userMongoDao.find("logData", "userlogout", query, pageNumber, PAGESIZE, new BasicDBObject("logoutTime", -1));
                map.put("count", logoutCount);
                map.put("list", logoutList);
                break;
            //时空分析日志
            case 3:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long spaceCount = userMongoDao.count("logData", "space", query);
                List<DBObject> spaceList = userMongoDao.find("logData", "space", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                map.put("count", spaceCount);
                map.put("list", spaceList);
                break;
            //一键搜日志
            case 4:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long searchCount = userMongoDao.count("logData", "search", query);
                List<DBObject> searchList = userMongoDao.find("logData", "search", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                map.put("count", searchCount);
                map.put("list", searchList);
                break;
            //关系碰撞日志
            case 5:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long relationCount = userMongoDao.count("logData", "relation", query);
                map.put("count", relationCount);
                List<DBObject> relationList = userMongoDao.find("logData", "relation", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                // 所有的设备唯一识别号
                Set<String> deviceUniques = new HashSet<>();
                relationList.parallelStream().forEach(device -> deviceUniques.addAll(Arrays.asList(getString(device.get("searchContent")).split(","))));
                List<Document> devices = baseDao.listQuery("infoData2", "t_device"
                        , new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, deviceUniques)));
                // 所有的唯一识别号到设备名称键值对
                Map<String, String> uni2name = new HashMap<>();
                devices.parallelStream().forEach(perDevice -> uni2name.put(perDevice.getString("device_unique"), perDevice.getString("devicename")));
                // 将设备唯一识别号替换为设备名称
                relationList.parallelStream().forEach(relation -> {
                    List<String> perDeviceUniques = Arrays.asList(getString(relation.get("searchContent")).split(","));
                    StringBuilder deviceNames = new StringBuilder();
                    perDeviceUniques.forEach(deviceUnique -> {
                        String s = uni2name.get(deviceUnique);
                        if(!StringUtils.isEmpty(s)){
                            deviceNames.append(s).append("，");
                        }
                    });
                    relation.put("searchContent", deviceNames.toString());
                });
                map.put("list", relationList);
                break;
            //虚拟身份日志
            case 6:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long qQremarkCount = userMongoDao.count("logData", "qqremark", query);
                List<DBObject> qqremarkList = userMongoDao.find("logData", "qqremark", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                map.put("count", qQremarkCount);
                map.put("list", qqremarkList);
                break;
            //手机画像日志
            case 7:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long mobilePortraitsCount = userMongoDao.count("logData", "mobileportraits", query);
                List<DBObject> mobileList = userMongoDao.find("logData", "mobileportraits", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                map.put("count", mobilePortraitsCount);
                map.put("list", mobileList);
                break;
            //设备数据查看日志
            case 8:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("searchDate", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("searchUnitType", unitPattern);
                }
                Long deviceCount = userMongoDao.count("logData", "deviceinfo", query);
                List<DBObject> deviceList = userMongoDao.find("logData", "deviceinfo", query, pageNumber, PAGESIZE, new BasicDBObject("searchDate", -1));
                map.put("count", deviceCount);
                map.put("list", deviceList);
                break;//设备数据查看日志
            case 9:
                //判断时间
                if (!timeQuery.isEmpty()) {
                    query.append("operateTime", timeQuery);
                }
                //判断单位
                if (!StringUtils.isEmpty(departmentCode)) {
                    query.append("operateUnit", unitPattern);
                }
                Long userManageCount = userMongoDao.count("logData", "userModify", query);
                List<DBObject> modifyList = userMongoDao.find("logData", "userModify", query, pageNumber, PAGESIZE, new BasicDBObject("operateTime", -1));
                map.put("count", userManageCount);
                map.put("list", modifyList);
                break;
            default:
                throw new RuntimeException("非法操作");
        }
        return map;
    }

}
