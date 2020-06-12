package com.hnf.honeycomb.serviceimpl;

import static com.hnf.honeycomb.util.ObjectUtil.getString;
import static com.hnf.honeycomb.util.ObjectUtil.getStringForLong;

import com.hnf.crypte.MD5Util;
import com.hnf.crypte.Utils;
import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.daoimpl.ImpactServerDao;
import com.hnf.honeycomb.daoimpl.ImpactSimpleDao;
import com.hnf.honeycomb.service.ImpactSimpleService;
import com.hnf.honeycomb.service.InsertLogService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.RedisUtilNew;
import com.hnf.honeycomb.util.TimeUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author hnf
 */
@Service("impactSimpleService")
public class ImpactSimpleServiceImpl implements ImpactSimpleService {

    private static Logger logger = LoggerFactory.getLogger(ImpactSimpleServiceImpl.class);

    @Resource
    private ImpactSimpleService impactSimpleService;

    @Autowired
    private RedisUtilNew redisUtilNew;

    @Autowired
    private ImpactServerDao impactServerDao;

    @Resource
    private InsertLogService insertLogService;

    @Resource
    private ImpactSimpleDao impactSimpleDao;

    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    @Lazy
    private MongoClient mongoClient;

    @Override
    public Map<String, Object> impactByQQNumbers(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "qq");
        Map<String, Object> result = new HashMap<>(4);
        result.put("relationType", "QQ");
        //总数限制条件
        countLimit = countLimit != null ? countLimit : 0;
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        // 所有的设备码
        List<String> uniquesList = new LinkedList<>();
        // 设备号对应的qq号 key-QQ value-设备
        Map<String, String> qqNumber2DeviceUnique = new HashMap<>();
        //设备对应设备名称
        Map<String, String> unique2deviceName = new HashMap<>();
        //所有的主控qq号
        List<String> mainQQNumbers = new ArrayList<>();
        ArrayList<String> qqList = new ArrayList<>();
        //装对应的节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //装对应的关系
        List<Map<String, Object>> relations = new ArrayList<>();

        Integer userCountLimit = countLimit;
        HashSet<String> addNodeDeviceUniques = new HashSet<>();
        //获取主控QQ号以及对应相关的信息
        this.extractQQNumberAndDevice2QQNumberByUniques(
                uniquesList, devices, qqNumber2DeviceUnique, unique2deviceName, mainQQNumbers, qqList);
        //获取人员信息，展示到页面需要
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);
        //碰撞共同QQ好友(好友关系，不含聊天)
        List<DBObject> impactResult = impactSimpleDao.impactByNumsAndType(mainQQNumbers, "QQ");
        //消息总数以及时间过滤条件
        BasicDBObject filterQuery = new BasicDBObject();
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(timeSelectType, startTime, endTime);
        //时间筛选条件
        if (!timeQuery.isEmpty()) {
            filterQuery.append("msgtime", timeQuery);
        }
        //此时返回结果
        if (CollectionUtils.isEmpty(impactResult)) {
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        //聊天信息唯一查询码，用于快速查询
        HashSet<String> c2cs = new HashSet<>();
        //拼接对应的查询条件
        impactResult.forEach(t -> {
            String friendQQNum = getString(t.get("_id"));
            //认识共同好友QQ号的人（qq）集合
            BasicDBList aList = (BasicDBList) t.get("value");
            for (Object a : aList) {
                c2cs.add(Utils.NumberStringUniqueMD5(getStringForLong(friendQQNum), getStringForLong(a)));
            }
        });

        if (!CollectionUtils.isEmpty(c2cs)) {
            filterQuery.append("c2cmsg_mark", new BasicDBObject(
                    QueryOperators.IN, c2cs));
        }

        Map<String, Integer> c2c2Count = new HashMap<>();
        //时间条件以及对应的范围进行查询
        List<Document> filterResult =
                impactSimpleDao.filterQQMsgImpactIsFriendResultByCountAndQuery(0, filterQuery);
        //获取c2c对电话号码的关系
        filterResult.forEach(t -> {
            String c2c = t.getString("_id");
            Integer oneCount = t.getInteger("count");
            c2c2Count.put(c2c, oneCount);
        });
        //设备唯一标识以及好友账号与聊天数的关系
        Map<String, Integer> deviceUniqueAndFriendNum2Count = new HashMap<>();
        //获取
        impactResult.forEach(t -> {
            //好友qq号码
            String friendQQNum = getString(t.get("_id"));
            //待碰撞的qq号码集合
            BasicDBList aList = (BasicDBList) t.get("value");
            //待碰撞QQ账号集合
            for (Object a : aList) {
                if (a != null) {
                    String deviceUnique = qqNumber2DeviceUnique.get(a);
                    String c2c = Utils.NumberStringUniqueMD5(getStringForLong(friendQQNum), getStringForLong(a));
                    Integer c2cCount = c2c2Count.get(c2c);
                    c2cCount = c2cCount != null ? c2cCount : 0;
                    String key = deviceUnique + "," + friendQQNum;
                    //代表一个设备下多个qq号在此群发言
                    if (deviceUniqueAndFriendNum2Count.get(key) != null) {
                        //说明联系了多次
                        Integer olderCount = deviceUniqueAndFriendNum2Count.get(key);
                        deviceUniqueAndFriendNum2Count.put(key, olderCount + c2cCount);
                    } else {
                        deviceUniqueAndFriendNum2Count.put(key, c2cCount);
                    }
                }
            }
        });

        //此时再次进行处理
        //遍历碰撞结果,组装成前端需要的格式
        impactResult.forEach(t -> {
            //保证deviceUniques不重复
            HashSet<String> relationDeviceUnqiues = new HashSet<>();
            //好友qq号码
            String friendQQNum = getString(t.get("_id"), " ");
            //待碰撞的qq号码集合
            BasicDBList aList = (BasicDBList) t.get("value");
            //遍历有关系的待碰撞电话号码集合
            aList.forEach(q -> {
                //获取qq号码所在的设备
                String device = qqNumber2DeviceUnique.get(q);
                //避免相同设备
                if (!relationDeviceUnqiues.contains(device)) {
                    relationDeviceUnqiues.add(device);
                }
            });
            logger.debug("friendQQNum:" + friendQQNum);
            logger.debug("relationDeviceUnqiues:" + relationDeviceUnqiues);
            //说明此QQ号是两个设备的deviceUnique
            if (relationDeviceUnqiues.size() > 1) {
                //存储需要连线的限制条件
                HashSet<String> filterDeviceUniques = new HashSet<>();
                for (Object oneQQUin : aList) {
                    String oneDeviceUnique = qqNumber2DeviceUnique.get(oneQQUin);
                    Integer count = deviceUniqueAndFriendNum2Count.get(oneDeviceUnique + "," + friendQQNum);
                    count = count != null ? count : 0;
                    if (count >= userCountLimit) {
                        filterDeviceUniques.add(oneDeviceUnique);
                    }
                }
                //代表有共同好友
                if (filterDeviceUniques.size() > 1) {
                    Map<String, String> numberNode = new HashMap<>();
                    numberNode.put("num", friendQQNum);//num连线规则 对应relation
                    numberNode.put("type", "elseNode");
                    numberNode.put("name", friendQQNum);//群号或群昵称根据需求
                    nodes.add(numberNode);
                    filterDeviceUniques.forEach(resultDeviceUnique -> {//增加对应的设备节点信息以及对应的relation
                        Map<String, Object> relation = new HashMap<>();//关系连线
                        relation.put("startNode", resultDeviceUnique);
                        relation.put("endNode", friendQQNum);
                        relation.put("LinkName", "qq");
                        Integer thisCount = deviceUniqueAndFriendNum2Count.get(
                                resultDeviceUnique + "," + friendQQNum);
                        relation.put("contactCount", thisCount != null ? thisCount : 0);
                        relations.add(relation);
                        if (!addNodeDeviceUniques.contains(resultDeviceUnique)) {//防止一个设备加入多个群时,添加多个节点
                            Map<String, String> oneNode = new HashMap<>();//设备节点连线
                            oneNode.put("num", resultDeviceUnique);
                            oneNode.put("type", "phone");
                            String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(resultDeviceUnique);
                            if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                oneNode.put("personName", stringsDeviceInfo[0]);
                                oneNode.put("personNumber", stringsDeviceInfo[1]);
                            }
                            oneNode.put("name", unique2deviceName.get(resultDeviceUnique));//根据需求调整显示名
                            nodes.add(oneNode);
                            addNodeDeviceUniques.add(resultDeviceUnique);
                        }
                    });
                }
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);
        return result;
    }

    /**
     * 将对应mapReduce获得的结果转换为需要的集合
     *
     * @param object 对应的BasicDBList
     * @return 返回对应的记过
     */
    private BasicDBList toNeedBasicDBList(Object object) {
        BasicDBList result = new BasicDBList();
        BasicDBList sourceList = (BasicDBList) object;
        for (Object a : sourceList) {
            if (a instanceof String) {
                result.add(a);
            }

            if (a instanceof BasicDBObject) {
                BasicDBObject aObject = (BasicDBObject) a;
                BasicDBList aaa = toNeedBasicDBList(aObject.get("rst"));
                result.addAll(aaa);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> impactByDepartment(String department, String type, Integer userId, String place, Long startTime,
                                                  Long endTime, Integer countLimit, Integer timeSelectType) {
        //查询出该单位下所有采集的设备并拼接字符串
        String oldDepartmentCode = com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(department);
        BasicDBObject query = new BasicDBObject("departmentCode", Pattern.compile("^" + oldDepartmentCode + ".*$"));
        FindIterable<Document> documents = mongoClient.getDatabase("infoData2")
                .getCollection("fetchlog").find(query)
                .projection(new BasicDBObject("device_unique", 1));
        if (documents == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        for (Document document : documents) {
            s.append(document.getString("device_unique")).append(",");
        }
        String deviceUniques = s.substring(0, s.length() - 1);
        switch (type) {
            case "qq":
                return impactByQQNumbers(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            case "qqTroop":
                return impactByQQtroop(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            case "wx":
                impactByWXNumbers(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            case "wxTroop":
                return impactByWXtroop(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            case "phone":
                return impactByPhoneAndIsFriend(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            case "phoneNotFriend":
                return impactByPhoneIsNotFriend(deviceUniques, userId, "", "", place, startTime, endTime, countLimit, timeSelectType);
            default:
                return null;
        }
    }

    @Override
    public Map<String, Object> impactByWXNumbers(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        //        if (userId == null) {
        //            throw new RuntimeException("对应的用户ID为null");
        //        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "wx");
        //返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "WX");
        //装对应的节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //装对应的关系
        List<Map<String, Object>> relations = new ArrayList<>();
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        // 所有的设备码
        List<String> uniquesList = new LinkedList<>();
        // 设备号对应的wx号 key-wx value-设备
        Map<String, String> wxNum2DeviceUnique = new HashMap<>();
        //设备对应设备名称
        Map<String, String> unique2deviceName = new HashMap<>();
        //所有的主控wx号
        List<String> mainWXNumbers = new ArrayList<>();
        ArrayList<String> wxList = new ArrayList<>();
        //总数限制条件
        countLimit = countLimit != null ? countLimit : 0;
        Integer userCountLimit = countLimit;
        HashSet<String> addNodeDeviceUniques = new HashSet<>();
        this.extractWXNumberAndDevice2WXNumberByUniques(
                uniquesList, devices, wxNum2DeviceUnique, unique2deviceName, mainWXNumbers, wxList);
        Long time1 = System.currentTimeMillis();
        List<DBObject> impactResult = impactSimpleDao.impactByNumsAndType(mainWXNumbers, "WX");
        Long time2 = System.currentTimeMillis();
        //无碰撞信息时
        if (CollectionUtils.isEmpty(impactResult)) {
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        Map<String, String[]> deviceUnique2PersonNameAndNumber = //设备唯一标识与人员姓名与身份证号的关系
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);
        //消息总数以及时间过滤条件
        BasicDBObject filterQuery = new BasicDBObject();
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);
        //时间筛选条件
        if (!timeQuery.isEmpty()) {
            filterQuery.append("msgtime", timeQuery);
        }
        //此时返回结果
        if (CollectionUtils.isEmpty(impactResult)) {
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        HashSet<String> c2cs = new HashSet<>();
        //拼接对应的查询条件
        impactResult.forEach(t -> {
            String friendQQNum = t.get("_id").toString();
            //待碰撞的wx号码
            BasicDBList aList = (BasicDBList) t.get("value");
            for (Object a : aList) {
                if (a != null) {
                    c2cs.add(Utils.StringUniqueMD5(friendQQNum, a.toString()));
                }
            }
        });
        if (!CollectionUtils.isEmpty(c2cs)) {
            filterQuery.append("c2cmark", new BasicDBObject(
                    QueryOperators.IN, c2cs));
        }
        Map<String, Integer> c2c2Count = new HashMap<>();
        //时间条件以及对应的范围进行查询
        List<Document> filterResult =
                impactSimpleDao.filterWXMsgImpactIsFriendResultByCountAndQuery(0, filterQuery);
        //获取c2c对电话号码的关系
        filterResult.forEach(t -> {
            String c2c = t.getString("_id");
            Integer oneCount = t.getInteger("count");
            c2c2Count.put(c2c, oneCount);
        });
        //设备唯一标识以及好友账号与聊天数的关系
        Map<String, Integer> deviceUniqueAndFriendNum2Count = new HashMap<>();
        impactResult.forEach(t -> {
            //好友qq号码
            String friendWXNum = t.get("_id").toString();
            //待碰撞的qq号码集合
            BasicDBList aList = (BasicDBList) t.get("value");
            //待碰撞QQ账号集合
            for (Object a : aList) {
                if (a != null) {
                    String deviceUnique = wxNum2DeviceUnique.get(a);
                    String c2c = Utils.StringUniqueMD5(friendWXNum, a.toString());
                    Integer c2cCount = c2c2Count.get(c2c);
                    c2cCount = c2cCount != null ? c2cCount : 0;
                    String key = deviceUnique + "," + friendWXNum;
                    //代表一个设备下多个qq号在此群发言
                    if (deviceUniqueAndFriendNum2Count.get(key) != null) {
                        Integer olderCount = deviceUniqueAndFriendNum2Count.get(key);
                        deviceUniqueAndFriendNum2Count.put(key, olderCount + c2cCount);
                    } else {
                        deviceUniqueAndFriendNum2Count.put(key, c2cCount);
                    }
                }
            }
        });
        //此时再次进行处理
        //遍历碰撞结果,组装成前端需要的格式
        impactResult.forEach(t -> {
            //保证deviceUniques不重复
            HashSet<String> relationDeviceUnqiues = new HashSet<>();
            //好友wx号码
            String friendWXNum = t.get("_id").toString();
            //待碰撞的qq号码集合
            BasicDBList aList = (BasicDBList) t.get("value");
            //遍历有关系的待碰撞电话号码集合
            aList.forEach(q -> {
                //获取wx号码所在的设备
                String device = wxNum2DeviceUnique.get(q);
                //避免相同设备
                if (!relationDeviceUnqiues.contains(device)) {
                    relationDeviceUnqiues.add(device);
                }
            });
//			logger.debug("friendWXNum:"+friendWXNum);
//			logger.debug("relationDeviceUnqiues:"+relationDeviceUnqiues);
            //说明此QQ号是两个设备的deviceUnique
            if (relationDeviceUnqiues.size() > 1) {
                //存储需要连线的限制条件
                HashSet<String> filterDeviceUniques = new HashSet<>();
                for (Object oneWXUin : aList) {
                    String oneDeviceUnique = wxNum2DeviceUnique.get(oneWXUin);
                    Integer count = deviceUniqueAndFriendNum2Count.get(oneDeviceUnique + "," + friendWXNum);
                    count = count != null ? count : 0;
                    if (count >= userCountLimit) {
                        filterDeviceUniques.add(oneDeviceUnique);
                    }
                }
                //代表有共同好友
                if (filterDeviceUniques.size() > 1) {
                    Map<String, String> numberNode = new HashMap<>();
                    //num连线规则 对应relation
                    numberNode.put("num", friendWXNum);
                    numberNode.put("type", "elseNode");
                    //群号或群昵称根据需求
                    numberNode.put("name", friendWXNum);
                    nodes.add(numberNode);
                    //增加对应的设备节点信息以及对应的relation
                    filterDeviceUniques.forEach(resultDeviceUnique -> {
                        Map<String, Object> relation = new HashMap<>();
                        //关系连线
                        relation.put("startNode", resultDeviceUnique);
                        relation.put("endNode", friendWXNum);
                        relation.put("LinkName", "wx");
                        Integer thisCount = deviceUniqueAndFriendNum2Count.get(
                                resultDeviceUnique + "," + friendWXNum);
                        relation.put("contactCount", thisCount != null ? thisCount : 0);
                        relations.add(relation);
                        //防止一个设备加入多个群时,添加多个节点
                        if (!addNodeDeviceUniques.contains(resultDeviceUnique)) {
                            //设备节点连线
                            Map<String, String> oneNode = new HashMap<>();
                            oneNode.put("num", resultDeviceUnique);
                            oneNode.put("type", "phone");
                            String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(resultDeviceUnique);
                            if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                oneNode.put("personName", stringsDeviceInfo[0]);
                                oneNode.put("personNumber", stringsDeviceInfo[1]);
                            }
                            //根据需求调整显示名
                            oneNode.put("name", unique2deviceName.get(resultDeviceUnique));
                            nodes.add(oneNode);
                            addNodeDeviceUniques.add(resultDeviceUnique);
                        }
                    });
                }
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);
        return result;
    }

    /**
     * 直接关系
     *
     * @param deviceUniques  设备唯一标识
     * @param userId
     * @param place
     * @param startTime
     * @param endTime
     * @param countLimit
     * @param timeSelectType
     * @return
     */
    @Override
    public Map<String, Object> impactByPhoneNumbers(String deviceUniques, Integer userId, String place,
                                                    Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("搜索设备唯一码不能为空");
        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "phone");
        Map<String, Object> result = new HashMap<>(4);
        result.put("relationType", "PHONE");
        //装对应的节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //装对应的关系
        List<Map<String, Object>> relations = new ArrayList<>();
        //得到设备集合并去重复
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        //查询设备对应的手机号码
        List<String> mainPhones = new ArrayList<>();
        //获得设备对应人名和身份证号码  key 设备unique, value 数组0姓名，1身份证，其余的为电话号码
        Map<String, String[]> deviceUnique2PersonNameAndNumber = impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);
        //设备对应的设备名称
        Map<String, String> device2DeviceName = new HashMap<>(devices.length);
        for (String device : devices) {
            String[] device2Phones = deviceUnique2PersonNameAndNumber.get(device);
            if (device2Phones == null || device2Phones.length < 3) {
                // 设备电话号码为空
                continue;
            }
            //前两位不是电话号码，因此需要减少两个容量
            String[] phones = new String[device2Phones.length - 2];
            System.arraycopy(device2Phones, 2, phones, 0, device2Phones.length - 2);
            mainPhones.addAll(Arrays.asList(phones));
            //获取设备对应的设备名称
            List<Document> deviceResult = impactSimpleDao.deviceFindOne(device);
            if (!CollectionUtils.isEmpty(deviceResult)) {
                String deviceName = deviceResult.get(0).getString("devicename");
                if (!device2DeviceName.containsKey(device)) {
                    //设备唯一值对应设备的名称
                    device2DeviceName.put(device, deviceName);
                }
            }
        }
        //总数限制条件
        countLimit = countLimit != null ? countLimit : 0;
        //1 查询所有设备的手机号码集合，用于在通讯录表中确认通话关系。
        //获得设备对应人名和身份证号码  key 设备unique, value 数组0姓名，1身份证，其余的为电话号码
        //存储手机号码对应的设备
        Map<String, String> phone2Device1 = new HashMap<>(16);
        //获取所有的手机号码
        for (String device : devices) {
            //device 代表phone
            String[] device2Phones = deviceUnique2PersonNameAndNumber.get(device);
            if (device2Phones == null || device2Phones.length < 3) {
                // 设备电话号码为空
                continue;
            }
            //从下标2开始，因为返回的数据前两位是用户名和证件号
            for (int i = 2; i < device2Phones.length; i++) {
                phone2Device1.put(device2Phones[i], device);
            }
        }
        //2 单个设备对应的手机号码用map存起来
        //3 通过查询通讯记录表确认节点与关系 适当考虑去除重复
        BasicDBObject filterQuery1 = new BasicDBObject();
        //时间筛选条件
        BasicDBObject timeQuery1 = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);
        if (!timeQuery1.isEmpty()) {
            filterQuery1.append("time", timeQuery1);
        }
        Integer finalCountLimit = countLimit;
        deviceUnique2PersonNameAndNumber.forEach((device, value) -> {
            filterQuery1.append("deviceUnique", device).append("phonenum", new BasicDBObject(QueryOperators.IN, mainPhones));
            //查询数据库
            List<Document> listResult1 = impactSimpleDao.filterPhoneStraightRalationByCountAndQuery(finalCountLimit, filterQuery1);
            if (listResult1 != null) {
                //封装节点信息
                listResult1.forEach(d -> {
                    //获取通话次数
                    Integer oneCount = d.getInteger("count");
                    //设备通话的手机号码
                    String phonenum = d.getString("_id");
                    //查询号码对应的设备
                    String relationDevice = phone2Device1.get(phonenum);
                    //两个节点 一个关系连线，形成一个关系
                    Map<String, String> relationNode = new HashMap<>();
                    if (relationDevice != null && !relationDevice.equals(device)) {
                        //去除给自己打电话的情况才添加节点信息
                        Map<String, String> oneNode = new HashMap<>();
                        oneNode.put("num", device);
                        oneNode.put("name", device2DeviceName.get(device));
                        oneNode.put("type", "phone");

                        String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(device);
                        if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                            oneNode.put("personName", stringsDeviceInfo[0]);
                            //用于前端连线悬停指示
                            oneNode.put("personNumber", stringsDeviceInfo[1]);
                        }
                        if (!nodes.contains(oneNode)) {
                            nodes.add(oneNode);
                        }
                        relationNode.put("num", relationDevice);
                        relationNode.put("name", device2DeviceName.get(relationDevice));
                        relationNode.put("type", "phone");
                        String[] stringsRelationDevice = deviceUnique2PersonNameAndNumber.get(relationDevice);
                        if (stringsRelationDevice != null && stringsRelationDevice.length > 1) {
                            relationNode.put("personName", stringsRelationDevice[0]);
                            relationNode.put("personNumber", stringsRelationDevice[1]);
                        }
                        if (!nodes.contains(relationNode)) {
                            nodes.add(relationNode);
                        }
                        Map<String, Object> relation = new HashMap<>();
                        relation.put("startNode", device);
                        relation.put("endNode", relationDevice);
                        relation.put("LinkName", "phone");
                        relation.put("contactCount", oneCount);
                        if (!relations.contains(relation)) {
                            relations.add(relation);
                        }

                    }

                });
            }
        });
        //返回碰撞结果
        result.put("numbers", nodes);
        result.put("relations", relations);
        result.put("mainPhones", mainPhones);
        return result;
    }

    //新关系碰撞
    @Override
    public Map<String, Object> impactByQQtroop(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        /**
         * 通过设备查到该设备对应qq号
         * 通过qq号去查询共同所在的qq群
         * 获取群信息 昵称 返回数据展示页面
         */
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "qqtroop");
        countLimit = countLimit != null ? countLimit : 0;//总数限制条件
        Integer userCountLimit = countLimit;
        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "QQTROOP");//qq群
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        //		if (userId == null) {
        //			throw new RuntimeException("对应的用户ID为null");
        //		}
        //insertLogService.insertRelationLog(userId, place, device_uniques, "QQtroop");//插入日志记录
        List<Map<String, String>> nodes = new ArrayList<>();//装对应的节点信息
        List<Map<String, Object>> relations = new ArrayList<>();//装对应的关系
        List<String> uniquesList = new ArrayList<>();
        Map<String, String> qqUin2DeviceUnique = new HashMap<>();// 设备号对应的qq号 key-QQ value-设备
        Map<String, String> unique2deviceName = new HashMap<>();//设备对应设备名称
        List<String> mainQQNumbers = new ArrayList<>();//传进去是null 得到的是所有的主控QQ号
        ArrayList<String> qqList = new ArrayList<>();
        HashSet<String> addNodeDeviceUniques = new HashSet<>();
        this.extractQQNumberAndDevice2QQNumberByUniques(uniquesList, devices, qqUin2DeviceUnique, unique2deviceName, mainQQNumbers, qqList);
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);
        //碰撞所有的共同QQ群
        List<DBObject> impactResult = impactSimpleDao.impactQQtroopByMainQQs(mainQQNumbers);
        if (CollectionUtils.isEmpty(impactResult)) {//若共同好友没有
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        BasicDBObject filterQuery = new BasicDBObject();//消息总数以及时间过滤条件
        BasicDBList queryOrObj = new BasicDBList();//对应的多个账号限制条件
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);//时间筛选条件
        if (!timeQuery.isEmpty()) {
            filterQuery.append("msgtime", timeQuery);
        }
        if (!CollectionUtils.isEmpty(impactResult)) {
            impactResult.forEach(t -> {//拼接对应的查询条件
                String qqTroop = getString(t.get("_id"));
                BasicDBList aList = (BasicDBList) t.get("value");//每个群对应的qq号集合
                BasicDBObject a = new BasicDBObject("troopuin", qqTroop)
                        .append("senderuin", new BasicDBObject(QueryOperators.IN, aList));
                queryOrObj.add(a);
            });
        }
        if (queryOrObj.size() > 0) {
            filterQuery.append("$or", queryOrObj);
        }
        //时间条件以及对应的范围进行查询
        List<Document> filterResult = impactSimpleDao.filterImpactQQTroopResultByCountAndQuery(countLimit, filterQuery);
        //存在一个设备对应两个QQ号均在同一个群下聊天的情况
        HashMap<String, Integer> deviceUnqiueAndTroopUin2Count = new HashMap<>();//设备唯一标识以及群号对应聊天信息的总数
        filterResult.forEach(t -> {
            logger.debug("t:" + t);
            Document deviceUniqueAndTroopUin = t.get("_id", Document.class);
            Integer oneCount = t.getInteger("count");
            String key = qqUin2DeviceUnique.get(deviceUniqueAndTroopUin.get("fuin"))
                    + "," + deviceUniqueAndTroopUin.get("uin");
            if (deviceUnqiueAndTroopUin2Count.get(key) != null) {//代表一个设备下多个qq号在此群发言
                Integer olderCount = deviceUnqiueAndTroopUin2Count.get(key);
                deviceUnqiueAndTroopUin2Count.put(key, olderCount + oneCount);
            } else {
                deviceUnqiueAndTroopUin2Count.put(key, oneCount);
            }
        });
        impactResult.forEach(t -> {
            HashSet<String> relationDeviceUnqiues = new HashSet<>();
            String qqTroop = getString(t.get("_id"));
            BasicDBList aList = (BasicDBList) t.get("value");//每个群对应的qq号集合
            aList.forEach(q -> {//遍历当前群对应的qq集合
                String device = qqUin2DeviceUnique.get(q);//获取qq所在的设备
                if (!relationDeviceUnqiues.contains(device)) {//避免相同设备
                    relationDeviceUnqiues.add(device);
                }
            });
            if (relationDeviceUnqiues.size() > 1) {//说明此条信息为两个设备的共同好友
                //此时筛序大于限制消息条数的对应设备,并将其存储,最后再处对应的连线
                HashSet<String> filterDeviceUniques = new HashSet<>();//存储需要连线的限制条件
                for (Object oneQQUin : aList) {
                    String oneDeviceUnique = qqUin2DeviceUnique.get(oneQQUin);
                    Integer count = deviceUnqiueAndTroopUin2Count.get(oneDeviceUnique + "," + qqTroop);
                    count = count != null ? count : 0;
                    if (count >= userCountLimit) {
                        filterDeviceUniques.add(oneDeviceUnique);
                    }
                }
                if (filterDeviceUniques.size() > 1) {//代表有共同好友
                    Map<String, String> troopNode = new HashMap<>();
                    troopNode.put("num", qqTroop);//num连线规则 对应relation
                    troopNode.put("type", "elseNode");
                    troopNode.put("name", qqTroop);//群号或群昵称根据需求
                    nodes.add(troopNode);
                    filterDeviceUniques.forEach(filterDevice -> {
                        //增加对应的设备节点信息以及对应的relation
                        Map<String, Object> relation = new HashMap<>();
                        //关系连线
                        relation.put("startNode", filterDevice);
                        relation.put("endNode", qqTroop);
                        relation.put("LinkName", "qqTroop");
                        Integer thisCount = deviceUnqiueAndTroopUin2Count.get(filterDevice + "," + qqTroop);
                        relation.put("contactCount", thisCount != null ? thisCount : 0);
                        relations.add(relation);
                        Map<String, String> oneNode = new HashMap<>();//设备节点连线
                        if (!addNodeDeviceUniques.contains(filterDevice)) {//防止一个设备加入多个群时,添加多个节点
                            oneNode.put("num", filterDevice);
                            oneNode.put("type", "phone");
                            String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(filterDevice);
                            if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                oneNode.put("personName", stringsDeviceInfo[0]);
                                oneNode.put("personNumber", stringsDeviceInfo[1]);
                            }
                            oneNode.put("name", unique2deviceName.get(filterDevice));//根据需求调整显示名
                            nodes.add(oneNode);
                            addNodeDeviceUniques.add(filterDevice);
                        }
                    });
                }
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);
        return result;
    }

    @Override
    public Map<String, Object> impactByWXtroop(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        /**
         * 通过设备查到该设备对应wx号
         * 通过WX号去查询共同所在的wx群
         * 获取群信息 昵称 返回数据展示页面
         */
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "wxtroop");
        countLimit = countLimit != null ? countLimit : 0;//总数限制条件
        Integer userCountLimit = countLimit;
        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "WXTROOP");
        //插入日志记录
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        List<String> uniquesList = new ArrayList<>();
        Map<String, String> wxNum2DeviceUnique = new HashMap<>();// 设备号对应的微信号 key-WX value-设备
        Map<String, String> unique2deviceName = new HashMap<>();//设备对应设备名称
        List<String> mainWXNumbers = new ArrayList<>();//所有的主控wx号
        ArrayList<String> wxList = new ArrayList<>();
        List<Map<String, String>> nodes = new ArrayList<>();//装对应的节点信息
        List<Map<String, Object>> relations = new ArrayList<>();//装对应的关系
        HashSet<String> addNodeDeviceUniques = new HashSet<>();//添加了节点信息的设备唯一标识
        this.extractWXNumberAndDevice2WXNumberByUniques(
                uniquesList, devices, wxNum2DeviceUnique, unique2deviceName, mainWXNumbers, wxList);
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);//查询设备唯一标识与人员姓名以及身份证号的关系
        List<DBObject> impactResult = impactSimpleDao.impactWXtroopByMainWXs(
                mainWXNumbers);
        if (CollectionUtils.isEmpty(impactResult)) {//若共同好友没有
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        BasicDBObject filterQuery = new BasicDBObject();//消息总数以及时间过滤条件
        BasicDBList queryOrObj = new BasicDBList();//对应的多个账号限制条件
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);//时间筛选条件
        if (!timeQuery.isEmpty()) {
            filterQuery.append("msgtime", timeQuery);
        }
        if (!CollectionUtils.isEmpty(impactResult)) {
            impactResult.forEach(t -> {//拼接对应的查询条件
                String wxTroop = t.get("_id").toString();
                BasicDBList aList = (BasicDBList) t.get("value");//每个群对应的qq号集合
                BasicDBObject a = new BasicDBObject("chatroomname", wxTroop)
                        .append("username", new BasicDBObject(QueryOperators.IN, aList));
                queryOrObj.add(a);
            });
        }
        if (queryOrObj.size() > 0) {
            filterQuery.append("$or", queryOrObj);
        }
        //时间条件以及对应的范围进行查询
        List<Document> filterResult =
                impactSimpleDao.filterImpactWXTroopResultByCountAndQuery(countLimit, filterQuery);
        //存在一个设备对应两个wx号均在同一个群下聊天的情况
        HashMap<String, Integer> deviceUnqiueAndTroopUin2Count = new HashMap<>();//设备唯一标识以及群号对应聊天信息的总数
        filterResult.forEach(t -> {
            //uin是群号 fuin是wx号
            Document deviceUniqueAndTroopUin = t.get("_id", Document.class);
            Integer oneCount = t.getInteger("count");
            String key = wxNum2DeviceUnique.get(deviceUniqueAndTroopUin.get("fuin"))
                    + "," + deviceUniqueAndTroopUin.get("uin");
            if (deviceUnqiueAndTroopUin2Count.get(key) != null) {//代表一个设备下多个qq号在此群发言
                Integer olderCount = deviceUnqiueAndTroopUin2Count.get(key);
                deviceUnqiueAndTroopUin2Count.put(key, olderCount + oneCount);
            } else {
                deviceUnqiueAndTroopUin2Count.put(key, oneCount);
            }

        });
        impactResult.forEach(t -> {
            HashSet<String> relationDeviceUnqiues = new HashSet<>();
            String wxTroop = t.get("_id").toString();
            BasicDBList aList = (BasicDBList) t.get("value");//每个群对应的qq号集合
            aList.forEach(q -> {//遍历当前群对应的qq集合
                String device = wxNum2DeviceUnique.get(q);//获取qq所在的设备
                if (!relationDeviceUnqiues.contains(device)) {//避免相同设备
                    relationDeviceUnqiues.add(device);
                }
            });
            if (relationDeviceUnqiues.size() > 1) {//说明此条信息为两个设备的共同好友
                //此时筛序大于限制消息条数的对应设备,并将其存储,最后再处对应的连线
                HashSet<String> filterDeviceUniques = new HashSet<>();//存储需要连线的限制条件
                for (Object oneQQUin : aList) {
                    String oneDeviceUnique = wxNum2DeviceUnique.get(oneQQUin);
                    Integer count = deviceUnqiueAndTroopUin2Count.get(oneDeviceUnique + "," + wxTroop);
                    count = count != null ? count : 0;
                    if (count >= userCountLimit) {
                        filterDeviceUniques.add(oneDeviceUnique);
                    }
                }
                if (filterDeviceUniques.size() > 1) {//代表有共同好友
                    Map<String, String> troopNode = new HashMap<>();
                    troopNode.put("num", wxTroop);//num连线规则 对应relation
                    troopNode.put("type", "elseNode");
                    troopNode.put("name", wxTroop);//群号或群昵称根据需求
                    nodes.add(troopNode);
                    filterDeviceUniques.forEach(fiterDevice -> {//增加对应的设备节点信息以及对应的relation
                        Map<String, Object> relation = new HashMap<>();//关系连线
                        relation.put("startNode", fiterDevice);
                        relation.put("endNode", wxTroop);
                        relation.put("LinkName", "wxTroop");
                        Integer thisCount = deviceUnqiueAndTroopUin2Count.get(fiterDevice + "," + wxTroop);
                        relation.put("contactCount", thisCount != null ? thisCount : 0);
                        relations.add(relation);
                        Map<String, String> oneNode = new HashMap<>();//设备节点连线
                        if (!addNodeDeviceUniques.contains(fiterDevice)) {//防止一个设备加入多个群时,添加多个节点
                            oneNode.put("num", fiterDevice);
                            oneNode.put("type", "phone");
                            String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(fiterDevice);
                            if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                oneNode.put("personName", stringsDeviceInfo[0]);
                                oneNode.put("personNumber", stringsDeviceInfo[1]);
                            }
                            oneNode.put("name", unique2deviceName.get(fiterDevice));//根据需求调整显示名
                            nodes.add(oneNode);
                            addNodeDeviceUniques.add(fiterDevice);
                        }
                    });
                }
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);
        return result;
    }

    @Override
    public Map<String, Object> impactByPhoneIsNotFriend(String devices, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        //初始化参数及返回类型
        if (StringUtils.isEmpty(devices)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        insertLogService.insertRelationLog(userId, place, devices, "phone");
        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "notFriend");
        String[] deviceUniquesSplit =
                com.hnf.honeycomb.util.Utils.bongDuplicateremove(devices.split(","));
        //所有传入的设备
        List<String> deviceUniques = new ArrayList<>();
        //主控手机号，用于对应自己的设备
        List<String> mainPhones = new ArrayList<>();
        //所有的手机号码 包含自己和通讯录好友的电话
        List<String> allPhones = new ArrayList<>();
        //电话号对应的设备信息  key=phone value=deviceUnique
        Map<String, String> phone2Device = new HashMap<>();
        //设备对应的设备名称
        Map<String, String> device2deviceName = new HashMap<>();
        //添加过的碰撞手机号
        HashSet<String> addPhoneNums = new HashSet<>();
        //添加过的碰撞设备
        HashSet<String> addDeviceUniques = new HashSet<>();
        //添加过的关系
        HashSet<String> addRelations = new HashSet<>();
        //节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //关系对应节点
        List<Map<String, Object>> relations = new ArrayList<>();
        //总数限制条件
        countLimit = countLimit != null ? countLimit : 0;
        Integer userCountLimit = countLimit;
        Map<String, Object> straightResult = impactSimpleService.impactByPhoneNumbers(devices, userId, place, startTime, endTime, countLimit, timeSelectType);
        if (straightResult.get("numbers") != null &&
                straightResult.get("relations") != null) {
            //添加直接关系节点 ,这里目前只需要添加他们的直接关系即可，否则直接和间接关系会重复节点。
            nodes = (List<Map<String, String>>) straightResult.get("numbers");
            relations = (List<Map<String, Object>>) straightResult.get("relations");
        }
        //获取直接短信关系
         result = impactSimpleService.impactByMessageStraight(devices, userId, place, startTime, endTime, countLimit, timeSelectType);

        //直接关系的设备集合，用于节点判断是否重复
        List<String> straightDevices = new ArrayList<>();
        nodes.forEach(d -> {
            String unique = d.get("num");
            straightDevices.add(unique);

        });
        //获取设备唯一标识与人员姓名以及对应身份证号的关系
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(deviceUniquesSplit);
        //查询出所有设备的通讯录号码
        getPersonPhones(deviceUniquesSplit, deviceUniques, mainPhones, allPhones, phone2Device, device2deviceName);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                //时间筛选条件
                timeSelectType, startTime, endTime);
        //查询非讯通录的通话关系
        // 去除所有通讯录好友关系并且在该设备范围下
        List<DBObject> impactResult =
                impactSimpleDao.impactByMainPhonesIsNotFriend2Record(allPhones, deviceUniques, timeQuery);
        //查询非通讯录短信关系
        List<DBObject> impactMessageResult =
                impactSimpleDao.impactByAllPhoneIsNotFriend2Message(allPhones, deviceUniques, timeQuery);
        //将两个对应的关系合并
        impactResult.addAll(impactMessageResult);
        if (CollectionUtils.isEmpty(impactResult)) {
            //若共同好友没有
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        //消息总数以及时间过滤条件
        Integer finalCountLimit = countLimit != null ? countLimit : 0;
        //用来装电话与对应的设备的联系次数
        HashMap<String, Integer> hashMap = new HashMap<>();
        List<Map<String, String>> finalNodes = nodes;
        List<Map<String, Object>> finalRelations = relations;
        impactResult.forEach(t -> {
            String key = null;
            Document parse = Document.parse(t.toString());
            String id = parse.getString("_id");
            Document value1 = parse.get("value", Document.class);
            List<Document> documents = value1.get("rst", List.class);
            for (Document document : documents) {
                String deviceUnique = document.getString("deviceUnique");
                key = id + "--" + deviceUnique;
                if(hashMap.containsKey(key)){
                    hashMap.put(key, hashMap.get(key) + 1);
                }else {
                    hashMap.put(key, 1);
                }
            }
            HashSet<String> phoneDeviceUniques = new HashSet<>();
            String friendPhone = t.get("_id").toString();
            BasicDBObject value = (BasicDBObject) t.get("value");
            String countDouble = value.getString("count");
            Integer count = Double.valueOf(countDouble).intValue();
            BasicDBList aList = (BasicDBList) value.get("rst");
            if (hashMap.get(key) >= finalCountLimit) {
                for (Object a : aList) {
                    if (a != null) {
                        //添加设备到set 中
                        if (!phone2Device.containsValue(phone2Device.get(friendPhone))) {
                            //避免与自己连线，严谨
                            String json = a.toString();
                            Document document = Document.parse(json);
                            String deviceUnique = document.getString("deviceUnique");
                            if (deviceUnique != null) {
                                phoneDeviceUniques.add(deviceUnique);
                            }
                        }
                    }
                }
            }
            //有共同好友关系 封装节点
            if (phoneDeviceUniques.size() > 1) {
                phoneDeviceUniques.forEach(deviceUnique -> {
                    if (!addPhoneNums.contains(friendPhone)) {
                        Map<String, String> phoneNumNode = new HashMap<>();
                        phoneNumNode.put("num", friendPhone);
                        phoneNumNode.put("type", "elseNode");
                        phoneNumNode.put("name", friendPhone);
                        finalNodes.add(phoneNumNode);
                        addPhoneNums.add(friendPhone);
                    }
                    if (!addDeviceUniques.contains(deviceUnique)) {
                        if (!straightDevices.contains(deviceUnique)) {
                            //避免出现设备节点重复
                            Map<String, String> oneNode = new HashMap<>();
                            oneNode.put("num", deviceUnique);
                            oneNode.put("type", "phone");
                            oneNode.put("name", device2deviceName.get(deviceUnique));
                            String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(deviceUnique);
                            if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                oneNode.put("personName", stringsDeviceInfo[0]);
                                oneNode.put("personNumber", stringsDeviceInfo[1]);
                            }
                            finalNodes.add(oneNode);
                            addDeviceUniques.add(deviceUnique);
                        }
                    }
                    //避免重复连线
                    if (!addRelations.contains(deviceUnique.trim() + "," + friendPhone.trim())) {
                        //关系连线
                        Map<String, Object> relation = new HashMap<>();
                        relation.put("startNode", deviceUnique);
                        relation.put("endNode", friendPhone);
                        relation.put("LinkName", "phone");
                        relation.put("contactCount", hashMap.get(friendPhone + "--" + deviceUnique));
//                        relation.put("contactCount", count);
                        finalRelations.add(relation);
                        addRelations.add(deviceUnique.trim() + "," + friendPhone.trim());
                    }
                });
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);

        /**  BasicDBObject filterQuery = new BasicDBObject();
         //对应的多个账号限制条件
         BasicDBList queryOrObj = new BasicDBList();
         BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
         //时间筛选条件
         timeSelectType, startTime, endTime);
         if (!timeQuery.isEmpty()) {
         filterQuery.append("time", timeQuery);
         }
         if (!CollectionUtils.isEmpty(impactResult)) {
         impactResult.forEach(t -> {
         //存储共同好友对应的设备集合
         HashSet<String> phoneDeviceUniques = new HashSet<>();
         //碰撞出来共同的好友手机号码
         String friendPhone = t.get("_id").toString();
         //获取到共同好友电话号码对应的设备集合
         BasicDBObject basicDBObject = (BasicDBObject) t.get("value");
         String count =  basicDBObject.get("count").toString();
         BasicDBList aList = (BasicDBList) basicDBObject.get("rst");
         //待碰撞的电话号码
         for (Object a : aList) {
         if (a != null) {
         phoneDeviceUniques.add(a.toString());
         }
         }
         BasicDBObject a = new BasicDBObject("phonenum", friendPhone)
         .append("deviceUnique", new BasicDBObject(
         QueryOperators.IN, phoneDeviceUniques));
         queryOrObj.add(a);
         });
         }
         if (queryOrObj.size() > 0) {
         filterQuery.append("$or", queryOrObj);
         }
         logger.debug("filterQuery:" + filterQuery);
         //可提取为公共方法
         //时间条件以及对应的范围进行查询
         List<Document> filterResult =
         impactSimpleDao.filterRecordImpactIsFriendResultByCountAndQuery(0, filterQuery);
         List<Document> messageFilterResult =
         impactSimpleDao.filterMessageImpactIsFriendResultByCountAndQuery(0, filterQuery);
         Map<String, Integer> deviceUnqiueAndPhone2RecordCount = new HashMap<>();
         Map<String, Integer> deviceUnqiueAndPhone2MessageCount = new HashMap<>();
         filterResult.forEach(t -> {
         //获取设备唯一标识及电话号码对通话记录的条数
         Document deviceUniqueAndPhoneNum = t.get("_id", Document.class);
         Integer oneCount = t.getInteger("count");
         deviceUnqiueAndPhone2RecordCount.put(deviceUniqueAndPhoneNum.get("fuin")
         + "," + deviceUniqueAndPhoneNum.get("uin"),
         oneCount);
         });
         messageFilterResult.forEach(t -> {
         //获取设备唯一标识及电话号码对短消息的条数
         Document deviceUniqueAndPhoneNum = t.get("_id", Document.class);
         Integer oneCount = t.getInteger("count");
         deviceUnqiueAndPhone2MessageCount.put(deviceUniqueAndPhoneNum.get("fuin")
         + "," + deviceUniqueAndPhoneNum.get("uin"),
         oneCount);
         });
         //遍历碰撞共同好友集合,组装成对应前端需要的数据格式
         impactResult.forEach(t -> {
         //去除重复打电话对应相同设备信息，影响判断结果及节点信息 |避免重复new 对象
         HashSet<String> set = new HashSet<>();
         String phoneNumTogether = t.get("_id").toString();
         //避免与自己连线,严谨
         if (!phone2Device.containsValue(phone2Device.get(phoneNumTogether))) {
         //获取号码对应的设备
         BasicDBList devices = (BasicDBList) t.get("value");
         devices.forEach(d -> {
         //logger.debug("d:"+d.getClass().getName());
         if (d instanceof String) {
         set.add((String) d);
         }
         });
         if (set.size() > 1) {
         //代表是两个共同好友
         HashSet<String> filterDeviceUniques = new HashSet<>();
         set.iterator().forEachRemaining(oneDeviceUnique -> {
         //用于去除不符合消息条数的deviceUniques
         //String oneDeviceUnique = phone2Device.get(oneImpactPhoneNum);
         Integer recordCount = deviceUnqiueAndPhone2RecordCount.get(oneDeviceUnique + "," + phoneNumTogether);
         recordCount = recordCount != null ? recordCount : 0;
         Integer messageCount = deviceUnqiueAndPhone2MessageCount.get(oneDeviceUnique + "," + phoneNumTogether);
         messageCount = messageCount != null ? messageCount : 0;
         if ((recordCount + messageCount) >= userCountLimit) {
         filterDeviceUniques.add(oneDeviceUnique);
         }
         });
         //此时代表符合条件搜索条件的结果
         if (filterDeviceUniques.size() > 1) {
         filterDeviceUniques.forEach(deviceUnique -> {
         //遍历设备唯一标识,组装数据
         if (!addPhoneNums.contains(phoneNumTogether)) {
         Map<String, String> phoneNumNode = new HashMap<>();
         phoneNumNode.put("num", phoneNumTogether);
         phoneNumNode.put("type", "elseNode");
         phoneNumNode.put("name", phoneNumTogether);
         nodes.add(phoneNumNode);
         addPhoneNums.add(phoneNumTogether);
         }
         if (!addDeviceUniques.contains(deviceUnique)) {
         Map<String, String> oneNode = new HashMap<>();
         oneNode.put("num", deviceUnique);
         oneNode.put("type", "phone");
         oneNode.put("name", device2deviceName.get(deviceUnique));
         oneNode.put("personName", deviceUnique2PersonNameAndNumber.get(deviceUnique)[0]);
         oneNode.put("personNumber", deviceUnique2PersonNameAndNumber.get(deviceUnique)[1]);
         nodes.add(oneNode);
         addDeviceUniques.add(deviceUnique);
         }
         //							logger.debug("addRelations:"+addRelations);
         //避免重复连线
         if (!addRelations.contains(deviceUnique.trim() + "," + phoneNumTogether.trim())) {
         //关系连线
         Map<String, Object> relation = new HashMap<>();
         relation.put("startNode", deviceUnique);
         relation.put("endNode", phoneNumTogether);
         relation.put("LinkName", "phone");
         Integer recordCount =
         deviceUnqiueAndPhone2RecordCount.get(deviceUnique + "," + phoneNumTogether);
         recordCount = recordCount != null ? recordCount : 0;
         Integer messageCount =
         deviceUnqiueAndPhone2MessageCount.get(deviceUnique + "," + phoneNumTogether);
         messageCount = messageCount != null ? messageCount : 0;
         relation.put("contactCount", (recordCount + messageCount));
         relations.add(relation);
         addRelations.add(deviceUnique.trim() + "," + phoneNumTogether.trim());
         }
         });

         }
         }
         }
         });
         result.put("numbers", nodes);
         result.put("relations", relations);*/
        return result;
    }

    //通过电话碰撞是通讯录好友的关系
    @Override
    public Map<String, Object> impactByPhoneAndIsFriend(String deviceUniques, Integer userId, String searchNum, String time, String place
            , Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {
        //初始化参数及返回类型
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备查询条件为空");
        }
        insertLogService.insertRelationLog(userId, place, deviceUniques, "phone");
        //直接关系
        Map<String, Object> straightResult = impactByPhoneNumbers(deviceUniques, userId, place, startTime, endTime, countLimit, timeSelectType);
        List<String> mainPhones1 = (List<String>) straightResult.get("mainPhones");

        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "PHONE");
        //电话对应的设备 k phone value device
        Map<String, String> phone2Device = new HashMap<>();
        String[] devices = com.hnf.honeycomb.util.Utils.bongDuplicateremove(deviceUniques.split(","));
        List<String> mainPhones = new ArrayList<>();
        // 设备对应的设备名称
        Map<String, String> device2DeviceName = new HashMap<>();
        HashSet<String> addNodeDeviceUniques = new HashSet<>();
        //装对应的节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //装对应的关系
        List<Map<String, Object>> relations = new ArrayList<>();
        if (straightResult.get("numbers") != null &&
                straightResult.get("relations") != null) {
            //添加直接关系节点 ,这里目前只需要添加他们的直接关系即可，否则直接和间接关系会重复节点。
            nodes = (List<Map<String, String>>) straightResult.get("numbers");
            relations = (List<Map<String, Object>>) straightResult.get("relations");
        }
        //直接关系的设备集合，用于节点判断是否重复
        List<String> straightDevices = new ArrayList<>();
        nodes.forEach(d -> {
            String unique = d.get("num");
            straightDevices.add(unique);

        });
        //获取设备唯一标识与人员姓名以及对应身份证号的关系
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(devices);
        //总数限制条件
        countLimit = countLimit != null ? countLimit : 0;
        Integer userCountLimit = countLimit;
        for (String u : devices) {
            //通过设备唯一标识查询 此设备的机主号码  和  通讯录号码
            List<Document> phones = impactSimpleDao.findPhoneBydevice(u);
            //			//logger.debug("phones"+phones);
            if (!CollectionUtils.isEmpty(phones)) {
                phones.forEach(p -> {
                    if (p != null && !p.isEmpty()) {
                        //将手机号  和 设备唯一值 装入HashMap
                        List<String> phone = (List<String>) p.get("phone");
                        //phone2Device.put(p.getString("phone"), u);//添加对应关系
                        if (phone != null) {
                            phone.forEach(t -> {
                                //添加号码对应设备
                                phone2Device.put(t, u);
                                if (!mainPhones.contains(t)) {//&&!mainPhones1.contains(t)
                                    //如果不重复的电话则添加
                                    mainPhones.add(t);
                                }
                            });
                        }
                    }
                });

            }
            /**
             * 获取设备信息
             */
            //查询设备信息
            List<Document> deviceResult = impactSimpleDao.deviceFindOne(u);
            if (!CollectionUtils.isEmpty(deviceResult)) {
                String deviceName = deviceResult.get(0).getString("devicename");
                if (!device2DeviceName.containsKey(u)) {
                    //设备唯一值对应设备的名称
                    device2DeviceName.put(u, deviceName);
                }
            }
        }
        //实际返回机主号码的好友手机号集合。 // 共同好友 A-B  C-B AC共同认识B
        List<DBObject> impactResult = impactSimpleDao.impactByMainPhonesMapReduce(mainPhones);
        if (CollectionUtils.isEmpty(impactResult)) {
            //若没有共同好友
            result.put("numbers", nodes);
            result.put("relations", relations);
            return result;
        }
        //消息总数以及时间过滤条件
        BasicDBObject filterQuery = new BasicDBObject();
        //对应的多个账号限制条件// 好友
        BasicDBList queryOrObj = new BasicDBList();
        //时间筛选条件
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            filterQuery.append("time", timeQuery);
        }
        if (!CollectionUtils.isEmpty(impactResult)) {
            impactResult.forEach(t -> {
                //拼接对应的查询条件
                HashSet<String> phoneDeviceUniques = new HashSet<>();
                String friendPhone = t.get("_id").toString();
                //待碰撞的电话号码（共同的好友 对应的手机号）
                BasicDBList aList = (BasicDBList) t.get("value");
                for (Object a : aList) {
                    if (a != null) {
                        phoneDeviceUniques.add(phone2Device.get(a.toString()));
                    }
                }
                BasicDBObject a = new BasicDBObject("phonenum", friendPhone)
                        .append("deviceUnique", new BasicDBObject(
                                QueryOperators.IN, phoneDeviceUniques));
                queryOrObj.add(a);
            });
        }
        if (queryOrObj.size() > 0) {
            filterQuery.append("$or", queryOrObj);
        }
        logger.warn("query check---->{}" + filterQuery);
        //时间条件以及对应的范围进行查询
        List<Document> filterResult =
                //通话关系碰撞结果
                impactSimpleDao.filterRecordImpactIsFriendResultByCountAndQuery(0, filterQuery);
        List<Document> messageFilterResult =
                impactSimpleDao.filterMessageImpactIsFriendResultByCountAndQuery(0, filterQuery);
        Map<String, Integer> deviceUnqiueAndPhone2RecordCount = new HashMap<>();
        Map<String, Integer> deviceUnqiueAndPhone2MessageCount = new HashMap<>();
        //获取设备唯一标识及电话号码对通话记录的条数
        filterResult.forEach(t -> {
            Document deviceUniqueAndPhoneNum = t.get("_id", Document.class);
            Integer oneCount = t.getInteger("count");
            // fuin 为deviceUnique, uin 是手机号码。生成设备与手机号的通话次数映射关系。
            deviceUnqiueAndPhone2RecordCount.put(deviceUniqueAndPhoneNum.get("fuin")
                            + "," + deviceUniqueAndPhoneNum.get("uin"),
                    oneCount);
        });
        //获取设备唯一标识及电话号码对短消息的条数
        messageFilterResult.forEach(t -> {
            Document deviceUniqueAndPhoneNum = t.get("_id", Document.class);
            Integer oneCount = t.getInteger("count");
            //生成设备与号码的短息次数映射关系
            deviceUnqiueAndPhone2MessageCount.put(deviceUniqueAndPhoneNum.get("fuin")
                            + "," + deviceUniqueAndPhoneNum.get("uin"),
                    oneCount);
        });

        List<Map<String, String>> finalNodes = nodes;
        List<Map<String, Object>> finalRelations = relations;
        //遍历碰撞结果,及将对应的数据按前端要求的格式返回
        impactResult.forEach(t -> {
            HashSet<String> relationDeviceUnqiues = new HashSet<>();
            //共同好友的电话号码
            String friendPhoneNum = t.get("_id").toString();
            //待碰撞的电话号码集合
            BasicDBList aList = (BasicDBList) t.get("value");
            //遍历有关系的待碰撞电话号码集合
            aList.forEach(q -> {
                //获取电话所在的设备
                String device = phone2Device.get(q);
                if (!relationDeviceUnqiues.contains(device)) {
                    //避免相同设备
                    relationDeviceUnqiues.add(device);
                }
            });
            //说明此电话号码为两个设备的共同好友
            if (relationDeviceUnqiues.size() > 1) {
                //存储需要连线的限制条件
                HashSet<String> filterDeviceUniques = new HashSet<>();
                for (Object oneImpactPhoneNum : aList) {
                    String oneDeviceUnique = phone2Device.get(oneImpactPhoneNum);
                    Integer recordCount = deviceUnqiueAndPhone2RecordCount.get(oneDeviceUnique + "," + friendPhoneNum);
                    recordCount = recordCount != null ? recordCount : 0;
                    Integer messageCount = deviceUnqiueAndPhone2MessageCount.get(oneDeviceUnique + "," + friendPhoneNum);
                    messageCount = messageCount != null ? messageCount : 0;
                    if ((recordCount + messageCount) >= userCountLimit) {
                        //这里主要用于限制生成节点，通话次数和短消息相加 大于等于限制次数才添加到最终的设备节点中。
                        filterDeviceUniques.add(oneDeviceUnique);
                    }
                }
                //代表有共同好友
                if (filterDeviceUniques.size() > 1) {
                    //对应的电话节点
                    Map<String, String> phoneNode = new HashMap<>();
                    //num连线规则 对应relation
                    if (!mainPhones1.contains(friendPhoneNum)) {
                        phoneNode.put("num", friendPhoneNum);
                        phoneNode.put("type", "elseNode");
                        phoneNode.put("name", friendPhoneNum);
                        finalNodes.add(phoneNode);
                    }
                    //增加对应的设备节点信息以及对应的relation
                    filterDeviceUniques.forEach(filterDevice -> {
                        //关系连线
                        Map<String, Object> relation = new HashMap<>();
                        relation.put("startNode", filterDevice);
                        relation.put("endNode", friendPhoneNum);
                        relation.put("LinkName", "phone");
                        Integer recordCount = deviceUnqiueAndPhone2RecordCount.get(filterDevice + "," + friendPhoneNum);
                        recordCount = recordCount != null ? recordCount : 0;
                        Integer messageCount = deviceUnqiueAndPhone2MessageCount.get(filterDevice + "," + friendPhoneNum);
                        messageCount = messageCount != null ? messageCount : 0;
                        //这里的通联次数是电话和短信相加的
                        relation.put("contactCount", (recordCount + messageCount));
                        finalRelations.add(relation);
                        //设备节点连线
                        Map<String, String> oneNode = new HashMap<>();
                        //防止一个设备加入多个群时,添加多个节点
                        if (!addNodeDeviceUniques.contains(filterDevice)) {
                            //如果直接关系和间接关系的节点不相同才添加节点
                            if (!straightDevices.contains(filterDevice)) {
                                oneNode.put("num", filterDevice);
                                oneNode.put("type", "phone");
                                String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(filterDevice);
                                if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                                    oneNode.put("personName", stringsDeviceInfo[0]);
                                    oneNode.put("personNumber", stringsDeviceInfo[1]);
                                }
                                //根据需求调整显示名
                                oneNode.put("name", device2DeviceName.get(filterDevice));
                                finalNodes.add(oneNode);
                                addNodeDeviceUniques.add(filterDevice);
                            }
                        }
                    });
                }
            }
        });
        result.put("numbers", nodes);
        result.put("relations", relations);
        return result;
    }

    /**
     * 通过uniques 获取主控QQ和 QQ对应的设备信息
     *
     * @param uniquesList       设备唯一码集合
     * @param deviceUniques
     * @param qqNum2Device      设备对应的QQ
     * @param unique2deviceName 设备对应的昵称
     * @param mainQQNumbers     主控QQ
     * @param qqList
     */
    private void extractQQNumberAndDevice2QQNumberByUniques(List<String> uniquesList, String[] deviceUniques,
                                                            Map<String, String> qqNum2Device,
                                                            Map<String, String> unique2deviceName,
                                                            List<String> mainQQNumbers, ArrayList<String> qqList) {
        List<Document> qqUserList;
        if (deviceUniques != null && deviceUniques.length > 1) {
            for (String u : deviceUniques) {
                uniquesList.add(u);
                //查询(单个)每一个设备对应的qq号 设计可一次查询多个
                qqUserList = impactSimpleDao.extractNumbersByType("QQ", uniquesList);
                if (qqUserList != null && qqUserList.size() > 0) {
                    //d为每个设备对应的的对象，包含qq信息
                    qqUserList.forEach((d) -> {
                        //一个设备数据库只对应一个qq //get 返回值需根据数据库类型
                        String qqNum = d.getString("uin");
                        qqList.add(qqNum);
                        //避免存在相同的qq
                        if (!mainQQNumbers.contains(qqNum)) {
                            mainQQNumbers.add(qqNum);
                        }
                        //设备与qq对应关系
                        qqNum2Device.put(qqNum, u);
                        //   }

                    });
                }
                //查询设备信息
                List<Document> deviceResult = impactSimpleDao.deviceFindOne(u);
                String deviceName = deviceResult.get(0).getString("devicename");
                //避免相同设备
                if (!unique2deviceName.containsKey(u)) {
                    unique2deviceName.put(u, deviceName);
                }
                //清空list 目的是每次只查一个设备//设计为可查多个
                uniquesList.clear();
                //清空当前设备对应的qq号
                qqList.clear();
                deviceResult.clear();
            }
        }
    }


    private void extractWXNumberAndDevice2WXNumberByUniques(List<String> uniquesList, String[] deviceUniques, Map<String, String> unique2WXnum, Map<String, String> unique2deviceName, List<String> mainWXNumbers, ArrayList<String> wxList) {
        List<Document> wxUserList;
        if (deviceUniques != null && deviceUniques.length > 1) {
            for (String u : deviceUniques) {
                uniquesList.add(u);
                //查询(单个)每一个设备对应的wx号 设计可一次查询多个
                wxUserList = impactSimpleDao.extractNumbersByType("WX", uniquesList);
                if (wxUserList != null && wxUserList.size() > 0) {
                    //d为每个设备对应的的对象，包含qq信息
                    wxUserList.forEach((d) -> {
                        //一个设备数据库只对应一个qq //get 返回值需根据数据库类型
                        String wxNum = d.getString("username");
                        wxList.add(wxNum);
                        //避免存在相同的wx
                        if (!mainWXNumbers.contains(wxNum)) {
                            mainWXNumbers.add(wxNum);
                        }
                        //设备与wx对应关系
                        unique2WXnum.put(wxNum, u);
                    });
                }
                //查询设备信息
                List<Document> deviceResult = impactSimpleDao.deviceFindOne(u);
                String deviceName = deviceResult.get(0).getString("devicename");
                //避免相同设备
                if (!unique2deviceName.containsKey(u)) {
                    unique2deviceName.put(u, deviceName);
                }
                //清空list 目的是每次只查一个设备//设计为可查多个
                uniquesList.clear();
                //清空当前设备对应的qq号
                wxList.clear();
                deviceResult.clear();
            }
        }
    }

    @Override
    public Long insertImpactHistory(Integer userId, String type, String deviceUnique, String time, String project,
                                    String explain) {
        // TODO Auto-generated method stub

        if (userId == null) {
            throw new RuntimeException("对应的用户id为空");
        }
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("碰撞设备为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("碰撞类型为空");
        }
        Document query = new Document();
        query.append("userId", userId);
        query.append("searchNum", deviceUnique.trim());
        query.append("type", type);
        List<Document> history = impactServerDao.findHistory(query);
        if (!CollectionUtils.isEmpty(history)) {
            throw new RuntimeException("已存在");
        }
        Date date = new Date();
        if (!StringUtils.isEmpty(time)) {
            date = TimeUtils.parseStrToDate(time);
        }
        Document insertDoc = new Document();
        insertDoc.append("userId", userId);
        insertDoc.append("type", type.trim());
        insertDoc.append("searchNum", deviceUnique.trim());
        insertDoc.append("time", date);
        insertDoc.append("project", project);
        insertDoc.append("explain", explain);
        insertDoc.append("unique", MD5Util.MD5(userId + deviceUnique));
        return impactServerDao.insertHistory(insertDoc);
    }

    @Override
    public List<Document> findImpactHistoryByUserId(Integer userId, String project, String deviceName, Integer page,
                                                    Integer pageSize) {
        // TODO Auto-generated method stub
        if (userId == null) {
            throw new RuntimeException("查询关系碰撞的用户账号为空");
        }
        Document query = new Document("userId", userId);
        if (!StringUtils.isEmpty(project)) {
            Pattern pattern = Pattern.compile("^.*" + project.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("project", pattern);
        }
        List<Pattern> list2 = new ArrayList<>();
        if (!StringUtils.isEmpty(deviceName)) {
            List<Document> list = impactSimpleDao.findDeviceByName(deviceName);
            list.forEach(t -> {
                String deviceUnique = t.getString("device_unique");
                Pattern pattern = Pattern.compile("^.*" + deviceUnique.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                list2.add(pattern);
            });
            //			//logger.debug("list2:"+list2);
        }
        List<Document> impactHistories = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list2)) {
            list2.forEach(t1 -> {
                query.append("searchNum", t1);
                //				//logger.debug("query:"+query);
                List<Document> impact = impactServerDao.findHistory(query);
                impact.forEach(t2 -> {
                    impactHistories.add(t2);
                });
            });
        } else {
            List<Document> impact = impactServerDao.findHistory(query);
            impact.forEach(t3 -> {
                impactHistories.add(t3);
            });
        }
        if (impactHistories != null) {
            impactHistories.forEach(t -> {
                List<Document> devices = new ArrayList<>();
                String searchNums = t.getString("searchNum");
                if (!StringUtils.isEmpty(searchNums)) {
                    String[] searchNumbers = StringUtils.split(searchNums, ",");
                    for (int i = 0; i < searchNumbers.length; i++) {
                        devices.add(impactSimpleDao.deviceFindOne(searchNumbers[i]).get(0));
                    }
                }
                t.append("searchTatil", devices);
            });
        }
        return impactHistories;
    }

    @Override
    public List<Document> findImpactProjectName(Integer userId) {
        List<Document> findHistory = impactServerDao.findHistory(new Document("userId", userId));
        findHistory.forEach(f -> {
            f.remove("_id");
            f.remove("type");
            f.remove("searchNum");
            f.remove("time");
            f.remove("explain");
        });
        return findHistory;
    }

    @Override
    public Document findImpactHistoryByUnique(String impactUnique) {
        // TODO Auto-generated method stub
        if (impactUnique == null) {
            throw new RuntimeException("查询关系碰撞的唯一值为空");
        }
        Document resultDoc = new Document();
        Document query = new Document("unique", impactUnique);
        List<Document> impact = impactServerDao.findHistory(query);
        //唯一值查询 只能查出一条数据  但这个Dao查询方法返回结果是List类型的 所以在这里循环一下
        String searchNums = "";
        for (Document document : impact) {
            resultDoc.put("impact", impact);
            searchNums = document.get("searchNum").toString();
        }
        List<Document> devices = new ArrayList<Document>();
        if (!StringUtils.isEmpty(searchNums)) {
            String[] searchNumbers = StringUtils.split(searchNums, ",");
            for (int i = 0; i < searchNumbers.length; i++) {
                devices.add(impactSimpleDao.deviceFindOne(searchNumbers[i]).get(0));
            }
        }
        resultDoc.put("devices", devices);
        return resultDoc;
    }

    @Override
    public Integer updateImpactHistory(String unique, String explain, String project, String deviceUnique) {
        // TODO Auto-generated method stub

        if (StringUtils.isEmpty(unique)) {
            throw new RuntimeException("对应的修改条件where条件为空");
        }
        Document newDoc = new Document();
        if (project != null) {
            newDoc.append("project", project.trim());
        }
        if (explain != null) {
            newDoc.append("explain", explain);
        }
        if (!StringUtils.isEmpty(deviceUnique)) {
            newDoc.append("searchNum", deviceUnique);
        }
        newDoc.append("time", new Date());
        Document query = new Document("unique", unique);
        return impactServerDao.updateHistory(query, newDoc);

    }

    @Override
    public Integer deleteImpactHistory(String unique) {
        // TODO Auto-generated method stub

        if (StringUtils.isEmpty(unique)) {
            throw new RuntimeException("删除的unique为空");
        }
        Document query = new Document("unique", unique);
        return impactServerDao.deleteHistory(query);
    }

    /**通过对应的节点信息查询对应的节点类型*/
    @Override
    public Object findNodeMsgByNodeDetail(Integer nodeType, String deviceUniques, String searchNum,
                                          Integer searchType, Long startTime, Long endTime, Integer timeSelectType) {
        if (searchType == null) {
            throw new RuntimeException("对应的查询类型为null");
        }
        if (StringUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备唯一标识为null");
        }
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("共同好友节点账号为NULL");
        }

        List<Map<String, Object>> result;
        switch (nodeType) {
            //当节点类型为设备时
            case 1:
                switch (searchType) {
                    //代表通讯录关系：短消息，通讯录
                    case 1:
                        result = this.getContactPhoneMsgAndRecordByDeviceUnique(deviceUniques, searchNum);
                        break;
                    //代表非通讯录关系
                    case 2:
                        result = this.getNoContactPhoneMsgAndRecordByDeviceUnique(deviceUniques, searchNum);
                        break;
                    //代表QQ好友
                    case 3:
                        List<String> qqUins = new ArrayList<>();
                        List<Document> qqUsers = impactSimpleDao.qquser(deviceUniques);
                        if (!CollectionUtils.isEmpty(qqUsers)) {
                            qqUsers.forEach(t -> {
                                String qqUin = t.getString("uin");
                                if (!StringUtils.isEmpty(qqUin)) {
                                    qqUins.add(qqUin);
                                }
                            });
                        }
                        //查询传入设备相关联的QQ账号与对应账号的聊天信息集合
                        result = this.getQQUserFriendMsgDetailByQQUinsAndRelationQQUins(
                            qqUins, searchNum
                        );
                        break;
                    case 4://代表QQ群
                        List<String> deviceQQs = new ArrayList<>();
                        List<Document> devices = impactSimpleDao.qquser(deviceUniques);
                        if (!CollectionUtils.isEmpty(devices)) {
                            devices.forEach(t -> {
                                String qqUin = t.getString("uin");
                                if (!StringUtils.isEmpty(qqUin)) {
                                    deviceQQs.add(qqUin);
                                }
                            });
                        }
                        result = this.getQQUserTroopMsgDetailByQQUinsAndRelationQQTroopUins(deviceQQs, searchNum);
                        break;
                    case 5://代表WX好友
                        List<Document> wxUser = impactSimpleDao.wxuser(deviceUniques);//查询WX号
                        List<String> deviceWXUsers = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(wxUser)) {
                            wxUser.forEach(t -> {
                                String wxUin = t.getString("username");
                                if (!StringUtils.isEmpty(wxUin)) {
                                    deviceWXUsers.add(wxUin);
                                }
                            });
                        }
                        //				result = deviceWXUsers;
                        result = this.getWXUserFriendMsgDetailByWXUinsAndRelationWXUins(deviceWXUsers, searchNum);
                        break;
                    case 6://代表WX群
                        List<Document> wxUser1 = impactSimpleDao.wxuser(deviceUniques);//查询WX号
                        List<String> deviceWXUsers1 = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(wxUser1)) {
                            wxUser1.forEach(t -> {
                                String wxUin = t.getString("username");
                                if (!StringUtils.isEmpty(wxUin)) {
                                    deviceWXUsers1.add(wxUin);
                                }
                            });
                        }
                        result =
                                this.getWXUserTroopMsgDetailByWXUinsAndRelationWXTroopUins(
                                        deviceWXUsers1, searchNum);
                        break;
                    default:
                        throw new RuntimeException("对应的关系类型不合规");
                }
                break;
            case 2://当节点类型为共同好友时
                if (StringUtils.isEmpty(searchNum)) {
                    throw new RuntimeException("对应共同好友的几点账号为null");
                }
                switch (searchType) {
                    case 1://代表通讯录关系：短消息，通讯录
                        result = this.getContactPhoneMsgAndRecordByDeviceUniquesAndPhone(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    case 2://代表非通讯录关系
                        result = this.getContactPhoneMsgAndRecordByDeviceUniquesAndPhone(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    case 3://代表QQ好友
                        result = this.getQQFrinedMsgByDeviceUniquesAndQQNumber(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    case 4://代表QQ群
                        result = this.getQQGroupMsgByDeviceUniquesAndQQNumber(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    case 5://代表WX好友
                        result = this.getWXFriendMsgByDeviceUniquesAndWXNumber(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    case 6://代表WX群
                        result = this.getWXGroupMsgByDeviceUniquesAndQQNumber(
                                StringUtils.split(deviceUniques, ","), searchNum);
                        break;
                    default:
                        throw new RuntimeException("对应的关系类型不合规");
                }
                break;
            default:
                throw new RuntimeException("节点类型不合规");
        }
        return result;
    }

    @Override
    public Map<String, Object> impactByMessageStraight(String devices, Integer userId, String place, Long startTime, Long endTime, Integer countLimit, Integer timeSelectType) {

        //节点信息
        List<Map<String, String>> nodes = new ArrayList<>();
        //关系对应节点
        List<Map<String, Object>> relations = new ArrayList<>();
        String[] deviceUniquesSplit =
                com.hnf.honeycomb.util.Utils.bongDuplicateremove(devices.split(","));
        //获取设备唯一标识与人员姓名以及对应身份证号的关系
        Map<String, String[]> deviceUnique2PersonNameAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(deviceUniquesSplit);
        List<String> deviceUniques = new ArrayList<>();
        List<String> mainPhones = new ArrayList<>();
        List<String> allPhones = new ArrayList<>();
        Map<String, String> phone2Device = new HashMap<>();
        Map<String, String> device2deviceName = new HashMap<>();
        //查询出所有设备的通讯录号码
        getPersonPhones(deviceUniquesSplit, deviceUniques, mainPhones, allPhones, phone2Device, device2deviceName);
        //时间筛选条件
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                timeSelectType, startTime, endTime);
        //获取碰撞结果
        List<DBObject> impactResult = impactSimpleDao.impactByMessageStraight(deviceUniques, allPhones, countLimit, timeQuery);
        if (impactResult != null) {
            //封装节点信息 返回
            impactResult.forEach(t -> {
                HashSet<String> phoneDeviceUniques = new HashSet<String>();
                //获取共同的好友手机号码
                String friendPhone = t.get("_id").toString();
                //获取认识的共同的设备
                String device = phone2Device.get(friendPhone);
                BasicDBObject value = (BasicDBObject) t.get("value");
                BasicDBList list = (BasicDBList) value.get("rst");
                Integer oneCount = Double.valueOf(value.get("count").toString()).intValue();
                if (list != null) {
                    //统计单个手机号对应的设备集合
                    list.forEach(d -> {
                        //添加设备到set 中
                        if (!phone2Device.containsValue(phone2Device.get(friendPhone))) {
                            //避免与自己连线，严谨
                            String json = d.toString();
                            Document document = Document.parse(json);
                            String deviceUnique = document.getString("device");
                            if (deviceUnique != null) {
                                phoneDeviceUniques.add(deviceUnique);
                            }
                        }
                    });
                    //如果是属于输入碰撞设备的号码才能添加节点，设备对应设备的关系
                    if (device != null && phoneDeviceUniques.size() > 0) {
                        //去除给自己打电话的情况才添加节点信息
                        Map<String, String> oneNode = new HashMap<>();
                        oneNode.put("num", device);
                        oneNode.put("name", device2deviceName.get(device));
                        oneNode.put("type", "phone");
                        String[] stringsDeviceInfo = deviceUnique2PersonNameAndNumber.get(device);
                        if (stringsDeviceInfo != null && stringsDeviceInfo.length > 1) {
                            oneNode.put("personName", stringsDeviceInfo[0]);
                            //用于前端连线悬停指示
                            oneNode.put("personNumber", stringsDeviceInfo[1]);
                        }
                        if (!nodes.contains(oneNode)) {
                            nodes.add(oneNode);
                        }
                        //循环关系设备遍历
                        phoneDeviceUniques.forEach(dv -> {
                            Map<String, String> relationNode = new HashMap<>();
                            relationNode.put("num", dv);
                            relationNode.put("name", device2deviceName.get(dv));
                            relationNode.put("type", "phone");
                            String[] stringsDevice = deviceUnique2PersonNameAndNumber.get(dv);
                            if (stringsDevice != null && stringsDevice.length > 1) {
                                relationNode.put("personName", stringsDevice[0]);
                                relationNode.put("personNumber", stringsDevice[1]);
                            }
                            if (!nodes.contains(relationNode)) {
                                nodes.add(relationNode);
                            }
                            Map<String, Object> relation = new HashMap<>();
                            relation.put("startNode", device);
                            relation.put("endNode", dv);
                            relation.put("LinkName", "phone");
                            relation.put("contactCount", oneCount);
                            if (!relations.contains(relation)) {
                                relations.add(relation);
                            }
                        });
                    }

                }
            });
        }
        Map<String, Object> result = new HashMap<>();
        result.put("relationType", "PHONE");
        result.put("relations", relations);
        result.put("numbers", nodes);
        return result;
    }

    private void getPersonPhones(String[] devices, List<String> deviceUniques, List<String> mainPhones, List<String> allPhones, Map<String, String> phone2Device, Map<String, String> device2deviceName) {
        if (devices.length > 1) {
            for (String u : devices) {
                List<Document> phones = impactSimpleDao.findContactPhoneByDeviceUnqiue(u);
                deviceUniques.add(u);
                phones.forEach(p -> {
                    String phone = p.getString("phonenumSelf");
                    if (!mainPhones.contains(phone)) {
                        mainPhones.add(phone);
                        allPhones.add(phone);
                        //添加电话对应设备
                        phone2Device.put(phone, u);
                    }
                    String friendPhone = p.getString("phonenum");
                    //无需做判断，主要用于排除该集合的号码后的非通讯录通联关系
                    allPhones.add(friendPhone);
                });
                //查询设备信息
                List<Document> deviceResult = impactSimpleDao.deviceFindOne(u);
                if (!CollectionUtils.isEmpty(deviceResult)) {
                    //只有一条信息，所以get(0)
                    String deviceName = deviceResult.get(0).getString("devicename");
                    if (!device2deviceName.containsKey(u)) {
                        //设备对应的名称
                        device2deviceName.put(u, deviceName);
                    }
                }
            }
        }
    }

    /**
     * 通过对应设备下的WX好友账号及对应的多个WX群号查询对应的聊天信息总数
     *
     * @param splitByComma 对应设备下所有的WX号
     * @param searchNum    对应的共同好友节点WX号
     * @return
     */
    private List<Map<String, Object>> getWXUserTroopMsgDetailByWXUinsAndRelationWXTroopUins(List<String> splitByComma, String searchNum) {
        String[] friendWXTroopUins = StringUtils.split(searchNum, ",");//多个共同好友的节点账号
        BasicDBObject query = //对应的查询条件
                new BasicDBObject("chatroomname", new BasicDBObject(QueryOperators.IN, friendWXTroopUins))
                        .append("username", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> groupResult = //不传值时,即为对应QQ号下的所有设备
                impactSimpleDao.filterImpactWXTroopResultByCountAndQuery(0, query);
        Map<String, String> uin2Nick = impactSimpleDao.findWXUin2Nick(splitByComma);
        Map<String, String> troopUin2Nick =
                impactSimpleDao.getChatroomUin2TroopName(Arrays.asList(friendWXTroopUins));
        Map<String, HashSet<String>> senderUin2TroopUins = new HashMap<>();
        Map<String, Integer> senderUinAndTroopUin2Count = new HashMap<>();
        groupResult.forEach(t -> {
            Document troopUinAndSenderUin = t.get("_id", Document.class);
            String senderUin = troopUinAndSenderUin.getString("fuin");
            String troopUin = troopUinAndSenderUin.getString("uin");
            Integer count = t.getInteger("count");
            senderUinAndTroopUin2Count.put(senderUin + "," + troopUin,
                    count);
            HashSet<String> troopUins = senderUin2TroopUins.get(senderUin);
            if (troopUins == null) {
                troopUins = new HashSet<>();
                troopUins.add(troopUin);
                senderUin2TroopUins.put(senderUin, troopUins);
            } else {
                troopUins.add(troopUin);
                senderUin2TroopUins.put(senderUin, troopUins);
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(senderUin2TroopUins)) {
            Set<Entry<String, HashSet<String>>> set = senderUin2TroopUins.entrySet();
            for (Entry<String, HashSet<String>> entry : set) {
                Map<String, Object> oneQQUserDetail = new HashMap<>();
                String senderUin = entry.getKey();
                String addSelfUin =
                        uin2Nick.get(senderUin) != null ? "," + uin2Nick.get(senderUin) : "";
                oneQQUserDetail.put("type", senderUin + addSelfUin);
                HashSet<String> troopUins = entry.getValue();
                List<Map<String, Object>> troops = new ArrayList<>();
                troopUins.forEach(t -> {
                    Map<String, Object> oneTroop = new HashMap<>();
                    oneTroop.put("selfUin", t);
                    oneTroop.put("nick", troopUin2Nick.get(t));
                    oneTroop.put("fuin", senderUin);
                    oneTroop.put("count", senderUinAndTroopUin2Count.get(senderUin + "," + t));
                    troops.add(oneTroop);
                });
                oneQQUserDetail.put("list", troops);
                result.add(oneQQUserDetail);
            }
        }
        return result;
    }


    /**
     * 通过对应的设备下的QQ好友号以及群号对对应的设备进行查询
     *
     * @param qqUins    对应设备下的主控QQ号
     * @param searchNum 对应的共同群号
     * @return 返回对应前端需的数据格式
     */
    private List<Map<String, Object>> getQQUserTroopMsgDetailByQQUinsAndRelationQQTroopUins(List<String> qqUins, String searchNum) {
        String[] friendQQTroopUins = StringUtils.split(searchNum, ",");//多个共同好友的节点账号
        BasicDBObject query = //对应的查询条件
                new BasicDBObject("troopuin", new BasicDBObject(QueryOperators.IN, friendQQTroopUins))
                        .append("senderuin", new BasicDBObject(QueryOperators.IN, qqUins));
        List<Document> groupResult = //不传值时,即为对应QQ号下的所有设备
                impactSimpleDao.filterImpactQQTroopResultByCountAndQuery(0, query);
        Map<String, String> uin2Nick = impactSimpleDao.findNickAndUin(qqUins);
        Map<String, String> troopUin2Nick =
                impactSimpleDao.getTroopUinToTroopName(Arrays.asList(friendQQTroopUins));
        Map<String, HashSet<String>> senderUin2TroopUins = new HashMap<>();
        Map<String, Integer> senderUinAndTroopUin2Count = new HashMap<>();
        groupResult.forEach(t -> {
            Document troopUinAndSenderUin = t.get("_id", Document.class);
            String senderUin = troopUinAndSenderUin.getString("fuin");
            String troopUin = troopUinAndSenderUin.getString("uin");
            Integer count = t.getInteger("count");
            senderUinAndTroopUin2Count.put(senderUin + "," + troopUin,
                    count);
            HashSet<String> troopUins = senderUin2TroopUins.get(senderUin);
            if (troopUins == null) {
                troopUins = new HashSet<>();
                troopUins.add(troopUin);
                senderUin2TroopUins.put(senderUin, troopUins);
            } else {
                troopUins.add(troopUin);
                senderUin2TroopUins.put(senderUin, troopUins);
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(senderUin2TroopUins)) {
            Set<Entry<String, HashSet<String>>> set = senderUin2TroopUins.entrySet();
            for (Entry<String, HashSet<String>> entry : set) {
                Map<String, Object> oneQQUserDetail = new HashMap<>();
                String senderUin = entry.getKey();
                String addSelfUin = uin2Nick.get(senderUin) != null ? "," + uin2Nick.get(senderUin) : "";
                oneQQUserDetail.put("type", senderUin + addSelfUin);
                HashSet<String> troopUins = entry.getValue();
                List<Map<String, Object>> troops = new ArrayList<>();
                troopUins.forEach(t -> {
                    Map<String, Object> oneTroop = new HashMap<>();
                    oneTroop.put("selfUin", t);
                    oneTroop.put("nick", troopUin2Nick.get(t));
                    oneTroop.put("fuin", senderUin);
                    oneTroop.put("count", senderUinAndTroopUin2Count.get(senderUin + "," + t));
                    troops.add(oneTroop);
                });
                oneQQUserDetail.put("list", troops);
                result.add(oneQQUserDetail);
            }
        }
        return result;
    }

    /**
     * 通过设备下的WX好友账号与多个共同WX好友账号查询对应的WX聊天次数
     *
     * @param deviceWXUsers 对应的多个设备下的WX好友
     * @param searchNum     对应共同好友节点账号,多个以,隔开
     * @return 返回前端所需的数据格式
     */
    private List<Map<String, Object>> getWXUserFriendMsgDetailByWXUinsAndRelationWXUins(List<String> deviceWXUsers, String searchNum) {
        String[] friendWXUins = StringUtils.split(searchNum, ",");//多个共同好友的节点账号
        List<Map<String, Object>> result = new ArrayList<>();//结果返回
        List<String> c2cs = new ArrayList<>();
        Map<String, String[]> c2c2SelfAndFriendWXUin = new HashMap<>();
        deviceWXUsers.forEach(wxUin -> {
            for (String friendWXUin : friendWXUins) {//好友的QQ账号
                String[] selfAndFriendUin = new String[2];
                String c2c = Utils.StringUniqueMD5(wxUin, friendWXUin);
                c2cs.add(c2c);
                selfAndFriendUin[1] = friendWXUin;
                selfAndFriendUin[0] = wxUin;
                c2c2SelfAndFriendWXUin.put(c2c, selfAndFriendUin);
            }
        });
        Map<String, String> selfUinAndFriendUin2Remark =
                impactSimpleDao.findWXRemarkBySelfWXUinsAndFriendUins(deviceWXUsers, Arrays.asList(friendWXUins));
        //合并所有的QQ号
        deviceWXUsers.addAll(Arrays.asList(friendWXUins));
        Map<String, String> uniToNick = impactSimpleDao.findWXUin2Nick(deviceWXUsers);//查询wx账号的昵称
        BasicDBObject msgQuery = new BasicDBObject();
        msgQuery.append("c2cmark", new BasicDBObject(QueryOperators.IN, c2cs));
        logger.debug("msgQuery:" + msgQuery);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmark")
                //					.append(key, value))
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> msgGroupResult =
                impactSimpleDao.groupInfoBySomeTerms("infoData", "wxmsg", Arrays.asList(new BasicDBObject("$match", msgQuery), group, sort));
        Map<String, HashSet<String>> selfUin2FriendUin = new HashMap<>();//存储设备下WX与共同好友WX的关系
        Map<String, Integer> c2c2Count = new HashMap<>();
        msgGroupResult.forEach(t -> {
            String c2c = t.getString("_id");//c2c
            Integer count = t.getInteger("count");//对应聊天的总数
            c2c2Count.put(c2c, count);
            String[] selfUinAndFriendUin = c2c2SelfAndFriendWXUin.get(c2c);
            String selfUin = selfUinAndFriendUin[0];
            String friendUin = selfUinAndFriendUin[1];
            HashSet<String> friendUins = selfUin2FriendUin.get(selfUin);
            if (friendUins == null) {
                friendUins = new HashSet<>();
                friendUins.add(friendUin);
                selfUin2FriendUin.put(selfUin, friendUins);
            } else {
                friendUins.add(friendUin);
                selfUin2FriendUin.put(selfUin, friendUins);
            }
        });
        if (!CollectionUtils.mapIsEmpty(selfUin2FriendUin)) {
            Set<Entry<String, HashSet<String>>> set = selfUin2FriendUin.entrySet();
            for (Entry<String, HashSet<String>> entrySet : set) {
                Map<String, Object> oneWXDetail = new HashMap<>();//存储单个主控QQ号下的相关信息
                String selfUin = entrySet.getKey();//自己账号
                String addSelfUin = //所需添加的昵称
                        uniToNick.get(selfUin) != null ? "," + uniToNick.get(selfUin) : "";
                oneWXDetail.put("type", selfUin + addSelfUin);
                List<Map<String, Object>> friendDetails = new ArrayList<>();
                HashSet<String> qqFriendUins = entrySet.getValue();//好友账号集合
                qqFriendUins.forEach(t -> {
                    Map<String, Object> friendDetail = new HashMap<>();
                    String c2c = Utils.StringUniqueMD5(selfUin, t);
                    Integer count = c2c2Count.get(c2c);
                    friendDetail.put("fuin", t);
                    friendDetail.put("count", count);
                    friendDetail.put("selfUin", selfUin);//还差qq好友昵称及账号
                    friendDetail.put("nick",
                            uniToNick.get(t) != null ? uniToNick.get(t) : "");
                    friendDetail.put("remark",
                            selfUinAndFriendUin2Remark.get(selfUin + "," + t) != null ?
                                    selfUinAndFriendUin2Remark.get(selfUin + "," + t) : "");
                    friendDetails.add(friendDetail);
                });
                oneWXDetail.put("list", friendDetails);
                result.add(oneWXDetail);
            }
        }
        return result;
    }

    /**
     * 通过对应的设备下所有的
     *
     * @param qqUins
     * @param searchNum TODO : @param timeQuery
     * @return
     */
    private List<Map<String, Object>> getQQUserFriendMsgDetailByQQUinsAndRelationQQUins(List<String> qqUins, String searchNum) {
        String[] friendQQUins = StringUtils.split(searchNum, ",");//多个共同好友的节点账号
        List<Map<String, Object>> result = new ArrayList<>();//结果返回
        List<String> c2cs = new ArrayList<>();
        Map<String, String[]> c2c2SelfAndFriendQQUin = new HashMap<>();
        qqUins.forEach(qqUin -> {//循环设备下的QQ账号
            for (String friendQQUin : friendQQUins) {//好友的QQ账号
                String[] selfAndFriendUin = new String[2];
                String c2c = Utils.NumberStringUniqueMD5(getStringForLong(qqUin), getStringForLong(friendQQUin));
                c2cs.add(c2c);
                selfAndFriendUin[1] = friendQQUin;
                selfAndFriendUin[0] = qqUin;
                c2c2SelfAndFriendQQUin.put(c2c, selfAndFriendUin);
            }
        });

        Map<String, String> selfUinAndFriendUin2Remark =
                impactSimpleDao.findQQRemarkBySelfQQUinsAndFriendUins(qqUins, Arrays.asList(friendQQUins));
        //合并所有的QQ号
        qqUins.addAll(Arrays.asList(friendQQUins));
        Map<String, String> uniToNick = impactSimpleDao.findNickAndUin(qqUins);//查询qq账号的昵称
        //对对应的信息进行聚合
        BasicDBObject msgQuery = new BasicDBObject();
        msgQuery.append("c2cmsg_mark", new BasicDBObject(QueryOperators.IN, c2cs));
        logger.debug("msgQuery:" + msgQuery);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmsg_mark")
                //					.append(key, value))
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> msgGroupResult =
                impactSimpleDao.groupInfoBySomeTerms("infoData", "qqmsg", Arrays.asList(new BasicDBObject("$match", msgQuery), group, sort));
        Map<String, HashSet<String>> selfUin2FriendUin = new HashMap<>();//存储设备下qq与共同好友QQ的关系
        Map<String, Integer> c2c2Count = new HashMap<>();
        msgGroupResult.forEach(t -> {
            String c2c = t.getString("_id");//c2c
            Integer count = t.getInteger("count");//对应聊天的总数
            c2c2Count.put(c2c, count);
            String[] selfUinAndFriendUin = c2c2SelfAndFriendQQUin.get(c2c);
            String selfUin = selfUinAndFriendUin[0];
            String friendUin = selfUinAndFriendUin[1];
            HashSet<String> friendUins = selfUin2FriendUin.get(selfUin);
            if (friendUins == null) {
                friendUins = new HashSet<>();
                friendUins.add(friendUin);
                selfUin2FriendUin.put(selfUin, friendUins);
            } else {
                friendUins.add(friendUin);
                selfUin2FriendUin.put(selfUin, friendUins);
            }
        });
        if (!CollectionUtils.mapIsEmpty(selfUin2FriendUin)) {
            Set<Entry<String, HashSet<String>>> set = selfUin2FriendUin.entrySet();
            for (Entry<String, HashSet<String>> entrySet : set) {
                Map<String, Object> oneQQDetail = new HashMap<>();//存储单个主控QQ号下的相关信息
                String selfUin = entrySet.getKey();//自己账号
                String addSelfUin = //所需添加的昵称
                        uniToNick.get(selfUin) != null ? "," + uniToNick.get(selfUin) : "";
                oneQQDetail.put("type", selfUin + addSelfUin);
                List<Map<String, Object>> friendDetails = new ArrayList<>();
                HashSet<String> qqFriendUins = entrySet.getValue();//好友账号集合
                qqFriendUins.forEach(t -> {
                    Map<String, Object> friendDetail = new HashMap<>();
                    String c2c = Utils.NumberStringUniqueMD5(getStringForLong(selfUin), getStringForLong(t));
                    Integer count = c2c2Count.get(c2c);
                    friendDetail.put("fuin", t);
                    friendDetail.put("count", count);
                    friendDetail.put("selfUin", selfUin);//还差qq好友昵称及账号
                    friendDetail.put("nick",
                            uniToNick.get(t) != null ? uniToNick.get(t) : "");
                    friendDetail.put("remark",
                            selfUinAndFriendUin2Remark.get(selfUin + "," + t) != null ?
                                    selfUinAndFriendUin2Remark.get(selfUin + "," + t) : "");
                    friendDetails.add(friendDetail);
                });
                oneQQDetail.put("list", friendDetails);
                result.add(oneQQDetail);
            }
        }
        return result;
    }

    /**
     * 通过共同好友的QQ账号及设备唯一标识查询对应的QQ群账号
     *
     * @param splitByComma 对应的设备唯一标识
     * @param searchNum    对应的QQ账号
     * @return
     */
    private List<Map<String, Object>> getQQGroupMsgByDeviceUniquesAndQQNumber(String[] splitByComma, String searchNum) {
        BasicDBObject qqUserQuery = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> qqUsers = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_qquser", qqUserQuery, null, null, null);
        HashSet<String> qqUserUins = new HashSet<>();//qq账号
        Map<String, String> qqUin2Device = new HashMap<>();//qq账号与设备关系
        List<String> deviceUniques = new ArrayList<>();
        if (!CollectionUtils.isEmpty(qqUsers)) {//qq账号与设备唯一标识的关系
            List<String> deviceUniqeArray = Arrays.asList(splitByComma);
            qqUsers.forEach(t -> {
                String qqUin = t.getString("uin");
                qqUserUins.add(qqUin);
                ArrayList<?> qqDeviceUniques = t.get("device_unique", ArrayList.class);
                if (!CollectionUtils.isEmpty(qqDeviceUniques)) {
                    qqDeviceUniques.forEach(t1 -> {
                        if (t1 != null && deviceUniqeArray.contains(t1)) {
                            qqUin2Device.put(qqUin, t1.toString());
                            if (!deviceUniques.contains(t1)) {//获取设备唯一标识
                                deviceUniques.add(t1.toString());
                            }
                        }
                    });
                }
            });
        }
        Map<String, String> uin2Nick = //查询对应的qq账号的昵称
                impactSimpleDao.findNickAndUin(new ArrayList<>(qqUserUins));
        Map<String, String[]> personPhoneAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(deviceUniques.toArray(new String[]{}));
        //查询设备唯一标识与设备名的关系
        BasicDBObject deviceQuery = new BasicDBObject(
                "device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> devices = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_device", deviceQuery, null, null, null);
        Map<String, Object> deviceUnique2DeviceName = new HashMap<>();//设备唯一标识与设备的关系
        devices.forEach(t -> {
            String deviceUnique = t.getString("device_unique");
            String deviceName = t.getString("devicename");
            deviceUnique2DeviceName.put(deviceUnique, deviceName);
        });

        BasicDBObject qqMsgQuery = new BasicDBObject("$match", new BasicDBObject("troopuin", searchNum)
                .append("senderuin", new BasicDBObject(QueryOperators.IN, qqUserUins.toArray(new String[]{}))));
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$senderuin")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> qqGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "qqTroopMsg", Arrays.asList(qqMsgQuery, group, sort));
        Map<String, Integer> senderUin2Count = new HashMap<>();//发送者uin聊天信息数的关系
        Map<String, HashSet<String>> deviceUnique2SenderUins =
                new HashMap<>();//一个设备可能有多个QQ号在一个群中发言
        qqGroupResult.forEach(t -> {
            String senderUin = t.getString("_id");
            Integer count = t.getInteger("count");
            senderUin2Count.put(senderUin, count);
            String deviceUnique = qqUin2Device.get(senderUin);
            HashSet<String> set = deviceUnique2SenderUins.get(deviceUnique);
            if (set == null) {
                set = new HashSet<>();
                set.add(senderUin);
                deviceUnique2SenderUins.put(deviceUnique, set);
            } else {
                set.add(senderUin);
                deviceUnique2SenderUins.put(deviceUnique, set);
            }
            //			t.append("uin", );//应是QQ账号
            //			t.append("deviceName", deviceUnique2DeviceName.get(qqUin2Device.get(t.getString("_id"))));
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(deviceUnique2SenderUins)) {
            Set<Entry<String, HashSet<String>>> set = deviceUnique2SenderUins.entrySet();
            for (Entry<String, HashSet<String>> entry : set) {
                Map<String, Object> oneDevice = new HashMap<>();
                String deviceUnqiue = entry.getKey();
                HashSet<String> senderUins = entry.getValue();
                String deviceNameAndPersonName =
                        personPhoneAndNumber.get(deviceUnqiue)[0] + "," + deviceUnique2DeviceName.get(deviceUnqiue);
                oneDevice.put("type", deviceNameAndPersonName);
                List<Map<String, Object>> list = new ArrayList<>();
                senderUins.forEach(t -> {
                    Map<String, Object> oneQQInfo = new HashMap<>();
                    oneQQInfo.put("fuin", t);
                    oneQQInfo.put("selfUin", searchNum);
                    oneQQInfo.put("nick", uin2Nick.get(t));
                    oneQQInfo.put("count", senderUin2Count.get(t));
                    list.add(oneQQInfo);
                });
                oneDevice.put("list", list);
                result.add(oneDevice);
            }
        }
        return result;
        //		qqGroupResult.forEach(t->{
        //			Document qqDoc = new Document();
        //			String uin = t.getString("uin");
        //			Object count = t.get("count");
        //			qqDoc.append("uin", uin);
        //			qqDoc.append("count", count);
        //			@SuppressWarnings("unchecked")
        //			ArrayList<Document> numbers = t.get("numbers",ArrayList.class);
        //			if(numbers == null){
        //				numbers = new ArrayList<>();
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}else{
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}
        //			t.remove("uin");
        //			t.remove("count");
        //		});
        //
        //		return qqGroupResult;
    }

    private List<Map<String, Object>> getWXGroupMsgByDeviceUniquesAndQQNumber(String[] splitByComma, String searchNum) {

        BasicDBObject qqUserQuery = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> qqUsers = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_wxuser", qqUserQuery, null, null, null);
        HashSet<String> qqUserUins = new HashSet<>();//qq账号
        Map<String, String> qqUin2Device = new HashMap<>();//qq账号与设备关系
        List<String> deviceUniques = new ArrayList<>();
        if (!CollectionUtils.isEmpty(qqUsers)) {//qq账号与设备唯一标识的关系
            List<String> deviceUniqeArray = Arrays.asList(splitByComma);
            qqUsers.forEach(t -> {
                String qqUin = t.getString("username");
                qqUserUins.add(qqUin);
                ArrayList<?> qqDeviceUniques = t.get("device_unique", ArrayList.class);
                if (!CollectionUtils.isEmpty(qqDeviceUniques)) {
                    qqDeviceUniques.forEach(t1 -> {
                        if (t1 != null && deviceUniqeArray.contains(t1)) {
                            qqUin2Device.put(qqUin, t1.toString());
                            if (!deviceUniques.contains(t1)) {//获取设备唯一标识
                                deviceUniques.add(t1.toString());
                            }
                        }
                    });
                }
            });
        }
        Map<String, String> uin2Nick = //查询对应的WX账号的昵称
                impactSimpleDao.findWXUin2Nick(new ArrayList<>(qqUserUins));
        Map<String, String[]> personPhoneAndNumber =
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(deviceUniques.toArray(new String[]{}));
        //查询设备唯一标识与设备名的关系
        BasicDBObject deviceQuery = new BasicDBObject(
                "device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> devices = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_device", deviceQuery, null, null, null);
        Map<String, Object> deviceUnique2DeviceName = new HashMap<>();//设备唯一标识与设备的关系
        devices.forEach(t -> {
            String deviceUnique = t.getString("device_unique");
            String deviceName = t.getString("devicename");
            deviceUnique2DeviceName.put(deviceUnique, deviceName);
        });

        BasicDBObject qqMsgQuery = new BasicDBObject("$match", new BasicDBObject("chatroomname", searchNum)
                .append("username", new BasicDBObject(QueryOperators.IN, qqUserUins.toArray(new String[]{}))));
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$username")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> qqGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "wxChatroomMsg", Arrays.asList(qqMsgQuery, group, sort));
        Map<String, Integer> senderUin2Count = new HashMap<>();//发送者uin聊天信息数的关系
        Map<String, HashSet<String>> deviceUnique2SenderUins =
                new HashMap<>();//一个设备可能有多个QQ号在一个群中发言
        qqGroupResult.forEach(t -> {
            String senderUin = t.getString("_id");
            Integer count = t.getInteger("count");
            senderUin2Count.put(senderUin, count);
            String deviceUnique = qqUin2Device.get(senderUin);
            HashSet<String> set = deviceUnique2SenderUins.get(deviceUnique);
            if (set == null) {
                set = new HashSet<>();
                set.add(senderUin);
                deviceUnique2SenderUins.put(deviceUnique, set);
            } else {
                set.add(senderUin);
                deviceUnique2SenderUins.put(deviceUnique, set);
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(deviceUnique2SenderUins)) {
            Set<Entry<String, HashSet<String>>> set = deviceUnique2SenderUins.entrySet();
            for (Entry<String, HashSet<String>> entry : set) {
                Map<String, Object> oneDevice = new HashMap<>();
                String deviceUnqiue = entry.getKey();
                HashSet<String> senderUins = entry.getValue();
                String deviceNameAndPersonName =
                        personPhoneAndNumber.get(deviceUnqiue)[0] + "," + deviceUnique2DeviceName.get(deviceUnqiue);
                oneDevice.put("type", deviceNameAndPersonName);
                List<Map<String, Object>> list = new ArrayList<>();
                senderUins.forEach(t -> {
                    Map<String, Object> oneWXInfo = new HashMap<>();
                    oneWXInfo.put("fuin", t);
                    oneWXInfo.put("selfUin", searchNum);
                    oneWXInfo.put("nick", uin2Nick.get(t));
                    oneWXInfo.put("count", senderUin2Count.get(t));
                    list.add(oneWXInfo);
                });
                oneDevice.put("list", list);
                result.add(oneDevice);
            }
        }
        return result;
        //		qqGroupResult.forEach(t->{
        //			Document qqDoc = new Document();
        //			String uin = t.getString("uin");
        //			Object count = t.get("count");
        //			qqDoc.append("uin", uin);
        //			qqDoc.append("count", count);
        //			@SuppressWarnings("unchecked")
        //			ArrayList<Document> numbers = t.get("numbers",ArrayList.class);
        //			if(numbers == null){
        //				numbers = new ArrayList<>();
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}else{
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}
        //			t.remove("uin");
        //			t.remove("count");
        //		});
        //
        //		return qqGroupResult;
    }

    /**
     * 通过设备唯一标识及wx号查询对应的的好友详情
     *
     * @param splitByComma
     * @param searchNum
     * @return
     */
    private List<Map<String, Object>> getWXFriendMsgByDeviceUniquesAndWXNumber(String[] splitByComma, String searchNum) {
        BasicDBObject wxUserQuery = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> wxUsers = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_wxuser", wxUserQuery, null, null, null);
        HashSet<String> wxUserUins = new HashSet<>();//qq账号
        Map<String, String> wxUin2Device = new HashMap<>();//qq账号与设备关系
        Map<String, String> c2c2wxUin = new HashMap<>();

        if (!CollectionUtils.isEmpty(wxUsers)) {//qq账号与设备唯一标识的关系
            List<String> deviceUniqeArray = Arrays.asList(splitByComma);
            wxUsers.forEach(t -> {
                String wxUin = t.getString("username");
                wxUserUins.add(wxUin);
                ArrayList<?> wxDeviceUniques = t.get("device_unique", ArrayList.class);
                if (!CollectionUtils.isEmpty(wxDeviceUniques)) {
                    wxDeviceUniques.forEach(t1 -> {
                        if (t1 != null && deviceUniqeArray.contains(t1)) {
                            wxUin2Device.put(wxUin, t1.toString());
                        }
                    });
                }
            });
        }
        Map<String, String> uin2Nick =
                impactSimpleDao.findWXUin2Nick(new ArrayList<>(wxUserUins));
        //查询设备唯一标识与设备名的关系
        BasicDBObject deviceQuery = new BasicDBObject(
                "device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> devices = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_device", deviceQuery, null, null, null);
        Map<String, Object> deviceUnique2DeviceName = new HashMap<>();//设备唯一标识与设备的关系
        devices.forEach(t -> {
            String deviceUnique = t.getString("device_unique");
            String deviceName = t.getString("devicename");
            deviceUnique2DeviceName.put(deviceUnique, deviceName);
        });
        Map<String, String[]> deviceUnique2PersonNameAndNumber = //查询设备唯一标识与人员姓名以及身份证号的关系
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(splitByComma);
        //c2c对WX账号的关系
        List<String> c2cs = new ArrayList<>();
        wxUserUins.forEach(t -> {
            String c2c = Utils.StringUniqueMD5(searchNum, t);
            c2cs.add(c2c);
            c2c2wxUin.put(c2c, t);
        });
        BasicDBObject wxFriendMsgQuery = new BasicDBObject("$match", new BasicDBObject("c2cmark",
                new BasicDBObject(QueryOperators.IN, c2cs.toArray(new String[]{}))));
        //		//logger.debug("qqFriendMsgQuery:"+wxFriendMsgQuery);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmark")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        Map<String, HashSet<String>> deviceUnique2HasMessageWXUin = new HashMap<>();//存储设备下有共同聊天记录的QQ账号
        Map<String, Integer> c2c2Count = new HashMap<>();
        List<Document> wxMsgGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "wxmsg", Arrays.asList(wxFriendMsgQuery, group, sort));
        wxMsgGroupResult.forEach(t -> {
            t.append("wxUin", c2c2wxUin.get(t.getString("_id")));
            t.append("deviceName", deviceUnique2DeviceName.get(wxUin2Device.get(c2c2wxUin.get(t.getString("_id")))));
        });
        wxMsgGroupResult.forEach(t -> {
            c2c2Count.put(t.getString("_id"), t.getInteger("count"));
            String deviceWXUin = c2c2wxUin.get(t.getString("_id"));
            String deviceUniuqe = wxUin2Device.get(c2c2wxUin.get(t.getString("_id")));
            HashSet<String> hasMsgWXUins = deviceUnique2HasMessageWXUin.get(deviceUniuqe);
            if (hasMsgWXUins == null) {
                hasMsgWXUins = new HashSet<>();
                hasMsgWXUins.add(deviceWXUin);
                deviceUnique2HasMessageWXUin.put(deviceUniuqe, hasMsgWXUins);
            } else {
                hasMsgWXUins.add(deviceWXUin);
                deviceUnique2HasMessageWXUin.put(deviceUniuqe, hasMsgWXUins);
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(deviceUnique2HasMessageWXUin)) {
            Set<Entry<String, HashSet<String>>> set = deviceUnique2HasMessageWXUin.entrySet();
            for (Entry<String, HashSet<String>> entrySet : set) {
                String deviceUnique = entrySet.getKey();
                Map<String, Object> oneWXDetail = new HashMap<>();
                String[] personNameAndNumber =
                        deviceUnique2PersonNameAndNumber.get(deviceUnique);
                oneWXDetail.put(
                        "type", personNameAndNumber[0] + "," + deviceUnique2DeviceName.get(deviceUnique));
                HashSet<String> hasMsgWXUins = entrySet.getValue();
                List<Map<String, Object>> qqList = new ArrayList<>();
                hasMsgWXUins.forEach(t -> {
                    Map<String, Object> oneWXInfo = new HashMap<>();//1个QQ的信息
                    String c2c = Utils.StringUniqueMD5(searchNum, t);
                    Integer count = c2c2Count.get(c2c);
                    oneWXInfo.put("count", count);
                    oneWXInfo.put("selfUin", t);
                    oneWXInfo.put("fuin", searchNum);
                    oneWXInfo.put("nick", uin2Nick.get(t) != null ? uin2Nick.get(t) : "");
                    qqList.add(oneWXInfo);
                });
                oneWXDetail.put("list", qqList);
                result.add(oneWXDetail);
            }
        }
        return result;
        //		//此时将数据处理成对应的数据格式
        //		wxMsgGroupResult.forEach(t->{
        //			Document qqDoc = new Document();
        //			String uin = t.getString("wxUin");
        //			Object count = t.get("count");
        //			qqDoc.append("uin", uin);
        //			qqDoc.append("count", count);
        //			@SuppressWarnings("unchecked")
        //			ArrayList<Document> numbers = t.get("numbers",ArrayList.class);
        //			if(numbers == null){
        //				numbers = new ArrayList<>();
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}else{
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}
        //			t.remove("wxUin");
        //			t.remove("count");
        //		});

        //		return wxMsgGroupResult;
    }

    /**
     * 通过对应的设备唯一标识及QQ账号查询对应的QQ账号与设备中的那些好友有聊天信息
     *
     * @param splitByComma 对应的设备唯一标识数组
     * @param searchNum    对应的搜索账号
     * @return 返回结果
     */
    private List<Map<String, Object>> getQQFrinedMsgByDeviceUniquesAndQQNumber(String[] splitByComma, String searchNum) {
        BasicDBObject qqUserQuery = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> qqUsers = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_qquser", qqUserQuery, null, null, null);
        HashSet<String> qqUserUins = new HashSet<>();//qq账号
        Map<String, String> qqUin2Device = new HashMap<>();//qq账号与设备关系
        Map<String, String> c2c2QQUin = new HashMap<>();
        //只需查询对应设备账号的昵称
        if (!CollectionUtils.isEmpty(qqUsers)) {//qq账号与设备唯一标识的关系
            List<String> deviceUniqeArray = Arrays.asList(splitByComma);
            qqUsers.forEach(t -> {
                String qqUin = t.getString("uin");
                qqUserUins.add(qqUin);
                ArrayList<?> qqDeviceUniques = t.get("device_unique", ArrayList.class);
                if (!CollectionUtils.isEmpty(qqDeviceUniques)) {
                    qqDeviceUniques.forEach(t1 -> {
                        if (t1 != null && deviceUniqeArray.contains(t1)) {
                            qqUin2Device.put(qqUin, t1.toString());
                        }
                    });
                }
            });
        }
        Map<String, String> uin2Nick =
                impactSimpleDao.findNickAndUin(new ArrayList<>(qqUserUins));

        Map<String, String[]> deviceUnique2PersonNameAndNumber = //查询设备唯一标识与人员姓名以及身份证号的关系
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(splitByComma);
        //查询设备唯一标识与设备名的关系
        BasicDBObject deviceQuery = new BasicDBObject(
                "device_unique", new BasicDBObject(QueryOperators.IN, splitByComma));
        List<Document> devices = impactSimpleDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_device", deviceQuery, null, null, null);
        Map<String, Object> deviceUnique2DeviceName = new HashMap<>();//设备唯一标识与设备的关系
        devices.forEach(t -> {
            String deviceUnique = t.getString("device_unique");
            String deviceName = t.getString("devicename");
            deviceUnique2DeviceName.put(deviceUnique, deviceName);
        });

        //c2c对QQ账号的关系
        List<String> c2cs = new ArrayList<>();
        qqUserUins.forEach(t -> {
            String c2c = Utils.NumberStringUniqueMD5(getStringForLong(searchNum), getStringForLong(t));
            c2cs.add(c2c);
            c2c2QQUin.put(c2c, t);
        });
        BasicDBObject qqFriendMsgQuery = new BasicDBObject("$match", new BasicDBObject("c2cmsg_mark",
                new BasicDBObject(QueryOperators.IN, c2cs.toArray(new String[]{}))));
        //		//logger.debug("qqFriendMsgQuery:"+qqFriendMsgQuery);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmsg_mark")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> qqMsgGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "qqmsg", Arrays.asList(qqFriendMsgQuery, group, sort));
        Map<String, HashSet<String>> deviceUnique2HasMessageQQUin = new HashMap<>();//存储设备下有共同聊天记录的QQ账号
        Map<String, Integer> c2c2Count = new HashMap<>();
        qqMsgGroupResult.forEach(t -> {
            c2c2Count.put(t.getString("_id"), t.getInteger("count"));
            String deviceQQUin = c2c2QQUin.get(t.getString("_id"));
            String deviceUniuqe = qqUin2Device.get(c2c2QQUin.get(t.getString("_id")));
            HashSet<String> hasMsgQQUins = deviceUnique2HasMessageQQUin.get(deviceUniuqe);
            if (hasMsgQQUins == null) {
                hasMsgQQUins = new HashSet<>();
                hasMsgQQUins.add(deviceQQUin);
                deviceUnique2HasMessageQQUin.put(deviceUniuqe, hasMsgQQUins);
            } else {
                hasMsgQQUins.add(deviceQQUin);
                deviceUnique2HasMessageQQUin.put(deviceUniuqe, hasMsgQQUins);
            }
        });
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.mapIsEmpty(deviceUnique2HasMessageQQUin)) {
            Set<Entry<String, HashSet<String>>> set = deviceUnique2HasMessageQQUin.entrySet();
            for (Entry<String, HashSet<String>> entrySet : set) {
                String deviceUnique = entrySet.getKey();
                Map<String, Object> oneQQDetail = new HashMap<>();
                String[] personNameAndNumber =
                        deviceUnique2PersonNameAndNumber.get(deviceUnique);
                oneQQDetail.put(
                        "type", personNameAndNumber[0] + "," + deviceUnique2DeviceName.get(deviceUnique));
                HashSet<String> hasMsgQQUins = entrySet.getValue();
                List<Map<String, Object>> qqList = new ArrayList<>();
                hasMsgQQUins.forEach(t -> {
                    Map<String, Object> oneQQInfo = new HashMap<>();//1个QQ的信息
                    String c2c = Utils.NumberStringUniqueMD5(getStringForLong(searchNum), getStringForLong(t));
                    Integer count = c2c2Count.get(c2c);
                    oneQQInfo.put("count", count);
                    oneQQInfo.put("selfUin", t);
                    oneQQInfo.put("fuin", searchNum);
                    oneQQInfo.put("nick", uin2Nick.get(t) != null ? uin2Nick.get(t) : "");
                    qqList.add(oneQQInfo);
                });
                oneQQDetail.put("list", qqList);
                result.add(oneQQDetail);
            }
        }
        return result;
        //		//此时将数据处理成对应的数据格式
        //		qqMsgGroupResult.forEach(t->{
        //			Document qqDoc = new Document();
        //			String uin = t.getString("uin");
        //			Object count = t.get("count");
        //			qqDoc.append("uin", uin);
        //			qqDoc.append("count", count);
        //			@SuppressWarnings("unchecked")
        //			ArrayList<Document> numbers = t.get("numbers",ArrayList.class);
        //			if(numbers == null){
        //				numbers = new ArrayList<>();
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}else{
        //				numbers.add(qqDoc);
        //				t.append("numbers", numbers);
        //			}
        //			t.remove("uin");
        //			t.remove("count");
        //		});
        //		return qqMsgGroupResult;
    }

    /**
     * 通过电话号码与设备唯一标识查询对应的通讯录聊天详情
     *
     * @param deviceUniques 设备唯一标识集合
     * @param searchNum     对应的电话号码
     * @return
     */
    private List<Map<String, Object>> getContactPhoneMsgAndRecordByDeviceUniquesAndPhone(String[] deviceUniques, String searchNum) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String[]> deviceUnique2PersonNameAndNumber = //查询设备唯一标识与人员账号与人名的关联关系
                impactSimpleDao.findDeviceUnique2PersonNameAndNumber(deviceUniques);
        BasicDBObject deviceQuery = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, deviceUniques));
        //		//logger.debug("aaaaaaaaaa:"+deviceQuery);
        List<Document> devices = impactSimpleDao.findInfoByGatherNameAndQuery("infoData2", "t_device", deviceQuery, null, null, null);
        Map<String, Object> deviceUnique2DeviceName = new HashMap<>();//设备唯一标识与设备的关系
        devices.forEach(t -> {
            String deviceUnique = t.getString("device_unique");
            String deviceName = t.getString("devicename");
            deviceUnique2DeviceName.put(deviceUnique, deviceName);
        });
        //		//logger.debug("aaaaaaaaaaaaaaaaaaaaa:"+deviceUnique2DeviceName);
        BasicDBObject msgQuery = new BasicDBObject("$match", new BasicDBObject("phonenum", searchNum)
                .append("deviceUnique", new BasicDBObject(QueryOperators.IN, deviceUniques)));
        //logger.debug("msgQuery:"+msgQuery);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$deviceUnique")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> msgGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "message", Arrays.asList(msgQuery, group, sort));
        if (!CollectionUtils.isEmpty(msgGroupResult)) {
            msgGroupResult.forEach(t -> {
                t.append("deviceName", deviceUnique2DeviceName.get(t.get("_id")));
                String[] personNameAndNumber = deviceUnique2PersonNameAndNumber.get(t.get("_id"));
                t.append("personName", personNameAndNumber[0]);
                t.append("personNumber", personNameAndNumber[1]);

            });
        }
        //		//logger.debug("msgGroupResult:"+msgGroupResult);
        Map<String, Object> shortMessage = new HashMap<>();
        shortMessage.put("type", "ShortMessage");
        shortMessage.put("list", msgGroupResult);
        //统计通讯记录详情
        BasicDBObject recordQuery = new BasicDBObject("$match", new BasicDBObject("phonenum", searchNum)
                .append("deviceUnique", new BasicDBObject(QueryOperators.IN, deviceUniques)));
        //logger.debug("recordQuery:"+recordQuery);
        BasicDBObject recordGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$deviceUnique")
                //					.append(key, value))
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject recordSort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> recordGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "record", Arrays.asList(recordQuery, recordGroup, recordSort));

        if (!CollectionUtils.isEmpty(recordGroupResult)) {
            recordGroupResult.forEach(t -> {
                //logger.debug(">>>>>>>>>>>>>>>>>>>>>"+deviceUnique2DeviceName.get(t.get("_id")));
                t.append("deviceName", deviceUnique2DeviceName.get(t.get("_id")));
                String[] personNameAndNumber = deviceUnique2PersonNameAndNumber.get(t.get("_id"));
                t.append("personName", personNameAndNumber[0]);
                t.append("personNumber", personNameAndNumber[1]);
            });
        }
        //logger.debug("recordGroupResult:"+recordGroupResult);
        Map<String, Object> callRecord = new HashMap<>();
        callRecord.put("type", "CallRecord");
        callRecord.put("list", recordGroupResult);
        result.add(shortMessage);
        result.add(callRecord);
        return result;
    }

    /**
     * 通过对应的对应的设备deviceUnique查询对应的非通讯录聊天详情
     *
     * @param deviceUniques 对应的设备唯一标识
     */
    private List<Map<String, Object>> getNoContactPhoneMsgAndRecordByDeviceUnique(String deviceUniques, String searchNum) {
        logger.debug("deviceUniques:" + deviceUniques);
        logger.debug("searchNum:" + searchNum);
        List<Map<String, Object>> result = new ArrayList<>();
        String[] phoneNums1 = //共同好友的电话号码
                com.hnf.honeycomb.util.Utils.bongDuplicateremove(searchNum.split(","));
        //设备号对应的电话
        List<String> phoneNums = new ArrayList<>();
        List<Document> documents = null;
        for (String phoneNum : phoneNums1) {
            documents = impactSimpleDao.personFindOne(phoneNum);
            phoneNums.add(phoneNum);
        }
        if (documents != null && documents.size() > 0) {
            phoneNums.addAll((List<String>) documents.get(0).get("phone"));
        }

        //获取对应的通讯录列表

        BasicDBObject msgQuery = new BasicDBObject("deviceUnique", deviceUniques);
        msgQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, phoneNums));
        logger.debug("msgQuery:" + msgQuery);

        BasicDBObject msgGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum").append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject msgSort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> msgGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "message", Arrays.asList(new BasicDBObject("$match", msgQuery), msgGroup, msgSort));
        Map<String, Object> shortMessage = new HashMap<>();
        shortMessage.put("type", "ShortMessage");
        shortMessage.put("list", msgGroupResult);

        BasicDBObject recordQuery = new BasicDBObject("deviceUnique", deviceUniques);
        recordQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, phoneNums));
        logger.debug("recordQuery:" + recordQuery);

        BasicDBObject recordGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum").append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject recordSort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> recordGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "record", Arrays.asList(new BasicDBObject("$match", recordQuery), recordGroup, recordSort));
        Map<String, Object> callRecord = new HashMap<>();
        callRecord.put("type", "CallRecord");
        callRecord.put("list", recordGroupResult);

        result.add(callRecord);
        result.add(shortMessage);
        return result;
    }

    /**
     * 通过对应的设备deviceUnique查询通讯录的短消息及通讯录详情
     *
     * @param deviceUniques 对应的设备唯一标识
     * @param searchNum     对应的设备唯一标识
     */
    private List<Map<String, Object>> getContactPhoneMsgAndRecordByDeviceUnique(String deviceUniques, String searchNum) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] phoneNums = //共同好友的电话号码
                com.hnf.honeycomb.util.Utils.bongDuplicateremove(searchNum.split(","));
        //设备号对应的电话
        List<String> strings = new ArrayList<>();
        for (String phoneNum : phoneNums) {
            Pattern compile = Pattern.compile("^\\w+$");
            boolean b = compile.matcher(phoneNum).find();
            if(b){
                List<Document> documents = impactSimpleDao.personFindOne(phoneNum);
                if (documents.size() > 0) {
                    for (Document document : documents) {
                        strings.addAll((List<String>) document.get("phone"));
                    }
                }
            }
            strings.add(phoneNum);
        }
        //获取对应的通讯录列表
        List<Document> contactPhones = impactSimpleDao.findContactPhoneByDeviceUnqiue(deviceUniques);
        //电话号码对备注的信息
        Map<String, Object> phone2Remark = new HashMap<>();
        contactPhones.forEach(t -> {
            String phone = t.getString("phonenum");
            String remark = t.getString("personname");
            if (!StringUtils.isEmpty(phone)) {
                phone2Remark.put(phone, remark);
            }
        });
        BasicDBObject msgQuery = new BasicDBObject("deviceUnique", deviceUniques);
        msgQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, strings));
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> msgGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "message", Arrays.asList(new BasicDBObject("$match", msgQuery), group, sort));
        msgGroupResult.forEach(t -> t.append("peronName", phone2Remark.get(t.getString("_id"))));
        Map<String, Object> shortMessage = new HashMap<>();
        shortMessage.put("type", "ShortMessage");
        shortMessage.put("list", msgGroupResult);

        BasicDBObject recordQuery = new BasicDBObject("deviceUnique", deviceUniques);
        recordQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, strings));
        BasicDBObject recordGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum")
                .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject recordSort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<Document> recordGroupResult = impactSimpleDao.groupInfoBySomeTerms("infoData", "record", Arrays.asList(new BasicDBObject("$match", recordQuery), recordGroup, recordSort));
        recordGroupResult.forEach(t -> {
            t.append("peronName", phone2Remark.get(t.getString("_id")));
        });
        Map<String, Object> callRecord = new HashMap<>();
        callRecord.put("type", "CallRecord");
        callRecord.put("list", recordGroupResult);
        result.add(callRecord);
        result.add(shortMessage);
        return result;

    }

    @Override
    public Map<String, Object> findMsgDetails(Integer type, Integer pageNumber, Integer pageSize, String uin, String fuin, String startDateString, String endDateString, String searchContent, Integer timeSelectType) {
        // TODO Auto-generated method stub
        if (type == null) {
            throw new RuntimeException("对应的搜索类型为NULL");
        }
        Map<String, Object> resultMap;
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Long startDate = Optional.ofNullable(startDateString)
                .filter(StringUtils::isNotBlank)
                .map(d -> StringUtils.isNumeric(d) ? Long.valueOf(d) : ZonedDateTime.parse(d, formatter).toInstant().toEpochMilli())
                .orElse(null);
        Long endDate = Optional.ofNullable(endDateString)
                .filter(StringUtils::isNotBlank)
                .map(d -> StringUtils.isNumeric(d) ? Long.valueOf(d) : ZonedDateTime.parse(d, formatter).toInstant().toEpochMilli())
                .orElse(null);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(timeSelectType, startDate, endDate);
        switch (type) {
            case 1://通话记录
                resultMap = getPhoneCallHistory(pageNumber, pageSize, uin, fuin, timeQuery);
                break;
            case 2://短消息
                resultMap = getShortMessageHistory(pageNumber, pageSize, uin, fuin, searchContent, timeQuery);
                break;
            case 3://QQ好友聊天信息
                resultMap = getQqMessageHistory(pageNumber, pageSize, uin, fuin, searchContent, timeQuery);
                break;
            case 4:// QQ群消息
                resultMap = getQqGroupHistory(pageNumber, pageSize, uin, fuin, searchContent, timeQuery);
                break;
            case 5://微信好友聊天信息
                resultMap = getWechatMessageHistory(pageNumber, pageSize, uin, fuin, searchContent, timeQuery);
                break;
            case 6://微信群消息
                resultMap = getWechatGroupHistory(pageNumber, pageSize, uin, fuin, searchContent, timeQuery);
                break;
            default:
                throw new RuntimeException("对应的关系类型不符合规范！");
        }
        return resultMap;
    }

    private Map<String, Object> getPhoneCallHistory(Integer pageNumber, int pageSize, String uin, String fuin, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (pageNumber == null) {
            throw new RuntimeException("传入对应的页码数有误");
        }
        if (uin == null || uin.trim().isEmpty()) {
            throw new RuntimeException("查询通讯录对应的设备unique为空");
        }
        if (fuin == null || fuin.trim().isEmpty()) {
            throw new RuntimeException("查询通讯录对应的电话号码为空");
        }
        query.append("deviceUnique", uin);
        query.append("phonenum", fuin);
        if (!timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "record", query, new BasicDBObject("time", 1), pageNumber, pageSize);
        map.put("message", result);
        //设备自身信息
        List<Document> personList = impactSimpleDao.personFindOne(uin);
        map.put("person", personList);
        //分页
        BasicDBObject recordBasicDBObject = new BasicDBObject();
        recordBasicDBObject.append("deviceUnique", uin).append("phonenum", fuin);
        if (!timeQuery.isEmpty()) {
            recordBasicDBObject.append("msgtime", timeQuery);
        }
        Map recordMap = impactSimpleDao.countRecord(recordBasicDBObject);
        Integer recordCount = (Integer) recordMap.get(uin);
        int recordmsgCount = 0;
        if (recordCount != null) {
            recordmsgCount = (int) Math.ceil((float) recordCount / pageSize);
        }
        map.put("msgCount", recordmsgCount);
        return map;
    }

    private Map<String, Object> getShortMessageHistory(Integer pageNumber, int pageSize, String uin, String fuin, String searchContent, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (pageNumber == null) {
            throw new RuntimeException("传入对应的页码数有误");
        }
        if (uin == null || uin.trim().isEmpty()) {
            throw new RuntimeException("查询通讯录对应的设备unique为空");
        }
        if (fuin == null || fuin.trim().isEmpty()) {
            throw new RuntimeException("查询通讯录对应的电话号码为空");
        }
        query.append("deviceUnique", uin);
        query.append("phonenum", fuin);
        if (!timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("content", pattern);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "message", query, new BasicDBObject("time", 1), pageNumber, pageSize);
        map.put("message", result);
        //设备自身信息
        List<Document> personList = impactSimpleDao.personFindOne(uin);
        map.put("person", personList);
        //分页
        BasicDBObject messageBasicDBObject = new BasicDBObject();
        messageBasicDBObject.append("deviceUnique", uin).append("phonenum", fuin);
        if (!timeQuery.isEmpty()) {
            messageBasicDBObject.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            messageBasicDBObject.append("content", pattern);
        }
        Map messageMap = impactSimpleDao.countMessage(messageBasicDBObject);
        Integer messageCount = (Integer) messageMap.get(uin);
        int msgCount = 0;
        if (messageCount != null) {
            msgCount = (int) Math.ceil((float) messageCount / pageSize);
        }
        map.put("msgCount", msgCount);
        return map;
    }

    private Map<String, Object> getQqMessageHistory(Integer pageNumber, int pageSize, String uin, String fuin, String searchContent, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (StringUtils.isEmpty(uin) || StringUtils.isEmpty(fuin)) {
            throw new RuntimeException("账号为空");
        }
        String qqc2cmark = Utils.NumberStringUniqueMD5(getStringForLong(uin), getStringForLong(fuin));
        query.append("c2cmsg_mark", qqc2cmark);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("msgdata", pattern);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "qqmsg", query,
                new BasicDBObject("msgtime", 1), pageNumber, pageSize);
        List<Document> friendqq1 = impactSimpleDao.findqq(fuin);
        // 聊天框上面 昵称 账号 备注 保存的集合 qquserFriend
        List<Document> qquserFriend = impactSimpleDao.qquserFriend(uin, fuin);
        map.put("FriendUin", fuin);
        String nickName = "";
        String friendRemakName = "";
        if (friendqq1 != null && friendqq1.size() != 0 && qquserFriend != null && qquserFriend.size() != 0) {
            Object friendQQObj = friendqq1.get(0).get("nickname");
            Object remakName = qquserFriend.get(0).get("friendremarkname");
            nickName = friendQQObj != null ? friendQQObj.toString() : "";
            friendRemakName = remakName != null ? remakName.toString() : "";
            map.put("nickname", nickName);
            map.put("friendRemakName", friendRemakName);
            if (nickName == "" || nickName.isEmpty()) {
                nickName = friendRemakName;
            }
        }
        String QQName = "";
        List<Document> findqqName = impactSimpleDao.findqq(uin);
        if (findqqName != null && findqqName != null) {
            Object findQQNameObj = findqqName.get(0).get("nickname");
            QQName = findQQNameObj != null ? findQQNameObj.toString() : "";
        }
        // 聊天信息中的QQ好友信息及QQ用户信息
        if (result != null) {
            for (Document doc1 : result) {
                if (uin.equals(doc1.get("senderuin"))) {
                    doc1.append("issend", 0);
                } else {
                    doc1.append("issend", 1);
                }
                ;
                doc1.append("QQName", QQName);
                doc1.append("nickName", nickName);
            }
        }
        map.put("message", result);
        // 聊天分页
        //			Long count = QQRMIUtils.countQQMsg(qqc2cmark);
        BasicDBObject qBasicDBObject = new BasicDBObject();
        qBasicDBObject.append("c2cmsg_mark", qqc2cmark);
        if (!timeQuery.isEmpty()) {
            qBasicDBObject.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            qBasicDBObject.append("msgdata", pattern);
        }
        Map map2 = impactSimpleDao.countQQFriendMsg(qBasicDBObject);
        //			//logger.debug("map2:"+map2);
        Integer count = (Integer) map2.get(qqc2cmark);
        //logger.debug("count:"+count);
        int qqmsgCount = 0;
        //			int pageSize = 100;
        if (count != null) {
            //				qqmsgCount = (int) (count / pageSize + 1);
            qqmsgCount = (int) Math.ceil((float) count / pageSize);
        }
        map.put("msgCount", qqmsgCount);
        return map;
    }

    private Map<String, Object> getQqGroupHistory(Integer pageNumber, int pageSize, String uin, String fuin, String searchContent, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (StringUtils.isEmpty(fuin)) {
            throw new RuntimeException("账号为空");
        }
        query.append("troopuin", fuin);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("msgdata", pattern);
        }
        if (!StringUtils.isEmpty(uin)) {
            //				String[] uins = StringUtils.splitByComma(uin);
            query.append("senderuin", uin);
            result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "qqTroopMsg", query,
                    new BasicDBObject("msgtime", 1), pageNumber, pageSize);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "qqTroopMsg", query,
                new BasicDBObject("msgtime", 1), pageNumber, pageSize);
        for (Document doc : result) {
            String uin2 = (String) doc.get("senderuin");// 发送者的qq
            List<Document> findqq = impactSimpleDao.findqq(uin2);// 获取群成员的昵称
            String nickname = "";
            if (findqq != null && !findqq.isEmpty()) {
                Object nickObj = findqq.get(0).get("nickname");
                nickname = nickObj != null ? nickObj.toString() : "";
            }
            doc.append("nickname", nickname);

        }
        map.put("message", result);
        // 查询群名称、群号、群公告
        List<Document> findTroopName = impactSimpleDao.findQQTroop(fuin);
        String troopName = "";
        String troopNumber = "";
        String troopMemo = "";
        for (Document document : findTroopName) {
            if (document.get("troopname") != null && document.get("troopname") != "") {
                troopName = document.get("troopname").toString();
            }
            if (document.get("troopuin") != null && document.get("troopuin") != "") {
                troopNumber = document.get("troopuin").toString();
            }
            if (document.get("troopmemo") != null && document.get("troopmemo") != "") {
                troopMemo = document.get("troopmemo").toString();
            }
            map.put("troopName", troopName);
            map.put("troopNumber", troopNumber);
            map.put("troopMemo", troopMemo);
        }

        // 分页
        int qqTrooMsgCount = 0;
        BasicDBObject qBasicDBObject2 = new BasicDBObject();
        qBasicDBObject2.append("troopuin", fuin);
        if (!timeQuery.isEmpty()) {
            qBasicDBObject2.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            qBasicDBObject2.append("msgdata", pattern);
        }
        Map qqMap = impactSimpleDao.countQQTroopMsg(qBasicDBObject2);
        Integer AllQQTroopMsg = (Integer) qqMap.get(fuin);
        //			//logger.debug("AllQQTroopMsg:"+AllQQTroopMsg);
        //			int pageSize4 = 100;
        if (AllQQTroopMsg != null) {
            //				qqTrooMsgCount = (int) ((float)AllQQTroopMsg / pageSize4 + 1);
            qqTrooMsgCount = (int) Math.ceil((float) AllQQTroopMsg / pageSize);
        }
        map.put("msgCount", qqTrooMsgCount);
        return map;
    }

    private Map<String, Object> getWechatMessageHistory(Integer pageNumber, int pageSize, String uin, String fuin, String searchContent, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (StringUtils.isEmpty(uin) || StringUtils.isEmpty(fuin)) {
            throw new RuntimeException("账号为空");
        }
        //logger.debug("uin:"+uin);
        //logger.debug("fuin:"+fuin);
        String wxc2cmark = Utils.StringUniqueMD5(uin, fuin);
        //logger.debug("wxc2cmark:"+wxc2cmark);
        query.append("c2cmark", wxc2cmark);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("msgdata", pattern);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "wxmsg", query,
                new BasicDBObject("msgtime", 1), pageNumber, pageSize);
        List<Document> friend = impactSimpleDao.wxuserFriend(uin, fuin);
        // 抽取聊天中的 微信名 nickname 微信用户的自命名
        List<Document> findwxName1 = impactSimpleDao.findwx(fuin);
        // 聊天框上面的微信朋友信息栏--账号
        map.put("friendUin", fuin);
        String FriendName = "";
        String nickName2 = "";
        if (friend != null && friend.size() != 0 && findwxName1 != null && findwxName1.size() != 0) {
            Object markName = friend.get(0).get("friendremarkname");
            Object nickNameObj = findwxName1.get(0).get("nickname");
            FriendName = markName != null ? markName.toString() : "";
            nickName2 = nickNameObj != null ? nickNameObj.toString() : "";
            // 聊天框上面的微信朋友信息栏--昵称
            map.put("friendName", nickName2);
            // 聊天框上面的微信朋友信息栏--备注
            map.put("friendRemakName", FriendName);
            // 如果没有备注名就用好友自身的名字
            if (FriendName == "" || FriendName.trim().isEmpty()) {
                FriendName = nickName2;
            }
        }
        List<Document> findwx = impactSimpleDao.findwx(uin);
        String wxName = "";
        if (findwx != null) {
            wxName = findwx.get(0).get("nickname").toString();
        }
        // 聊天信息中的WX好友信息及WX用户信息
        if (result != null) {
            for (Document doc1 : result) {
                // 找到当前微信的名字

                doc1.append("wxName", wxName);

                doc1.append("wxFriendName", FriendName);
            }
        }
        map.put("message", result);
        // 聊天分页
        int wxmsgCount = 0;
        //			Long countWXMsg = WXRMIUtils.countWXMsg(wxc2cmark);
        BasicDBObject qBasicDBObject3 = new BasicDBObject();
        qBasicDBObject3.append("c2cmark", wxc2cmark);
        if (!timeQuery.isEmpty()) {
            qBasicDBObject3.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            qBasicDBObject3.append("msgdata", pattern);
        }
        Map wxMap = impactSimpleDao.countWxFriendMsg(qBasicDBObject3);
        Integer countWXMsg = (Integer) wxMap.get(wxc2cmark);
        //			int pageSize2 = 100;
        if (countWXMsg != null) {
            //				wxmsgCount = (int) (countWXMsg / pageSize2 + 1);
            wxmsgCount = (int) Math.ceil((float) countWXMsg / pageSize);
        }
        map.put("msgCount", wxmsgCount);
        return map;
    }

    private Map<String, Object> getWechatGroupHistory(Integer pageNumber, int pageSize, String uin, String fuin, String searchContent, BasicDBObject timeQuery) {
        List<Document> result;
        BasicDBObject query = new BasicDBObject();
        Map<String, Object> map = new LinkedHashMap<>();
        if (StringUtils.isEmpty(fuin)) {
            throw new RuntimeException("账号为空");
        }
        query.append("chatroomname", fuin);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("msgname", pattern);
        }
        if (!StringUtils.isEmpty(uin)) {
            query.append("username", uin);
        }
        result = impactSimpleDao.findInfoByGatherNameAndQuery("infoData", "wxChatroomMsg", query,
                new BasicDBObject("msgtime", 1), pageNumber, pageSize);
        for (Document doc : result) {
            Object unameObj = doc.get("username");
            if (unameObj == null) {
                doc.append("nickName", "未知");
                continue;
            }
            String username = unameObj.toString();
            List<Document> findwx2 = impactSimpleDao.findwx(username);// 取群成员的昵称
            String nickname = "";
            if (findwx2 != null && !findwx2.isEmpty()) {
                nickname = findwx2.get(0).get("nickname").toString();
            }
            doc.append("nickName", nickname);
        }
        map.put("message", result);
        // 群聊天框上面的群信息
        // 抽取群名字chatroomnickname 抽取群号chatroomname
        List<Document> findwxchatroom = impactSimpleDao.findwxchatroom(fuin);
        String chatRoomName = "";// 群名字
        String chatRoomUser = "";// 群号
        if (findwxchatroom != null && !findwxchatroom.isEmpty()) {
            chatRoomName = findwxchatroom.get(0).get("chatroomnickname").toString();
            chatRoomUser = findwxchatroom.get(0).get("chatroomname").toString();
        }
        map.put("chatRoomName", chatRoomName);
        map.put("chatRoomUser", chatRoomUser);

        // 聊天分页
        int wxRoomMessageCount = 0;
        //			Long countWXChatroomMsg = WXRMIUtils.countWXChatroomMsg(fuin);// 群里的所有消息
        BasicDBObject qBasicDBObject4 = new BasicDBObject();
        qBasicDBObject4.append("chatroomname", fuin);
        if (!timeQuery.isEmpty()) {
            qBasicDBObject4.append("msgtime", timeQuery);
        }
        if (!StringUtils.isEmpty(searchContent)) {
            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            qBasicDBObject4.append("msgname", pattern);
        }
        Map wxRoomMap = impactSimpleDao.countWxTroopMsg(qBasicDBObject4);
        Integer countWXChatroomMsg = (Integer) wxRoomMap.get(fuin);
        //			int pageSize3 = 100;
        if (countWXChatroomMsg != null) {
            //				wxRoomMessageCount = (int) (countWXChatroomMsg / pageSize3 + 1);
            wxRoomMessageCount = (int) Math.ceil((float) countWXChatroomMsg / pageSize);
        }
        map.put("msgCount", wxRoomMessageCount);
        return map;
    }

    @Override
    public Map findFriendList(Integer type, String uin, String startDateString, String endDateString, String searchContent) throws Exception {
        // TODO Auto-generated method stub
        Map<Object, Object> map = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        BasicDBObject timeQuery = new BasicDBObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Date startDate = Optional.ofNullable(startDateString)
                .filter(StringUtils::isNotBlank)
                .map(d -> Date.from(ZonedDateTime.parse(d, formatter).toInstant()))
                .orElse(null);
        Date endDate = Optional.ofNullable(endDateString)
                .filter(StringUtils::isNotBlank)
                .map(d -> Date.from(ZonedDateTime.parse(d, formatter).toInstant()))
                .orElse(null);
        //此处增加对应的时间限制条件
        if (startDate != null && endDate != null) {
            timeQuery.append("$gte", startDate);
            timeQuery.append("$lte", endDate);
        }
        switch (type) {
            case 3://qq好友
                List<Document> findqq = impactSimpleDao.findqq(uin);
                findqq.forEach(doc -> {
                    if (!doc.containsKey("nickname")) {
                        doc.append("nickname", null);
                    }
                });
                map.put("user", findqq);
                List<Document> resultQqUserFriend = impactSimpleDao.qquserFriend(uin);

                List<String> c2cs = new ArrayList<>();
                List<String> qqUins = new ArrayList<>();
                Map<String, String> uin2C2c = new HashMap<>();
                List<Document> qqUserFriend = new ArrayList<>();
                Map<String, String> uinToRemark = new HashMap<>();
                if (resultQqUserFriend != null) {
                    for (Document doc : resultQqUserFriend) {
                        String fuin = getString(doc.get("fuin"));
                        String c2cmark = Utils.NumberStringUniqueMD5(getStringForLong(uin), getStringForLong(fuin));
                        qqUins.add(fuin);
                        c2cs.add(c2cmark);
                        uin2C2c.put(fuin, c2cmark);
                        uinToRemark.put(fuin, doc.getString("friendremarkname"));
                    }
                    query.append("c2cmsg_mark", new BasicDBObject(QueryOperators.IN, c2cs.toArray(new String[]{})));
                    if (!timeQuery.isEmpty()) {
                        query.append("msgtime", timeQuery);
                    }
                    if (!StringUtils.isEmpty(searchContent)) {
                        Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                        query.append("msgdata", pattern);
                    }
                    //				//logger.debug("query:"+query);
                    //查询对应的c2c对消息总数的对应关系
                    Map<String, Integer> c2cToCount = impactSimpleDao.countQQFriendMsg(query);
                    //				//logger.debug("c2cToCount:"+c2cToCount);
                    //查询账号与昵称的对应关系
                    Map<String, String> uniToNick = impactSimpleDao.findNickAndUin(qqUins);
                    for (String qqUin : qqUins) {
                        Document doc = new Document();
                        doc.append("selfUin", qqUin);
                        doc.append("friendremarkname", uinToRemark.get(qqUin) != null ? uinToRemark.get(qqUin) : "");
                        doc.append("nickname", uniToNick.get(qqUin) != null ? uniToNick.get(qqUin) : "");
                        doc.append("qqmsgcount", c2cToCount.get(uin2C2c.get(qqUin)) != null ? c2cToCount.get(uin2C2c.get(qqUin)) : 0);
                        doc.append("fuin", uin);
                        if ((Integer) doc.get("qqmsgcount") != 0) {
                            qqUserFriend.add(doc);
                        }
                    }
                    //				//logger.debug("qqUserFriend:"+qqUserFriend);
                    //排序------java8自带的
                    qqUserFriend.sort((soft1, soft2) -> {
                        Document document1 = soft1;
                        Document document2 = soft2;
                        Integer soft1Count = (Integer) document1.get("qqmsgcount");
                        //					//logger.debug("soft1Count:"+soft1Count);
                        Integer soft2Count = (Integer) document2.get("qqmsgcount");
                        //					//logger.debug("soft2Count:"+soft2Count);
                        return -soft1Count.compareTo(soft2Count);
                    });
                }
                map.put("friend", qqUserFriend);
                break;
            case 4://qq群
                List<Document> findqq2 = impactSimpleDao.findqq(uin);
                findqq2.forEach(doc -> {
                    if (!doc.containsKey("nickname")) {
                        doc.append("nickname", null);
                    }
                });
                map.put("user", findqq2);
                List<Document> resultTroopQqUser = impactSimpleDao.troopQquser(uin);
                List<String> troopUins = new LinkedList<>();
                List<Document> troops = new LinkedList<>();
                if (resultTroopQqUser != null) {
                    for (Document doc1 : resultTroopQqUser) {
                        troopUins.add(doc1.get("troopuin").toString());
                    }
                    if (!CollectionUtils.isEmpty(troopUins)) {
                        query.append("troopuin", new BasicDBObject(QueryOperators.IN, troopUins.toArray(new String[]{})));
                        if (!timeQuery.isEmpty()) {
                            query.append("msgtime", timeQuery);
                        }
                        if (!StringUtils.isEmpty(searchContent)) {
                            Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                            query.append("msgdata", pattern);
                        }
                        Map<String, Integer> troopUin2Count = impactSimpleDao.countQQTroopMsg(query);
                        Map<String, String> troopUin2TroopName = impactSimpleDao.getTroopUinToTroopName(troopUins);
                        for (String troopUin : troopUins) {
                            Document doc = new Document();
                            doc.append("troopuin", troopUin);
                            doc.append("qqTroopMsgcount", troopUin2Count.get(troopUin) != null ? troopUin2Count.get(troopUin) : 0);
                            doc.append("troopname", troopUin2TroopName.get(troopUin) != null ? troopUin2TroopName.get(troopUin) : "");
                            if ((Integer) doc.get("qqTroopMsgcount") != 0) {
                                troops.add(doc);
                            }
                        }
                    }
                    //排序------java8自带的
                    troops.sort((soft1, soft2) -> {
                        Document document1 = soft1;
                        Document document2 = soft2;
                        Integer soft1Count = (Integer) document1.get("qqTroopMsgcount");
                        Integer soft2Count = (Integer) document2.get("qqTroopMsgcount");
                        return -soft1Count.compareTo(soft2Count);
                    });
                }
                map.put("friend", troops);
                break;
            case 5://微信好友
                //用于存储对应的wx群账号,对其进行对应消息统计
                if (uin == null || uin.trim().isEmpty()) {
                    throw new RuntimeException("查询对应的wx账号为空");
                }
                //		WXRMIUtils wxRMIUtils = new WXRMIUtils();
                //----------------自己-------------------------
                List<Document> findwx = impactSimpleDao.findwx(uin);
                map.put("user", findwx);
                Long time1 = System.currentTimeMillis();
                List<Document> wxUserFriends = impactSimpleDao.wxuserFriend(uin);
                List<String> c2cMarks = new ArrayList<>();
                List<String> wxUins = new ArrayList<>();
                Map<String, String> uin2C2c2 = new HashMap<>();
                List<Document> wxUserFriend = new ArrayList<>();
                Map<String, String> uinToRemark2 = new HashMap<>();
                if (wxUserFriends != null) {
                    for (Document doc : wxUserFriends) {
                        String c2cmark = Utils.StringUniqueMD5(uin, doc.get("fusername").toString());
                        c2cMarks.add(c2cmark);
                        wxUins.add(doc.getString("fusername"));
                        uin2C2c2.put(doc.getString("fusername"), c2cmark);
                        uinToRemark2.put(doc.getString("fusername"), doc.getString("friendremarkname"));
                    }
                    query.append("c2cmark", new BasicDBObject(QueryOperators.IN, c2cMarks.toArray(new String[]{})));
                    if (!timeQuery.isEmpty()) {
                        query.append("msgtime", timeQuery);
                    }
                    if (!StringUtils.isEmpty(searchContent)) {
                        Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                        query.append("msgdata", pattern);
                    }
                    Map<String, Integer> c2cToCount = impactSimpleDao.countWxFriendMsg(query);
                    Map<String, String> uniToNick = impactSimpleDao.findNickAndUin(wxUins);
                    for (String wxUin : wxUins) {
                        Document doc = new Document();
                        doc.append("selfUin", wxUin);
                        doc.append("friendremarkname", uinToRemark2.get(wxUin) != null ? uinToRemark2.get(wxUin) : "");
                        doc.append("nickname", uniToNick.get(wxUin) != null ? uniToNick.get(wxUin) : "");
                        doc.append("wxmsgcount", c2cToCount.get(uin2C2c2.get(wxUin)) != null ? c2cToCount.get(uin2C2c2.get(wxUin)) : 0);
                        doc.append("fuin", uin);
                        if ((Integer) doc.get("wxmsgcount") != 0) {
                            wxUserFriend.add(doc);
                        }
                    }
                    //排序------java8自带的
                    wxUserFriend.sort((soft1, soft2) -> {
                        Document document1 = soft1;
                        Document document2 = soft2;
                        Integer soft1Count = (Integer) document1.get("wxmsgcount");
                        Integer soft2Count = (Integer) document2.get("wxmsgcount");
                        return -soft1Count.compareTo(soft2Count);
                    });
                }
                map.put("friend", wxUserFriend);
                break;
            case 6://微信群
                if (uin == null || uin.trim().isEmpty()) {
                    throw new RuntimeException("查询对应的wx账号为空");
                }
                List<Document> findwx2 = impactSimpleDao.findwx(uin);
                map.put("user", findwx2);
                List<Document> chatRoomWxUser = impactSimpleDao.chatroomWxuser(uin);
                List<String> wxChatroomUins = new ArrayList<>();
                List<Document> wxChatrooms = new LinkedList<>();

                if (chatRoomWxUser != null) {
                    for (Document doc : chatRoomWxUser) {
                        wxChatroomUins.add(doc.get("chatroomname").toString());
                    }
                    query.append("chatroomname", new BasicDBObject(QueryOperators.IN, wxChatroomUins.toArray(new String[]{})));
                    if (!timeQuery.isEmpty()) {
                        query.append("msgtime", timeQuery);
                    }
                    if (!StringUtils.isEmpty(searchContent)) {
                        Pattern pattern = Pattern.compile("^.*" + searchContent.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                        query.append("msgname", pattern);
                    }
                    Map<String, Integer> chatroomUin2MsgCount = impactSimpleDao.countWxTroopMsg(query);
                    Map<String, String> chatroonUin2troopName = impactSimpleDao.getChatroomUin2TroopName(wxChatroomUins);
                    for (String chatroomUin : wxChatroomUins) {
                        Document doc = new Document();
                        doc.append("chatroomname", chatroomUin);
                        doc.append("chatroomnickname", chatroonUin2troopName.get(chatroomUin) != null ? chatroonUin2troopName.get(chatroomUin) : "");
                        doc.append("wxChatroomMsgcount", chatroomUin2MsgCount.get(chatroomUin) != null ? chatroomUin2MsgCount.get(chatroomUin) : 0);
                        if ((Integer) doc.get("wxChatroomMsgcount") != 0) {
                            wxChatrooms.add(doc);
                        }
                    }
                    //排序------java8自带的
                    wxChatrooms.sort((soft1, soft2) -> {
                        Document document1 = soft1;
                        Document document2 = soft2;
                        Integer soft1Count = (Integer) document1.get("wxChatroomMsgcount");
                        Integer soft2Count = (Integer) document2.get("wxChatroomMsgcount");
                        return -soft1Count.compareTo(soft2Count);
                    });
                }
                map.put("friend", wxChatrooms);
                break;
            default:
                throw new RuntimeException("对应的关系类型不符合规范！");
        }
        return map;
    }

    @Override
    public List<Document> findImpactHistory(Integer userId, String project, String deviceName, Integer page,
                                            Integer pageSize) {
        // TODO Auto-generated method stub
        if (userId == null) {
            throw new RuntimeException("查询关系碰撞的用户账号为空");
        }
        Document query = new Document("userId", userId);
        if (!StringUtils.isEmpty(project)) {
            Pattern pattern = Pattern.compile("^.*" + project.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("project", pattern);
        }
        List<Pattern> list2 = new ArrayList<>();
        if (!StringUtils.isEmpty(deviceName)) {
            List<Document> list = impactServerDao.findDeviceByName(deviceName);
            list.forEach(t -> {
                String deviceUnique = t.getString("device_unique");
                Pattern pattern = Pattern.compile("^.*" + deviceUnique.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                list2.add(pattern);
            });
            //			//logger.debug("list2:"+list2);
        }
        List<Document> impactHistories = new ArrayList<>();
        List<Object> list = new ArrayList<>();//用户去除重复数据
        if (!CollectionUtils.isEmpty(list2)) {
            list2.forEach(t1 -> {
                query.append("searchNum", t1);
                //				//logger.debug("query:"+query);
                List<Document> impact = impactServerDao.findHistory(query);
                impact.forEach(t2 -> {
                    if (!list.contains(t2.get("unique"))) {
                        list.add(t2.get("unique"));
                        impactHistories.add(t2);
                    }
                });
            });
            logger.debug("测试：" + list);
        } else {
            List<Document> impact = impactServerDao.findHistory(query);
            impact.forEach(t3 -> {
                impactHistories.add(t3);
            });
        }
        //logger.debug("impactHistories:"+impactHistories);
        if (impactHistories != null) {
            impactHistories.forEach(t -> {
                List<Document> devices = new ArrayList<>();
                String searchNums = t.getString("searchNum");
                if (!StringUtils.isEmpty(searchNums)) {
                    String[] searchNumbers = StringUtils.split(searchNums, ",");
                    for (int i = 0; i < searchNumbers.length; i++) {
                        devices.add(impactSimpleDao.deviceFindOne(searchNumbers[i]).get(0));
                    }
                }
                t.append("searchTatil", devices);
            });
        }
        return impactHistories;
    }

    @Override
    public String impactAddDevices(String policeNumber, String deviceUnique, String personName, String idNumber, String departmentName) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        List<String> list = Arrays.asList(deviceUnique.split(","));
        List<Document> devices = impactSimpleDao.deviceFindByDeviceUnique(list);
        logger.debug("devices:" + devices);

        //保存至redis
        String impactKey = RedisUtilNew.IMPACT_DEVICE + policeNumber;
        String policeNumberKey = personName + "(" + idNumber + ")";
        HashMap<String, Object> impact = (HashMap<String, Object>) redisUtilNew.get(impactKey);
        List<Map<String, Object>> impactList = null;
        if (impact != null) {
            impactList = (List<Map<String, Object>>) impact.get(departmentName);
            if (impactList != null) {
                for (Map<String, Object> map : impactList) {
                    if (policeNumberKey.equals(map.get("policeNumberKey"))) {
                        List<Document> deviceList = (List<Document>) map.get("devices");
                        if (!CollectionUtils.isEmpty(deviceList)) {
                            devices.stream().filter(device -> !deviceList.contains(device)).forEach(deviceList::add);
                            return "save redis success!";
                        } else {
                            map.put("devices", devices);
                            return "save redis success!";
                        }
                    }
                }
            } else {
                impactList = new ArrayList<>();
            }
        } else {
            impact = new HashMap<>();
            impactList = new ArrayList<>();
        }
        Map<String, Object> newMap = new HashMap<>();
        newMap.put("policeNumberKey", policeNumberKey);
        newMap.put("devices", devices);
        impactList.add(newMap);

        impact.put(departmentName, impactList);
        redisUtilNew.set(impactKey, impact);
        return "save redis success!";
    }

    @Override
    public List<Object> impactfindDevices(String policeNumber) {
        // TODO Auto-generated method stub
        List<Object> list = new ArrayList<>();
        String impactKey = RedisUtilNew.IMPACT_DEVICE + policeNumber;
        HashMap<String, List<Document>> impactMap = (HashMap<String, List<Document>>) redisUtilNew.get(impactKey);
        if (!CollectionUtils.mapIsEmpty(impactMap)) {
            for (Entry<String, List<Document>> entry : impactMap.entrySet()) {
                Map map = new HashMap<>();
                map.put("key", entry.getKey());
                map.put("value", entry.getValue());
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public String impactdeleteDeviceByName(String policeNumber, String personName, String idNumber, String departmentName) {
        String policeNumberKey = personName + "(" + idNumber + ")";
        String impactKey = RedisUtilNew.IMPACT_DEVICE + policeNumber;
        HashMap<String, Object> impactMap = (HashMap<String, Object>) redisUtilNew.get(impactKey);
        logger.debug("impactMap:" + impactMap);
        List<Map<String, Object>> list = (List<Map<String, Object>>) impactMap.get(departmentName);
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i).get("policeNumberKey");
            if (policeNumberKey.equals(s)) {
                list.remove(i);
            }
        }
        redisUtilNew.set(impactKey, impactMap);
        return "delete success";
    }

    @Override
    public String impactdeleteDevices(String policeNumber) {
        String impactKey = RedisUtilNew.IMPACT_DEVICE + policeNumber;
        redisUtilNew.remove(impactKey);
        return "delete all success";
    }

    @Override
    public List findDevice(String caseuniquemark, String usernumber, String departmentCode) {
        // TODO Auto-generated method stub
        List<Document> persons = new ArrayList<>();
        //查询条件
        BasicDBObject query = new BasicDBObject();
        if (!StringUtils.isEmpty(caseuniquemark)) {
            query.append("caseuniquemark", caseuniquemark);
            //可能有多条
            persons = impactSimpleDao.findPersonByPersonOrCase(query);
            if (!CollectionUtils.isEmpty(persons)) {
                persons.forEach(t -> {
                    List deviceList = (List) t.get("device_unique");
                    List<Object> list = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(deviceList)) {
                        deviceList.forEach(t1 -> {
                            List<Document> device = impactSimpleDao.findDeviceByUnique(new BasicDBObject("device_unique", t1));
                            list.add(device.get(0));
                        });
                    }
                    t.append("deviceDatail", list);
                });
            }
        }
        if (!StringUtils.isEmpty(usernumber)) {
            query.append("usernumber", usernumber);
            //只可能有一条
            persons = impactSimpleDao.findPersonByPersonOrCase(query);
            if (persons != null && !persons.isEmpty()) {
                List<String> deviceList = (List) persons.get(0).get("device_unique");
                if (!CollectionUtils.isEmpty(deviceList)) {
                    List<Document> mapped = deviceList.stream().flatMap(t -> {
                        List<Document> device = impactSimpleDao
                            .findDeviceByUnique(new BasicDBObject("device_unique", t));
//                        List<String> device = impactSimpleDao
//                            .findDeviceByUnique(t);
                        return device.stream();
                    }).collect(Collectors.toList());
                    persons.get(0).append("deviceDatail", mapped);
                }
            }
        }
        return persons;
    }

    @Override
    public List<Document> findDeviceImpactHistory(Integer userId, String project, String personName, Integer page,
                                                  Integer pageSize) {
        // TODO Auto-generated method stub
        if (userId == null) {
            throw new RuntimeException("查询关系碰撞的用户账号为空");
        }
        Document query = new Document("userId", userId);
        if (!StringUtils.isEmpty(project)) {
            Pattern pattern = Pattern.compile("^.*" + project.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("project", pattern);
        }
        List<Pattern> list2 = new ArrayList<>();
        if (!StringUtils.isEmpty(personName)) {
            List<Document> list = impactServerDao.findPersonByName(personName);
            list.forEach(t -> {
                String usernumber = t.getString("usernumber");
                Pattern pattern = Pattern.compile("^.*" + usernumber.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                list2.add(pattern);
            });
        }
        List<Document> impactHistories = new ArrayList<>();
        //用户去除重复数据
        List<Object> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list2)) {
            list2.forEach(t1 -> {
                query.append("searchNum", t1);
                List<Document> impact = impactServerDao.findHistory(query);
                impact.forEach(t2 -> {
                    if (!list.contains(t2.get("unique"))) {
                        list.add(t2.get("unique"));
                        impactHistories.add(t2);
                    }
                });
            });
            logger.debug("测试：" + list);
        } else {
            List<Document> impact = impactServerDao.findHistory(query);
            impact.forEach(t3 -> {
                impactHistories.add(t3);
            });
        }
        if (impactHistories != null) {
            impactHistories.forEach(t -> {
                List<Document> devices = new ArrayList<>();
                String searchNums = t.getString("searchNum");
                if (!StringUtils.isEmpty(searchNums)) {
                    String[] searchNumbers = StringUtils.split(searchNums, ",");
                    for (int i = 0; i < searchNumbers.length; i++) {
                        devices.add(impactSimpleDao.deviceFindOne(searchNumbers[i]).get(0));
                    }
                }
                t.append("searchTatil", devices);
            });
        }
        return impactHistories;
    }

    @Override
    public Long insertImpactHistoryNew(Integer userId, String type, List<String> idNumber, String time, String project,
                                       String explain) {
        if (userId == null) {
            throw new RuntimeException("对应的用户id为空");
        }
        if (CollectionUtils.isEmpty(idNumber)) {
            throw new RuntimeException("碰撞设备为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("碰撞类型为空");
        }
        //存储身份证号
        List<Object> userList = new ArrayList<>();
        //存储设备唯一标识
        List<Object> deviceList = new ArrayList<>();
        for (String s : idNumber) {
            int i = s.lastIndexOf(",");
            deviceList.add(s.substring(0, i));
            // 最后一个是身份证号码+IDnumber
            userList.add(s.substring(i + 1, s.length() - 8));
        }

        Document query = new Document();
        query.append("userId", userId);
        query.append("searchNum", userList);
        query.append("deviceUnique", deviceList);
        query.append("type", type);
        List<Document> history = impactServerDao.findHistory(query);
        if (!CollectionUtils.isEmpty(history)) {
            throw new RuntimeException("已存在");
        }
        Date date = new Date();
        if (!StringUtils.isEmpty(time)) {
            date = TimeUtils.parseStrToDate2(time);
        }
        Document insertDoc = new Document();
        insertDoc.append("userId", userId);
        insertDoc.append("type", type.trim());
        insertDoc.append("searchNum", userList);
        insertDoc.append("time", date);
        insertDoc.append("project", project);
        insertDoc.append("explain", explain);
        insertDoc.append("deviceUnique", deviceList);
        insertDoc.append("unique", MD5Util.MD5(userList + time));
        return impactServerDao.insertHistory(insertDoc);
    }

    @Override
    public List<Document> findImpactHistoryNew(Integer userId, String project, String personName, Integer page,
                                               Integer pageSize) {
        if (userId == null) {
            throw new RuntimeException("查询关系碰撞的用户账号为空");
        }
        Document query = new Document("userId", userId);
        if (!StringUtils.isEmpty(project)) {
            Pattern pattern = Pattern.compile("^.*" + project.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("project", pattern);
        }
        //存储模糊匹配返回的证件号
        List<Object> uList = new ArrayList<>();
        //存储返回的历史碰撞业务
        List<Document> impactHistories = new ArrayList<>();
        //用户去除重复数据
        List<Object> list = new ArrayList<>();
        if (!StringUtils.isEmpty(personName)) {
            //模糊匹配人员
            List<Document> user = impactServerDao.findPersonByName(personName);
            user.forEach(t -> {
                String usernumber = t.getString("usernumber");
                uList.add(usernumber);
            });
            //姓名字段不为空且匹配到具体的人
            if (!CollectionUtils.isEmpty(uList)) {
                uList.forEach(t1 -> {
                    query.append("searchNum", t1);
                    List<Document> impact = impactServerDao.findHistory(query);
                    impact.forEach(t2 -> {
                        if (!list.contains(t2.get("unique"))) {
                            list.add(t2.get("unique"));
                            impactHistories.add(t2);
                        }
                    });
                });
            } else {
                return impactHistories;
            }
        } else {
            logger.debug("2");
            List<Document> impact = impactServerDao.findHistory(query);
            impact.forEach(t3 -> {
                impactHistories.add(t3);
            });
        }
        //获取searchNum字段的详细信息，便于前端显示
        logger.debug("impactHistories:" + impactHistories);
        impactHistories.forEach(t -> {
            Document qDocument = new Document();
            qDocument.append("unique", t.get("unique").toString());
            List<String> searchNum = (List<String>) t.get("searchNum");
            Object[] searchNums = searchNum.toArray();
            //存储返回的人员信息
            List<Document> persons = new ArrayList<>();
            if (searchNums.length > 0) {
                for (int i = 0; i < searchNums.length; i++) {
                    List<Document> person = impactServerDao.findPersonByIDnumber(searchNums[i].toString());
                    if (CollectionUtils.isEmpty(person)) {
                        continue;
                    }
                    persons.add(person.get(0));
                    //查询符合条件的所有业务
                    List<Document> device = impactServerDao.findDevice(qDocument);
                    if (CollectionUtils.isEmpty(device)) {
                        continue;
                    }
                    List<Object> deviceUniques = (List<Object>) device.get(0).get("deviceUnique");
                    //i对应具体的人
                    String dString = deviceUniques.get(i).toString();
                    String[] deviceUnique = StringUtils.split(dString, ",");
                    List<Document> deviceNames = new ArrayList<>();
                    for (int j = 0; j < deviceUnique.length; j++) {
                        BasicDBObject device_unique = new BasicDBObject("device_unique", deviceUnique[j]);
                        List<Document> deviceFindOne = impactSimpleDao.findDeviceByUnique(device_unique);
                        if (CollectionUtils.isEmpty(deviceFindOne)) {
                            continue;
                        }
                        Document document = deviceFindOne.get(0);
                        //获取设备信息
                        deviceNames.add(document);
                    }
                    person.get(0).append("deviceDatail", deviceNames);
                }
                //加入人员信息以及人员对应的设备信息
                t.append("searchTatil", persons);
            }
        });
        return impactHistories;
    }

    @Override
    public String impactAddDevicesByCase(String policeNumber, String caseuniquemark, String departmentName) {
        // TODO Auto-generated method stub
        if (StringUtils.isEmpty(caseuniquemark)) {
            throw new RuntimeException("案件唯一标识为空");
        }
        //保存至redis//查询案件信息
        List<Document> cases = impactSimpleDao.finCase(caseuniquemark);
        //查询案件下面的人员信息
        List<String> usernumber = (List<String>) cases.get(0).get("usernumber");
        //查询案件下面的设备信息
        List<String> deviceSplit = (List<String>) cases.get(0).get("device_unique");
        //封装设备唯一标识信息
        List<String> deviceUniques = new ArrayList<>();
        for (int i = 0; i < usernumber.size(); i++) {
            //查询人员信息
            List<Document> persons = impactSimpleDao.finPerson(usernumber.get(i));
            System.out.println("persons:" + persons);
            //查询人员下面的设备信息
            List<String> deviceUniqueNew = (List<String>) persons.get(0).get("device_unique");
            //和案件相关的设备才加入“购物车”
            deviceUniqueNew.forEach(t1 -> {
                if (deviceSplit.contains(t1)) {
                    deviceUniques.add(t1);
                }
            });
            //查询设备详情
            List<Document> devices = impactSimpleDao.deviceFindByDeviceUnique(deviceUniques);
            deviceUniques.clear();
            String impactKey = RedisUtilNew.IMPACT_DEVICE + policeNumber;
            String policeNumberKey = persons.get(0).get("personname") + "(" + usernumber.get(i) + ")";
            HashMap<String, Object> impact = (HashMap<String, Object>) redisUtilNew.get(impactKey);
            List<Map<String, Object>> impactList = null;
            if (impact != null) {
                impactList = (List<Map<String, Object>>) impact.get(departmentName);
                if (impactList != null) {
                    for (Map<String, Object> map : impactList) {
                        if (policeNumberKey.equals(map.get("policeNumberKey"))) {
                            List<Document> deviceList = (List<Document>) map.get("devices");
                            if (!CollectionUtils.isEmpty(deviceList)) {
                                devices.stream().filter(device -> !deviceList.contains(device)).forEach(deviceList::add);
                                return "save redis success!";
                            } else {
                                map.put("devices", devices);
                                return "save redis success!";
                            }
                        }
                    }
                } else {
                    impactList = new ArrayList<>();
                }
            } else {
                impact = new HashMap<>();
                impactList = new ArrayList<>();
            }
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("policeNumberKey", policeNumberKey);
            newMap.put("devices", devices);
            impactList.add(newMap);

            impact.put(departmentName, impactList);
            redisUtilNew.set(impactKey, impact);
        }
        return "save redis success!";
    }

}
