package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.remote.user.BusinessDepartmentMapper;
import com.hnf.honeycomb.remote.user.BusinessDeviceMapper;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.service.DeviceService;
import com.hnf.honeycomb.util.*;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.operation.AggregateOperation;
import org.bson.Document;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;

/**
 * 设备数据业务层接口实现
 *
 * @author zhouhong
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMongoDao deviceMongoDao;
    @Resource
    private BusinessDeviceMapper businessDeviceMapper;
    @Resource
    private BusinessUserMapper businessUserMapper;
    @Resource
    private BusinessDepartmentMapper businessDepartmentMapper;
    private final DepartmentMapper departmentMapper;

    @Resource
    private RedisUtilNew redisUtilNew;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);
    /**redis中存的名称*/
    private static String redisDeviceName = "/device/findAll";
    static String redisPersonName = "/person/queryAll";
    static String redisCaseName = "/case/queryAll";

    private boolean isFirstRun = true;

    @Autowired
    public DeviceServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public Map<String, Object> findDeviceBySomeTerms(Integer page, String deviceName, Integer deviceType
            , String startTime, String endTime, String policeNumber
            , String departmentCode, Integer mineOnly
            , Integer qq, Integer wx, Integer phone, Integer gps, Integer collType, String queryPoliceNumber) {
        //redis中存的名称
        //每次查询的所有条件组为key
        String keyNameCount = page + policeNumber + departmentCode + deviceName + deviceType + startTime + endTime +
                mineOnly + qq + wx + phone + gps + collType + queryPoliceNumber + "count";
        String keyNameList = page + policeNumber + departmentCode + deviceName + deviceType + startTime + endTime +
                mineOnly + qq + wx + phone + gps + collType + queryPoliceNumber + "List";
        // 从MySql同步数据 没有新增数据返回false
//        boolean resetRedis = synchronizeFetchLogToMongoDb();
        //fetch log中新增的数据把部门名称添加上 并清空redis
        synchronizeAndCleanRedis();
        if (page == null) {
            throw new RuntimeException("传入的页码数为空");
        }
        //查询条件
        BasicDBObject query = new BasicDBObject();
        if (deviceName != null && !deviceName.trim().isEmpty()) {

            //正则表达式需要处理特殊符号
            deviceName = dispose(deviceName);
            Pattern pattern = Pattern.compile("^.*" + deviceName.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.put("devicename", pattern);
        }
        //部门编号
        if (!StringUtils.isEmpty(departmentCode)) {
            departmentCode = StringUtils.getOldDepartmentCode(departmentCode.trim());
            Pattern pattern = Pattern.compile("^" + departmentCode + ".*$", Pattern.CASE_INSENSITIVE);
            BasicDBList departmentQuery = new BasicDBList();
            BasicDBObject queryCode = new BasicDBObject().append("department_code", pattern);
            departmentQuery.add(new BasicDBObject().append("department_code", null));
            departmentQuery.add(queryCode);
            query.append("$or", departmentQuery);
        } else {
            throw new RuntimeException("部门账号为空");
        }

        //获取redis中存的这个接口的所有数据
        Map<String, Object> redisDevicesMap = (Map<String, Object>) redisUtilNew.get(DeviceServiceImpl.redisDeviceName);
        //如果是第一次 避免空指针需要给它new一个
        if (redisDevicesMap == null) {
            redisDevicesMap = new HashMap<>(5);
        }
        //获取redis中存的该条件的总数
        Long count = (Long) redisDevicesMap.get(keyNameCount);
        //获取redis中存的该条件下的 设备数据
        List<Document> devices = (List<Document>) redisDevicesMap.get(keyNameList);
        Map<String, Object> result = new HashMap<>(3);
        //如果redis中存在数据  直接将数据返回
        if (count != null && devices != null) {
            result.put("count", count);
            result.put("devices", devices);
            result.put("totalPage", Math.ceil(count / 50d));
            return result;
        }
        //如果redis中不存在数据再进行下一步查询
        //时间条件处理
        if (!StringUtils.isEmpty(startTime) || !StringUtils.isEmpty(endTime)) {
            BasicDBObject timeQuery = new BasicDBObject();
            if (!StringUtils.isEmpty(startTime)) {
                Long startTimeMS = TimeUtils.getLongFromDateStr(startTime.trim());
                timeQuery.append("$gte", startTimeMS);
            }
            if (!StringUtils.isEmpty(endTime)) {
                Long endTimeMS = TimeUtils.getAddLongFromDateStr(endTime.trim());
                timeQuery.append("$lte", endTimeMS);
            }
            query.append("fetchtime", timeQuery);
        }
        if (qq != null) {
            query.append("qq", 1);
        }
        if (wx != null) {
            query.append("wx", 1);
        }
        if (phone != null) {
            query.append("phone", 1);
        }
        if (gps != null) {
            query.append("gps", 1);
        }
        if (collType != null) {
            query.append("collType", collType);
        }
        //仅查看个人采集数据
        if (policeNumber != null && mineOnly != null && mineOnly > 0 && !policeNumber.trim().isEmpty()) {
            query.append("key_id", policeNumber.trim());
        } else if (queryPoliceNumber != null && !queryPoliceNumber.trim().isEmpty()) {
            query.append("key_id", queryPoliceNumber.trim());
        }
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("device_unique", "$device_unique"))
                .append("count", new BasicDBObject("$sum", 1))
        );
        //如果redis中不存在就去数据库查询
        if (count == null) {
            count = deviceMongoDao.countByQueryAndDBAndCollName("infoData2", "fetchlog",
                    Arrays.asList(new BasicDBObject("$match", query), group));
            //把查询的数据添加到redis中的map中 redisFetchChangeName : { redisDevicesName : {keyNameCount ,value}}
            //redisDevicesName中放的keyNameCount
            redisDevicesMap.put(keyNameCount, count);
        }
        BasicDBObject group1 = new BasicDBObject("$group", new BasicDBObject("_id", "$device_unique")
                .append("fetchtime", new BasicDBObject("$max", "$fetchtime"))
        );
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("fetchtime", -1));
        BasicDBObject start = new BasicDBObject("$skip", (page - 1) * 50);
        BasicDBObject size = new BasicDBObject("$limit", 50);
        logger.info("查询条件query是{}", query);
        //如果不存在就去数据库查
        if (devices == null) {
            devices = deviceMongoDao.aggregateDeviceByGatheNameAndDBNameAndQuery("infoData2",
                    "fetchlog", Arrays.asList(new BasicDBObject("$match", query),
                            group1, sort, start, size));
            //redisDevicesName中放的keyNameList
            redisDevicesMap.put(keyNameList, devices);
            //redis中放的redisFetchChangeName
            redisUtilNew.set(DeviceServiceImpl.redisDeviceName, redisDevicesMap);
        }
        logger.info("查询到的设备为{},其设备数量为:{}", devices, devices.size());
        // 查询完整的单位信息
        if (!CollectionUtils.isEmpty(devices)) {
            logger.info("没有加密狗号查询完成的单位信息--为设备添加单位名称");
            devices.forEach(t -> {
                Object selfDepartmentCode = t.get("department_code");
                Object selfDepartmentName = t.get("department_name");
                if (selfDepartmentCode != null && (selfDepartmentName == null || "".equals(selfDepartmentName))) {
                    String departmentName = businessDepartmentMapper.findDepartNameByDepartNum(selfDepartmentCode.toString());
                    t.put("department_name", departmentName);
                }
            });
        }
        result.put("count", count);
        result.put("devices", devices);
        result.put("totalPage", Math.ceil(count / 50d));
        return result;
    }

    private void synchronizeAndCleanRedis(){
        Long updateTime = (Long)redisUtilNew.get("mongodbUpdateTime");
        updateTime = updateTime == null ? 0 : updateTime;
        //获取mongo上面对应的采集信息最大id值
        Long maxMongoUpdateTime = deviceMongoDao.maxUpdateTime("infoData2", "fetchlog");
        if( isFirstRun || maxMongoUpdateTime > updateTime){
            if (isFirstRun){
                // 从 7 天前开始同步
                updateTime = Instant.now().minusSeconds(Duration.ofDays(7).getSeconds()).toEpochMilli();
                isFirstRun = false;
            }
            redisUtilNew.set("mongodbUpdateTime",maxMongoUpdateTime);
            //查询出有多少新添加的  把部门名称添加上
            BasicDBObject start = new BasicDBObject("$gt", updateTime).append("$lte", maxMongoUpdateTime);
            List<Document> query = deviceMongoDao.findInfoByGatherNameAndQueryAll("infoData2", "fetchlog", new BasicDBObject("_updateTime" ,start));
            for (Document document : query) {
                String departmentCode = document.getString("department_code");
                String department = document.getString("department_name");
                if(!StringUtils.isEmpty(department)){
                    continue;
                }
                DepartmentBean departmentBean = departmentMapper.findByDepartmentCode(departmentCode);
                if(departmentBean != null){
                    String departmentName = departmentBean.getDepartmentName();
                    deviceMongoDao.updateDocument(
                            "infoData2",
                            "fetchlog",
                            new Document("_id",document.get("_id"))
                            , new Document("department_name",departmentName));
                }
            }
            //将redis中存的所有 这个接口的数据全部清空
            redisUtilNew.remove(DeviceServiceImpl.redisDeviceName);
            redisUtilNew.remove(DeviceServiceImpl.redisCaseName);
            redisUtilNew.remove(DeviceServiceImpl.redisPersonName);
        }
    }

    /**
     * 处理字符串中与正则表达式冲突的特殊字符
     *
     * @param deviceName 需要处理的字符串
     * @return 处理结果
     */
    public static String dispose(String deviceName) {
        String[] strings = new String[]{"(", ")", "[", "]", "{", "}", ".","*","^"};
        for (String string : strings) {
            if (deviceName.contains(string)) {
                int a = deviceName.lastIndexOf(string);
                StringBuilder beforeStr = new StringBuilder(deviceName.substring(0, a));
                deviceName = beforeStr.append("\\").append(deviceName.substring(a)).toString();
            }
        }
        return deviceName;
    }

    /**
     * mongoDB同步采集数据
     */
    private boolean synchronizeFetchLogToMongoDb() {
        boolean resetRedis = false;
        //获取mongo上面对应的采集信息最大id值
        Long maxMongoId = deviceMongoDao.maxId("infoData2", "fetchlog");
        logger.info("maxMongoId=={}", maxMongoId);
        //通过最大id查询对应的信息 t_fetchlog
        Integer maxMysqlId = businessDeviceMapper.findMaxFetchLogId();
        if (maxMysqlId == null) {
            return false;
        }
        logger.info("maxMysqlId=={}", maxMysqlId);
        if (maxMongoId >= maxMysqlId) {
            return false;
        }
        //如果mongo中的数据较少,新增
        logger.info("into the synchronize data ");
        //对数据进行对应的入库操作
        //查询大蜜蜂设备采集日志
        List<Map<String, Object>> docs = businessDeviceMapper.findFetchLogMore(maxMongoId);
//        //小蜜蜂采集日志
//        List<Map<String, Object>> log = businessDeviceMapper.findMoreSmallFetchLog(maxMongoId);
//        docs.addAll(log);
        //执行入库操作
        if (docs.size() > 0) {
            resetRedis = true;
            List<String> deviceUniques = docs.stream()
                    .map(doc -> ObjectUtil.getString(doc.get("fk_device_unique")))
                    .distinct().filter(Objects::nonNull).collect(Collectors.toList());
            BasicDBObject query = new BasicDBObject("device_unique",
                    new BasicDBObject(QueryOperators.IN, deviceUniques.toArray(new String[]{})));
            List<Document> devices = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", query);
            //查询对应的app是否有数据
            List<Document> deviceApps = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_exist_app", query);
            //存储unique与对应app采集的关系
            Map<String, Document> uniqueToApps = new HashMap<>();
            for (Document apps : deviceApps) {
                uniqueToApps.put(apps.getString("device_unique"), apps);
            }
            Map<String, Document> uniqueToInfo = new HashMap<>();
            for (Document device : devices) {
                uniqueToInfo.put(device.getString("device_unique"), device);
            }
            List<Document> insertDocs = new ArrayList<>();
            for (Map<String, Object> doc : docs) {
                Document insertDoc = new Document();
                insertDoc.append("id", doc.get("_id"));
                insertDoc.append("fetchtime", doc.get("fetchtime"));
                insertDoc.append("device_unique", doc.get("fk_device_unique"));
                insertDoc.append("case_unique", doc.get("fk_case_unique"));
                insertDoc.append("key_id", doc.get("police_number"));
                insertDoc.append("department_code", doc.get("department_code"));
                insertDoc.append("department_name", doc.get("department_name"));
                insertDoc.append("collType", doc.get("collType"));
                Object deviceUniqueObj = doc.get("fk_device_unique");
                if (deviceUniqueObj == null) {
                    insertDocs.add(insertDoc);
                    continue;
                }

                String deviceUnique = deviceUniqueObj.toString();
                Document device = uniqueToInfo.get(deviceUnique);
                if (device != null) {
                    insertDoc.append("type", device.get("type"));
                    insertDoc.append("model", device.getString("model"));
                    insertDoc.append("brand", device.getString("brand"));
                    insertDoc.append("devicename", device.get("devicename"));
                    insertDoc.append("androidver", device.get("androidver"));
                }
                Document apps = uniqueToApps.get(deviceUnique);
                apps = apps != null ? apps : new Document();
                insertDoc.append("qq", getInteger(apps.getString("QQ"), 0));
                insertDoc.append("phone", getInteger(apps.getString("PH"), 0));
                insertDoc.append("wx", getInteger(apps.getString("WX"), 0));
                insertDoc.append("gps", getInteger(apps.getString("GPS"), 0));
                if (apps.getString("CALL_LOG") != null) {
                    //通话记录
                    insertDoc.append("callLog", getInteger(apps.getString("CALL_LOG"), 0));
                }
                if (apps.getString("SHORT_MESSAGE") != null) {
                    //短信记录
                    insertDoc.append("shortMessage", getInteger(apps.getString("SHORT_MESSAGE"), 0));
                }
                insertDocs.add(insertDoc);
            }
            deviceMongoDao.insertDocs("infoData2", "fetchlog", insertDocs);
            logger.info("同步数据完成--->>>>>");
        }
        return resetRedis;
    }

    @Override
    public Map findByDeviceUnique(String deviceUnique) {
        // TODO Auto-generated method stub
        Map<String, Object> deviceMap = new HashMap<>(2);
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique", deviceUnique);
        List<Document> caseList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_case", query);
        BasicDBObject query1 = new BasicDBObject();
        query1.append("device_unique", deviceUnique);
        List<Document> personList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_person", query1);
        deviceMap.put("case", caseList);
        deviceMap.put("person", personList);
        return deviceMap;
    }

    @Override
    public List<Map<String, Object>> queryByDeviceUnique(Integer userId, String deviceUnique, String place) {
        // TODO Auto-generated method stub
        Map userBean = businessUserMapper.findById(userId);
        if (deviceUnique != null && userBean != null) {
            HashMap<String, Object> basicData = new HashMap<>(8);
            BasicDBObject query = new BasicDBObject();
            query.append("device_unique", deviceUnique);
            List<Document> deviceData = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", query);
            if (!(deviceData != null && !deviceData.isEmpty())) {
                basicData.put("deviceData", null);
                basicData.put("policeNumber", userBean.get("policenumber"));

                basicData.put("searchContent", null);
                basicData.put("searchAll", null);

                basicData.put("searchDate", new Date());
                basicData.put("searchIp", place);
                basicData.put("searchType", "deviceInfo");

                basicData.put("searchUnitType", userBean.get("department_code"));
            } else {
                basicData.put("deviceData", deviceData);
                basicData.put("policeNumber", userBean.get("policenumber"));

                basicData.put("searchContent", (deviceData.get(0).get("devicename")));
                basicData.put("searchAll", (deviceData.get(0).get("devicename") + " " + userBean.get("policenumber")));


                basicData.put("searchDate", new Date());
                basicData.put("searchIp", place);
                basicData.put("searchType", "deviceInfo");

                basicData.put("searchUnitType", userBean.get("department_code"));
            }
            deviceMongoDao.insertOperationDocument("logData", "deviceinfo", basicData);
        }
        try {

            List<Map<String, Object>> directory = new ArrayList<>();
            Map<String, Object> personInfo = new HashMap<>(1);
            Map<String, Object> caseInfo = new HashMap<>(1);
            Map<String, Object> deviceInfo = new HashMap<>(1);

            personInfo.put("text", "人员信息");
            caseInfo.put("text", "案件信息");
            deviceInfo.put("text", "设备信息");

            List<Map<String, Object>> listArray = new ArrayList<>();

            Map<String, Object> bastInfo = new HashMap<>(2);

            listArray.add(personInfo);
            listArray.add(caseInfo);
            listArray.add(deviceInfo);

            bastInfo.put("text", "基本信息");
            bastInfo.put("nodes", listArray);

            Map<String, Object> contact = new HashMap<>(1);
            Map<String, Object> message = new HashMap<>(1);
            Map<String, Object> record = new HashMap<>(1);
            contact.put("text", "通讯录");
            List<Map<String, Object>> listArray1 = new ArrayList<>();
            BasicDBObject query = new BasicDBObject("device_unique", deviceUnique);
            BasicDBObject query2 = new BasicDBObject("deviceUnique", deviceUnique);
            List<Document> contactlist = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_contact_phonenum", query);
            if (contactlist.size() != 0) {
                listArray1.add(contact);
            }
            BasicDBList messageAll = deviceMongoDao.record("infoData", "message", deviceUnique);
            if (messageAll.size() != 0) {
                message.put("text", "短信息");
                listArray1.add(message);
            }
            List<Document> recordData = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData", "record", query2);
            if (recordData.size() != 0) {
                record.put("text", "通讯记录");
                listArray1.add(record);
            }

            Map<String, Object> stateObject = new HashMap<>(1);
            stateObject.put("expanded", "false");

            Map<String, Object> baseMessage = new HashMap<>(3);
            baseMessage.put("text", "基本通讯信息");
            baseMessage.put("nodes", listArray1);
            baseMessage.put("state", stateObject);


            List<Map<String, Object>> listArray2 = new ArrayList<>();
            List<Map<String, Object>> listArray3 = new ArrayList<>();
            List<Map<String, Object>> listArray4 = new ArrayList<>();

            List<Document> qqUser = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_qquser", query);


            if (qqUser.size() != 0) {
                for (Document aQqUser : qqUser) {
                    Map<String, Object> qqObject = new HashMap<>(2);
                    if (aQqUser.get("uin") != null) {
                        qqObject.put("text", aQqUser.get("uin"));
                        qqObject.put("type", "qq");
                        listArray2.add(qqObject);
                    }
                }
                Map<String, Object> qqMac = new HashMap<>(2);
                qqMac.put("text", "qq账号");
                qqMac.put("nodes", listArray2);
                listArray4.add(qqMac);
            }

            List<Document> wxUser = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_wxuser", query);

            if (wxUser.size() != 0) {
                for (Document aWxUser : wxUser) {
                    Map<String, Object> wxObject = new HashMap<>(2);
                    if (aWxUser.get("username") != null) {
                        wxObject.put("text", aWxUser.get("username"));
                        wxObject.put("type", "wx");
                        listArray3.add(wxObject);
                    }
                }
                Map<String, Object> wxMac = new HashMap<>(2);
                wxMac.put("text", "微信账号");
                wxMac.put("nodes", listArray3);
                listArray4.add(wxMac);
            }

            Map<String, Object> instantMes = new HashMap<>(2);
            instantMes.put("text", "即时通讯录");
            instantMes.put("nodes", listArray4);
            directory.add(bastInfo);

            if (contactlist.size() != 0 || messageAll.size() != 0 || recordData.size() != 0) {
                directory.add(baseMessage);
            }

            if (qqUser.size() != 0 || wxUser.size() != 0) {
                directory.add(instantMes);
            }
            return directory;

        } catch (Exception e) {
            logger.error("查询过程中出现异常",e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Object> findLogByDeviceUnique(String deviceUnique) {
        // TODO Auto-generated method stub
        if (deviceUnique == null) {
            throw new RuntimeException("查询的设备唯一标识为空");
        }
        //查询大蜜蜂采集信息
        List<Object> fetchLogs = businessDeviceMapper.findAboutFetchLogByDeviceUnique(deviceUnique.trim());
        List<Object> sBeeFetchLogs = businessDeviceMapper.findSBeeFetchDetailByDeviceUnique(deviceUnique.trim());
        fetchLogs.addAll(sBeeFetchLogs);
        return fetchLogs;
    }

    @Override
    public Boolean isNotCheck(String deviceUnique, String permission, String softDogOrPoliceNumber) {
        boolean isCheck = false;
        BasicDBObject query = new BasicDBObject();
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        query.put("device_unique", deviceUnique);
        if (!StringUtils.isEmpty(permission)) {
            Pattern pattern = Pattern.compile("^" + permission + ".*$", Pattern.CASE_INSENSITIVE);
            query.put("department_code", pattern);
        }
        if (!StringUtils.isEmpty(softDogOrPoliceNumber)) {
            String[] dogAndPnumber = softDogOrPoliceNumber.split(",");
            query.append("key_id", new BasicDBObject(QueryOperators.IN, Arrays.asList(dogAndPnumber)));
        }
        List<Document> deviceList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "fetchlog", query);
        if (!CollectionUtils.isEmpty(deviceList)) {
            isCheck = true;
        }
        return isCheck;
    }
}
