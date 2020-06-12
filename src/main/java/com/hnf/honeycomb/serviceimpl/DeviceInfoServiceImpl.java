package com.hnf.honeycomb.serviceimpl;

import com.hnf.crypte.Utils;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.service.DeviceInfoService;
import com.hnf.honeycomb.util.*;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.bson.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.util.ObjectUtil.*;

/**
 * @author hnf
 */
@Service("deviceInfoService")
public class DeviceInfoServiceImpl implements DeviceInfoService {

    @Resource
    private DeviceMongoDao deviceMongoDao;

    /**
     * 案件唯一标识查询人员
     */
    @Override
    public List<Document> findRelationPersonsByDeviceUnique(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        BasicDBObject query = new BasicDBObject("device_unique", deviceUnique.trim());
        return deviceMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_person", query, null, null, null);
    }

    /**
     * 设备
     */
    @Override
    public List<Document> findRelationCasesByDeviceUnique(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        BasicDBObject query = new BasicDBObject("device_unique", deviceUnique.trim());
        List<Document> devices = deviceMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_device", query, null, null, null);
        List list = devices.get(0).get("caseuniquemark", List.class);
        if(list == null){
            return null;
        }
        String caseUnique = list.get(0).toString();
        BasicDBObject caseQuery = new BasicDBObject("caseuniquemark", caseUnique);
        return deviceMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_case", caseQuery, null, null, null);
    }

    /**
     * 案件唯一标识查询案件详情
     */
    @Override
    public List<Document> findDeviceInfoByDeviceUnique(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        BasicDBObject query = new BasicDBObject("device_unique", deviceUnique.trim());
        return deviceMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_device", query, null, null, null);
    }

    /**
     * 查询设备的通讯录详情
     */
    @Override
    public Object findContactInfoByDeviceUnique(String deviceUnique, Integer page) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为空");
        }
        if (page == null) {
            throw new RuntimeException("页码数为空");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique", deviceUnique.trim());
        Long count = deviceMongoDao.countByGatherNameAndDBNameAndQuery("infoData2",
                "t_contact_phonenum", query);
        List<Document> contacts = deviceMongoDao.findInfoByGatherNameAndQuery("infoData2",
                "t_contact_phonenum", query, null, page, 1000);
        return BuilderMap.of(String.class, Object.class)
                .put("totalPage", Math.ceil(count / 1000d))
                .put("count", count)
                .put("data", contacts).get();
    }

    /**
     * 查询短消息联系次数
     */
    @Override
    public Object findMsgCountByDeviceUnique(String deviceUnique, Long startTime, Long endTime, Integer timeSelectType,
                                             String searchContent, String startDate, String endDate) {
        if (deviceUnique == null || deviceUnique.trim().isEmpty()) {
            throw new RuntimeException("传入的设备unique为空");
        }
        deviceUnique = deviceUnique.trim();
        Map<String, Object> map = new HashMap<>(4);
        BasicDBObject msgQuery = new BasicDBObject("deviceUnique", deviceUnique);
        BasicDBObject msgGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum")
                .append("count", new BasicDBObject("$sum", 1))
        );
        BasicDBObject msgSort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            msgQuery.append("time", timeQuery);
        }
        // 内容过滤
        contentFilter(searchContent, msgQuery, "content");
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            msgQuery.append("timeNode", tQuery);
        }
        System.out.println("tQuery:" + tQuery);
        List<Document> msgGroupResult = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                "infoData", "message", Arrays.asList(new BasicDBObject("$match", msgQuery), msgGroup, msgSort));
        // 统计前端需要的总数
        Integer msgCount = 0;
        List<String> phones = new ArrayList<>();
        // 对应的聚合结果不为空
        if (!CollectionUtils.isEmpty(msgGroupResult)) {
            for (Document doc : msgGroupResult) {
                msgCount += doc.getInteger("count");
                phones.add(doc.getString("_id"));
            }
        }
        BasicDBObject contactQuery = new BasicDBObject();
        contactQuery.append("device_unique", deviceUnique);
        contactQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, phones.toArray(new String[]{})));
        List<Document> contactPhones = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_contact_phonenum", contactQuery);
        Map<String, String> phone2ContactPersonName = new HashMap<>(2);
        if (!CollectionUtils.isEmpty(contactPhones)) {
            contactPhones.forEach(t -> {
                String phone = t.getString("phonenum");
                String personName = t.getString("personname");
                phone2ContactPersonName.put(phone, personName);
            });
        }
        msgGroupResult.forEach(t -> {
            String onePhoneNum = phone2ContactPersonName.get(t.getString("_id"));
            onePhoneNum = onePhoneNum != null ? onePhoneNum : "";
            t.append("personname", onePhoneNum);
        });
        // 短消息页面数据中加入机主姓名和机主手机号
        List<Document> queryPerson = deviceMongoDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_person",
                new BasicDBObject("device_unique", deviceUnique)
                , null, null, null);
        if (queryPerson.size() != 0) {
            for (Document document : queryPerson) {
                map.put("phoneMasterName", document.get("personname"));
                map.put("phoneMasterNumber", document.get("phone"));
            }
        } else {
            System.out.println("queryPerson没数据");
        }
        if (msgGroupResult.isEmpty()) {
            Document data = new Document();
            data.append("id", "").append("count", 0).append("personname", "");
            msgGroupResult.add(data);
        }
        map.put("list", msgGroupResult);
        map.put("msgCount", msgCount);
        return map;
    }

    private void contentFilter(String searchContent, BasicDBObject msgQuery, String content) {
        if (!StringUtils.isEmpty(searchContent)) {
            String searchContents = WordUtil.seg(searchContent, SegmentationAlgorithm.MaximumMatching);
            String[] searches = searchContents.split(" ");
            BasicDBList queryList = new BasicDBList();
            for (String search : searches) {
                BasicDBObject qObject = new BasicDBObject();
                Pattern pattern = Pattern.compile("^.*" + search.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                qObject.append(content, pattern);
                queryList.add(qObject);
            }
            msgQuery.append("$or", queryList);
        }
    }

    /**
     * 统计设备下的所有通讯录总数
     */
    @Override
    public Object findRecordCallByDeviceUnique(String deviceUnique, Long startTime, Long endTime, Integer timeSelectType,
                                               String startDate, String endDate) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("设备唯一标识为NULL");
        }
        deviceUnique = deviceUnique.trim();
        BasicDBObject msgQuery = new BasicDBObject("deviceUnique", deviceUnique);
        BasicDBObject msgGroup = new BasicDBObject("$group", new BasicDBObject("_id", "$phonenum")
                .append("count", new BasicDBObject("$sum", 1))
        );
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            msgQuery.append("time", timeQuery);
        }
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            msgQuery.append("timeNode", tQuery);
        }
        List<Document> msgGroupResult = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                "infoData", "record", Arrays.asList(new BasicDBObject("$match", msgQuery), msgGroup, sort));
        List<String> phones = new ArrayList<>();
        // 对应的聚合结果不为空
        if (!CollectionUtils.isEmpty(msgGroupResult)) {
            for (Document doc : msgGroupResult) {
                phones.add(doc.getString("_id"));
            }
        }
        BasicDBObject contactQuery = new BasicDBObject();
        contactQuery.append("device_unique", deviceUnique);
        contactQuery.append("phonenum", new BasicDBObject(QueryOperators.IN, phones.toArray(new String[]{})));
        List<Document> contactPhones = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_contact_phonenum", contactQuery);
        Map<String, String> phone2ContactPersonName = new HashMap<>(2);
        if (!CollectionUtils.isEmpty(contactPhones)) {
            contactPhones.forEach(t -> {
                String phone = t.getString("phonenum");
                String personName = t.getString("personname");
                phone2ContactPersonName.put(phone, personName);
            });
        }
        msgGroupResult.forEach(t -> {
            String onePhoneNum = phone2ContactPersonName.get(t.getString("_id"));
            onePhoneNum = onePhoneNum != null ? onePhoneNum : "";
            t.append("personname", onePhoneNum);
        });
        return msgGroupResult;
    }

    /**
     * 查询通讯录详情
     */
    @Override
    public List<Document> findContactDetailByDeviceUniqueAndPhone(String deviceUnqiue, String phone) {
        if (StringUtils.isEmpty(deviceUnqiue)) {
            throw new RuntimeException("设备唯一标识为NULL");
        }
        if (StringUtils.isEmpty(phone)) {
            throw new RuntimeException("电话号码为NULL");
        }
        BasicDBObject query = new BasicDBObject("device_unique", deviceUnqiue.trim());
        query.append("phonenum", phone.trim());
        return deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_contact_phonenum", query);

    }

    /**
     * 查询对应人之间的短消息详情
     */
    @Override
    public List<Document> findOne2OneMsgByDeviceUniqueAndPhone(String deviceUnique, String phone, Integer page,
                                                               Integer pageSize, Long startTime, Long endTime,
                                                               Integer timeSelectType, String searchContent,
                                                               String startDate, String endDate) {
        if (page == null || pageSize == null) {
            throw new RuntimeException("分页条件有误");
        }
        deviceUnique = deviceUnique.trim();
        phone = phone.trim();
        BasicDBObject query = new BasicDBObject();
        query.append("deviceUnique", deviceUnique);
        if (!StringUtils.isEmpty(phone)) {
            query.append("phonenum", phone);
        }
        BasicDBObject sort = new BasicDBObject("time", 1);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        contentFilter(searchContent, query, "content");
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        return deviceMongoDao.findInfoByGatherNameAndQuery("infoData", "message", query, sort, page, pageSize);
    }

    /**
     * 查询通话记录详情
     */
    @Override
    public List<Document> findOne2OneRecordByDeviceUniqueAndPhone(String deviceUnique, String phone, Integer page,
                                                                  Integer pageSize, Long startTime, Long endTime,
                                                                  Integer timeSelectType, String startDate, String endDate) {
        deviceUnique = deviceUnique.trim();
        phone = phone.trim();
        BasicDBObject query = new BasicDBObject();
        query.append("deviceUnique", deviceUnique);
        query.append("phonenum", phone);
        BasicDBObject sort = new BasicDBObject("time", -1);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        return deviceMongoDao.findInfoByGatherNameAndQuery
                ("infoData", "record", query, sort, null, null);
    }

    @Override
    public Map<String, Object> findQQUserDetailByQQUin(String qqUin, Long startTime, Long endTime, Integer timeSelectType,
                                                       String searchContent, String startDate, String endDate) {
        if (StringUtils.isEmpty(qqUin)) {
            throw new RuntimeException("qq账号为NULL");
        }
        Map<String, Object> result = new HashMap<>(3);
        List<Document> qqUserInfo = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser", new BasicDBObject("uin", qqUin));
        qqUserInfo.forEach(doc -> {
            if (!doc.containsKey("nickname")) {
                doc.append("nickname", null);
            }
        });
        result.put("qquser", qqUserInfo.get(0));
        // 查询好友详情
        BasicDBObject querySearch = new BasicDBObject();
        querySearch.append("uin", qqUin);
        List<Document> qqUserFriends = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser_friend", querySearch);
        List<String> c2cs = new ArrayList<>();
        List<String> qqUins = new ArrayList<>();
        Map<String, String> uin2C2c = new HashMap<>(30);
        List<Document> qqUserFriend = new ArrayList<>();
        Map<String, String> uinToRemark = new HashMap<>(30);
        if (!CollectionUtils.isEmpty(qqUserFriends)) {
            qqUserFriends.forEach(doc -> {
                String fuin = getString(doc.get("fuin"));
                String c2cmark = Utils.NumberStringUniqueMD5(getStringForLong(qqUin), getStringForLong(fuin));
                qqUins.add(fuin);
                c2cs.add(c2cmark);
                uin2C2c.put(fuin, c2cmark);
                uinToRemark.put(fuin, doc.getString("friendremarkname"));
            });

            BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmsg_mark")
                    .append("count", new BasicDBObject("$sum", 1))
            );
            BasicDBObject query = new BasicDBObject("c2cmsg_mark",
                    new BasicDBObject(QueryOperators.IN, c2cs.toArray(new String[]{})));
            BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                    //时间筛选条件
                    timeSelectType, startTime, endTime);
            if (!timeQuery.isEmpty()) {
                query.append("msgtime", timeQuery);
            }
            contentFilter(searchContent, query, "msgdata");
            // 时间段筛选处理
            BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
            if (!tQuery.isEmpty()) {
                query.append("timeNode", tQuery);
            }
            List<Document> qqMsgGroupResult = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                    "infoData", "qqmsg", Arrays.asList(new BasicDBObject("$match", query), group));
            Map<String, Integer> c2cToCount = new HashMap<>(50);
            if (!CollectionUtils.isEmpty(qqMsgGroupResult)) {
                qqMsgGroupResult.forEach(t -> c2cToCount.put(t.getString("_id"), t.getInteger("count")));
            }
            BasicDBObject qqFriendQuery = new BasicDBObject("uin",
                    new BasicDBObject(QueryOperators.IN, qqUins.toArray(new String[]{})));
            List<Document> qqFriends = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                    "infoData2", "t_qquser", qqFriendQuery);
            Map<String, String> uniToNick = new HashMap<>(50);
            if (!CollectionUtils.isEmpty(qqFriends)) {
                qqFriends.forEach(t -> uniToNick.put(t.getString("uin"), t.getString("nickname")));
            }

            for (String qq : qqUins) {
                Document doc = new Document();
                doc.append("fuin", qq);
                doc.append("friendremarkname", uinToRemark.get(qq) != null ? uinToRemark.get(qq) : "");
                doc.append("nickname", uniToNick.get(qq) != null ? uniToNick.get(qq) : "");
                doc.append("qqmsgcount", c2cToCount.get(uin2C2c.get(qq)) != null ? c2cToCount.get(uin2C2c.get(qq)) : 0);
                qqUserFriend.add(doc);
            }
            // 排序------java8自带的
            sort(qqUserFriend, "qqmsgcount");
        }
        System.out.println("qqUserFriend:" + qqUserFriend);
        result.put("qquser_friend", qqUserFriend);
        // 查询群详情
        BasicDBObject qqsQuery = new BasicDBObject();
        qqsQuery.append("uin", qqUin);
        List<Document> qqTroopUsers = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_troop_qquser", qqsQuery);
        List<String> troopUins = new LinkedList<>();
        List<Document> troops = new LinkedList<>();
        Map<String, Integer> troopUin2Count = new HashMap<>(50);
        Map<String, String> troopUin2TroopName = new HashMap<>(50);
        if (!CollectionUtils.isEmpty(qqTroopUsers)) {
            qqTroopUsers.forEach(doc1 -> troopUins.add(doc1.getString("troopuin")));
            BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$troopuin")
                    .append("count", new BasicDBObject("$sum", 1))
            );
            BasicDBObject query = new BasicDBObject().append("troopuin",
                    new BasicDBObject(QueryOperators.IN, troopUins.toArray(new String[]{})));
            BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                    //时间筛选条件
                    timeSelectType, startTime, endTime);
            if (!timeQuery.isEmpty()) {
                query.append("msgtime", timeQuery);
            }
            contentFilter(searchContent, query, "msgdata");
            // 时间段筛选处理
            BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
            if (!tQuery.isEmpty()) {
                query.append("timeNode", tQuery);
            }
            List<Document> troopMsgGroupResult = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                    "infoData", "qqTroopMsg", Arrays.asList(new BasicDBObject("$match", query), group));
            if (!CollectionUtils.isEmpty(troopMsgGroupResult)) {
                troopMsgGroupResult.forEach(t -> troopUin2Count.put(t.getString("_id"), t.getInteger("count")));
            }
            BasicDBObject troopQuery;
            troopQuery = new BasicDBObject("troopuin", new BasicDBObject(QueryOperators.IN, troopUins.toArray(new String[]{})));
            BasicDBObject fileds = new BasicDBObject("troopuin", 1).append("troopname", 1).append("_id", 0);
            List<Document> troopInfos = deviceMongoDao.findSomeFiledsByGatherNameAndQuery("infoData2", "t_qq_troop", troopQuery, fileds);
            if (!CollectionUtils.isEmpty(troopInfos)) {
                troopInfos.forEach(t -> troopUin2TroopName.put(t.getString("troopuin"), t.getString("troopname")));
            }
            for (String troopUin : troopUins) {
                Document doc = new Document();
                doc.append("troopuin", troopUin);
                doc.append("qqTroopMsgcount", troopUin2Count.get(troopUin) != null ? troopUin2Count.get(troopUin) : 0);
                doc.append("troopname", troopUin2TroopName.get(troopUin) != null ? troopUin2TroopName.get(troopUin) : "");
                troops.add(doc);
            }
            sort(troops, "qqTroopMsgcount");
        }
        result.put("troop_qquser", troops);
        return result;
    }

    /**
     * 查询wx账号详细信息
     */
    @Override
    public Map<String, Object> findWXUserDetailByWXUin(String wxUin, Long startTime, Long endTime, Integer timeSelectType,
                                                       String searchContent, String startDate, String endDate) {
        if (StringUtils.isEmpty(wxUin)) {
            throw new RuntimeException("对应的wx账号为NULL");
        }
        Map<String, Object> map = new HashMap<>(4);
        List<Document> findWx = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxuser", new BasicDBObject("username", wxUin));
        if (findWx.size() > 0) {
            map.put("findwx", findWx.get(0));
        } else {
            map.put("findwx", new ArrayList<Document>());
        }
        BasicDBObject querySearch = new BasicDBObject();
        querySearch.append("username", wxUin);
        List<Document> wxUserFriends = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxuser_friend", querySearch);
        List<String> c2cMarks = new ArrayList<>();
        List<String> wxUins = new ArrayList<>();
        Map<String, String> uin2C2c = new HashMap<>(30);
        List<Document> wxUserFriend = new ArrayList<>();
        Map<String, String> uinToRemark = new HashMap<>(30);
        if (!CollectionUtils.isEmpty(wxUserFriends)) {
            wxUserFriends.forEach(doc -> {
                String c2cmark = Utils.StringUniqueMD5(wxUin, doc.get("fusername").toString());
                c2cMarks.add(c2cmark);
                wxUins.add(doc.getString("fusername"));
                uin2C2c.put(doc.getString("fusername"), c2cmark);
                uinToRemark.put(doc.getString("fusername"), doc.getString("friendremarkname"));
            });
            Map<String, Integer> c2cToCount = new HashMap<>();
            Map<String, String> uniToNick = new HashMap<>();
            BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmark")
                    .append("count", new BasicDBObject("$sum", 1))
            );
            BasicDBObject query = new BasicDBObject().append("c2cmark",
                    new BasicDBObject(QueryOperators.IN, c2cMarks.toArray(new String[]{})));
            BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                    //时间筛选条件
                    timeSelectType, startTime, endTime);
            if (!timeQuery.isEmpty()) {
                query.append("msgtime", timeQuery);
            }
            contentFilter(searchContent, query, "msgdata");
            //时间段筛选处理
            BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
            if (!tQuery.isEmpty()) {
                query.append("timeNode", tQuery);
            }
            List<Document> wxFriendMsgCounts = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                    "infoData", "wxmsg", Arrays.asList(new BasicDBObject("$match", query), group));
            if (!CollectionUtils.isEmpty(wxFriendMsgCounts)) {
                wxFriendMsgCounts.forEach(t -> c2cToCount.put(t.getString("_id"), t.getInteger("count")));
            }
            BasicDBObject nickQuery;
            nickQuery = new BasicDBObject("username",
                    new BasicDBObject(QueryOperators.IN, wxUins.toArray(new String[]{})));
            BasicDBObject fileds = new BasicDBObject("username", 1).append("nickname", 1).append("_id", 0);
            List<Document> wxFriends = deviceMongoDao.findSomeFiledsByGatherNameAndQuery(
                    "infoData2", "t_wxuser", nickQuery, fileds);
            if (!CollectionUtils.isEmpty(wxFriends)) {
                wxFriends.forEach(t -> uniToNick.put(t.getString("username"), t.getString("nickname")));
            }
            for (String wx : wxUins) {
                Document doc = new Document();
                doc.append("fusername", wx);
                doc.append("friendremarkname", getString(uinToRemark.get(wx), ""));
                doc.append("nickname", getString(uniToNick.get(wx), ""));
                Integer msgCount = getInteger(c2cToCount.get(uin2C2c.get(wx)), 0);
                if (msgCount > 0) {
                    doc.append("wxmsgcount", msgCount);
                    wxUserFriend.add(doc);
                }
            }
            // 排序------java8自带的
            sort(wxUserFriend, "wxmsgcount");
        }
        map.put("wxuser_friend", wxUserFriend);

        // 查询群
        BasicDBObject groupQuery = new BasicDBObject();
        groupQuery.append("username", wxUin);
        List<Document> chatroomWxUsers = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_chatroom_wxuser", groupQuery);
        List<String> wxChatroomUins = new ArrayList<>();
        List<Document> wxChatrooms = new LinkedList<>();
        if (!CollectionUtils.isEmpty(chatroomWxUsers)) {
            chatroomWxUsers.forEach(doc -> wxChatroomUins.add(doc.get("chatroomname").toString()));
            Map<String, Integer> chatroomUin2MsgCount = new HashMap<>(16);
            Map<String, String> chatroonUin2troopName = new HashMap<>(16);
            BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$chatroomname")
                    .append("count", new BasicDBObject("$sum", 1))
            );
            BasicDBObject query = new BasicDBObject().append("chatroomname",
                    new BasicDBObject(QueryOperators.IN, wxChatroomUins.toArray(new String[]{})));
            BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                    // 时间筛选条件
                    timeSelectType, startTime, endTime);
            if (!timeQuery.isEmpty()) {
                query.append("msgtime", timeQuery);
            }
            contentFilter(searchContent, query, "msgname");
            // 时间段筛选处理
            BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
            if (!tQuery.isEmpty()) {
                query.append("timeNode", tQuery);
            }
            List<Document> wxRoomMsgGroupResult = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                    "infoData", "wxChatroomMsg",
                    Arrays.asList(new BasicDBObject("$match", query), group));
            if (!CollectionUtils.isEmpty(wxRoomMsgGroupResult)) {
                wxRoomMsgGroupResult.forEach(t -> chatroomUin2MsgCount.put(
                        t.getString("_id"), t.getInteger("count"))
                );
            }
            BasicDBObject wxUserQuery;
            wxUserQuery = new BasicDBObject(
                    "chatroomname", new BasicDBObject(QueryOperators.IN, wxChatroomUins.toArray(new String[]{})));
            BasicDBObject fileds = new BasicDBObject("chatroomname", 1)
                    .append("chatroomnickname", 1)
                    .append("_id", 0);
            List<Document> wxFriendUsers = deviceMongoDao.findSomeFiledsByGatherNameAndQuery(
                    "infoData2", "t_wxchatroom", wxUserQuery, fileds);
            if (!CollectionUtils.isEmpty(wxFriendUsers)) {
                wxFriendUsers.forEach(t -> chatroonUin2troopName.put(t.getString("chatroomname"),
                        t.getString("chatroomnickname")));
            }
            for (String chatroomUin : wxChatroomUins) {
                Document doc = new Document();
                doc.append("chatroomname", chatroomUin);
                doc.append("chatroomnickname", getString(chatroonUin2troopName.get(chatroomUin), ""));
                Integer msgCount = getInteger(chatroomUin2MsgCount.get(chatroomUin), 0);
                if (msgCount > 0) {
                    doc.append("wxChatroomMsgcount", msgCount);
                    wxChatrooms.add(doc);
                }
            }
            sort(wxChatrooms, "wxChatroomMsgcount");
        }
        map.put("chatroom_wxuser", wxChatrooms);
        return map;
    }

    private void sort(List<Document> sortList, String field) {
        // 排序------java8自带的
        sortList.sort((soft1, soft2) -> {
            Integer soft1Count = (Integer) soft1.get(field);
            Integer soft2Count = (Integer) soft2.get(field);
            return -soft1Count.compareTo(soft2Count);
        });
    }

    // 查询好友之间的聊天xinxi
    @Override
    public Map<String, Object> findOne2OneWXFriendMsgByTwoWXUserName(String selfWxUserName, String friendWxUserName,
                                                                     Integer page, Integer pageSize, Long startTime,
                                                                     Long endTime, Integer timeSelectType,
                                                                     String searchContent,
                                                                     String startDate, String endDate) {
        if (StringUtils.isEmpty(selfWxUserName)) {
            throw new RuntimeException("自己的WX账号为NULL");
        }
        if (StringUtils.isEmpty(friendWxUserName)) {
            throw new RuntimeException("自己的WX账号为NULL");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("分页条件有误");
        }
        selfWxUserName = selfWxUserName.trim();
        friendWxUserName = friendWxUserName.trim();
        Map<String, Object> wxMessageMap = new HashMap<>(5);
        String c2cmark = Utils.StringUniqueMD5(selfWxUserName, friendWxUserName);
        // 搜索条件
        BasicDBObject query = new BasicDBObject();
        query.append("c2cmark", c2cmark);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        contentFilter(searchContent, query, "msgdata");
//        if (!StringUtils.isEmpty(searchContent)) {
//            String search = WordUtil.seg(searchContent, SegmentationAlgorithm.MaximumMatching);
//            String[] searchs = search.split(" ");
//            BasicDBList queryList = new BasicDBList();
//            for (int i = 0; i < searchs.length; i++) {
//                BasicDBObject qObject = new BasicDBObject();
//                Pattern pattern = Pattern.compile("^.*" + searchs[i].trim() + ".*$", Pattern.CASE_INSENSITIVE);
//                qObject.append("msgdata", pattern);
//                queryList.add(qObject);
//            }
//            query.append("$or", queryList);
//        }
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        List<Document> wxmsg = deviceMongoDao.findInfoByGatherNameAndQuery(
                "infoData",
                "wxmsg",
                query,
                new BasicDBObject("msgtime", 1),
                page,
                pageSize);
        // 找到当前微信朋友的名字
        List<Document> friend = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2",
                "t_wxuser_friend",
                new BasicDBObject("username", selfWxUserName).append("fusername", friendWxUserName));
        // 抽取聊天中的 微信名 nickname 微信用户的自命名
        List<Document> findwxName1 = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2",
                "t_wxuser",
                new BasicDBObject("username", friendWxUserName));

        // 聊天框上面的微信朋友信息栏--账号
        wxMessageMap.put("friendUser", friendWxUserName);
        String friendName = "";
        String nickName;
        if (friend != null && friend.size() != 0 && findwxName1 != null && findwxName1.size() != 0) {
            Object markName = friend.get(0).get("friendremarkname");
            Object nickNameObj = findwxName1.get(0).get("nickname");
            friendName = markName != null ? markName.toString() : "";
            nickName = nickNameObj != null ? nickNameObj.toString() : "";
            // 聊天框上面的微信朋友信息栏--昵称
            wxMessageMap.put("friendName", nickName);
            // 聊天框上面的微信朋友信息栏--备注
            wxMessageMap.put("friendRemakName", friendName);
            // 如果没有备注名就用好友自身的名字
            if ("".equals(friendName) || friendName.trim().isEmpty()) {
                friendName = nickName;
            }
        }

        List<Document> findwx = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2",
                "t_wxuser",
                new BasicDBObject("username", selfWxUserName));

        String wxName = "";
        if (findwx != null) {
            wxName = findwx.get(0).get("nickname").toString();
        }
        // 聊天信息中的WX好友信息及WX用户信息
        if (wxmsg != null) {
            for (Document doc1 : wxmsg) {
                // 找到当前微信的名字

                doc1.append("wxName", wxName);

                doc1.append("wxFriendName", friendName);
            }
        }
        wxMessageMap.put("wxmsg", wxmsg);

        // 聊天分页
        int wxmsgCount = 0;
        Long countWXMsg = deviceMongoDao.countByGatherNameAndDBNameAndQuery(
                "infoData", "wxmsg", new BasicDBObject("c2cmark", c2cmark));
        if (countWXMsg != null) {
            wxmsgCount = (int) (countWXMsg / pageSize + 1);
        }
        wxMessageMap.put("wxmsgCount", wxmsgCount);
        return wxMessageMap;
    }

    @Override
    public Map<String, Object> findOne2OneQQFriendMsgByTwoQQUin(String selfQqUin, String qqFriendUin, Integer page,
                                                                Integer pageSize, Long startTime, Long endTime, Integer timeSelectType, String searchContent, String startDate, String endDate) {
        if (StringUtils.isEmpty(selfQqUin) || StringUtils.isEmpty(qqFriendUin)) {
            throw new RuntimeException("账号为空");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("分页条件有误");
        }
        selfQqUin = selfQqUin.trim();
        qqFriendUin = qqFriendUin.trim();
        Map<String, Object> messageMap = new HashMap<>(5);
        String c2cmark = Utils.NumberStringUniqueMD5(getStringForLong(selfQqUin), getStringForLong(qqFriendUin));
        // 搜索条件
        BasicDBObject query = new BasicDBObject();
        query.append("c2cmsg_mark", c2cmark);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        contentFilter(searchContent, query, "msgdata");
//        if (!StringUtils.isEmpty(searchContent)) {
//            String search = WordUtil.seg(searchContent, SegmentationAlgorithm.MaximumMatching);
//            String[] searchs = search.split(" ");
//            BasicDBList queryList = new BasicDBList();
//            for (int i = 0; i < searchs.length; i++) {
//                BasicDBObject qObject = new BasicDBObject();
//                Pattern pattern = Pattern.compile("^.*" + searchs[i].trim() + ".*$", Pattern.CASE_INSENSITIVE);
//                qObject.append("msgdata", pattern);
//                queryList.add(qObject);
//            }
//            query.append("$or", queryList);
//        }
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        List<Document> qqmsg = deviceMongoDao.findInfoByGatherNameAndQuery(
                "infoData", "qqmsg",
                query,
                new BasicDBObject("msgtime", 1),
                page, pageSize);
        List<Document> friendqq1 = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser", new BasicDBObject("uin", qqFriendUin));
        // 聊天框上面  昵称  账号  备注  保存的集合 qquserFriend qqUserUin, qqFriendUin
        List<Document> qqUserFriend = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser_friend",
                new BasicDBObject("uin", selfQqUin).append("fuin", qqFriendUin));
        messageMap.put("qqFriendUin", qqFriendUin);
        String nickName = "";
        String friendRemakName;
        if (friendqq1 != null && friendqq1.size() != 0 && qqUserFriend != null && qqUserFriend.size() != 0) {
            Object qqFriend = friendqq1.get(0).get("nickname");
            Object remarkName = qqUserFriend.get(0).get("friendremarkname");
            nickName = qqFriend != null ? qqFriend.toString() : "";
            friendRemakName = remarkName != null ? remarkName.toString() : "";
            messageMap.put("nickname", nickName);
            messageMap.put("friendRemakName", friendRemakName);
            if ("".equals(nickName) || nickName.isEmpty()) {
                nickName = friendRemakName;
            }
        }
        //qqUserUin
        String qqName = "";
        List<Document> findqqName = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser", new BasicDBObject("uin", selfQqUin));
        if (findqqName != null && findqqName.size() > 0) {
            Object nickname = findqqName.get(0).get("nickname");
            qqName = nickname != null ? nickname.toString() : "";
        }
        // 聊天信息中的QQ好友信息及QQ用户信息
        if (qqmsg != null) {
            for (Document doc1 : qqmsg) {
                if (selfQqUin.equals(doc1.get("senderuin"))) {
                    doc1.append("issend", 0);
                } else {
                    doc1.append("issend", 1);
                }
                ;
                doc1.append("QQName", qqName);
                doc1.append("nickName", nickName);
            }
        }
        messageMap.put("qqmsg", qqmsg);
        // 聊天分页
        Long count = deviceMongoDao.countByGatherNameAndDBNameAndQuery(
                "infoData", "qqmsg",
                new BasicDBObject("c2cmsg_mark", c2cmark));
        int qqmsgCount = 0;
        //		int pageSize = 100;
        if (count != null) {
            qqmsgCount = (int) (count / pageSize + 1);
        }
        messageMap.put("qqmsgCount", qqmsgCount);

        return messageMap;
    }

    @Override
    public Map<String, Object> findWXTroopUinMsgByWXTroopUin(String wxTroopUin, Integer page, Integer pageSize, Long startTime, Long endTime, Integer timeSelectType, String searchContent, String startDate, String endDate) {
        if (StringUtils.isEmpty(wxTroopUin)) {
            throw new RuntimeException("对应的WX群号为NULL");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("分页条件有误");
        }
        wxTroopUin = wxTroopUin.trim();
        Map<String, Object> wxRoomMessageMap = new HashMap<>(4);
        // 搜索条件
        BasicDBObject query = new BasicDBObject();
        query.append("chatroomname", wxTroopUin);
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        contentFilter(searchContent, query, "msgname");
//        if (!StringUtils.isEmpty(searchContent)) {
//            String search = WordUtil.seg(searchContent, SegmentationAlgorithm.MaximumMatching);
//            String[] searchs = search.split(" ");
//            BasicDBList queryList = new BasicDBList();
//            for (String search1 : searchs) {
//                BasicDBObject qObject = new BasicDBObject();
//                Pattern pattern = Pattern.compile("^.*" + search1.trim() + ".*$", Pattern.CASE_INSENSITIVE);
//                qObject.append("msgname", pattern);
//                queryList.add(qObject);
//            }
//            query.append("$or", queryList);
//        }
        // 群消息
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        List<Document> wxChatroomMsg = deviceMongoDao.findInfoByGatherNameAndQuery(
                "infoData",
                "wxChatroomMsg",
                query,
                new BasicDBObject("msgtime", 1),
                page,
                pageSize);
        for (Document doc : wxChatroomMsg) {
            Object unameObj = doc.get("username");
            if (unameObj == null) {
                doc.append("nickanme", "未知");
                continue;
            }
            String username = unameObj.toString();
            List<Document> findwx = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                    "infoData2",
                    "t_wxuser",
                    new BasicDBObject().append("username", username));
            // 取群成员的昵称
            String nickname = "";
            if (findwx != null && !findwx.isEmpty()) {
                nickname = findwx.get(0).get("nickname").toString();
            }
            doc.append("nickname", nickname);
        }
        wxRoomMessageMap.put("wxChatroomMsg", wxChatroomMsg);
        // 群聊天框上面的群信息
        // 抽取群名字chatroomnickname   抽取群号chatroomname
        List<Document> findwxchatroom = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2",
                "t_wxchatroom",
                new BasicDBObject().append("chatroomname", wxTroopUin));
        // 群名字
        String chatRoomName = "";
        // 群号
        String chatRoomUser = "";
        if (findwxchatroom != null && !findwxchatroom.isEmpty()) {
            chatRoomName = findwxchatroom.get(0).get("chatroomnickname").toString();
            chatRoomUser = findwxchatroom.get(0).get("chatroomname").toString();
        }
        wxRoomMessageMap.put("chatRoomName", chatRoomName);
        wxRoomMessageMap.put("chatRoomUser", chatRoomUser);

        // 聊天分页
        int wxRoomMessageCount = 0;
        Long countWXChatroomMsg = deviceMongoDao.countByGatherNameAndDBNameAndQuery(
                "infoData",
                "wxChatroomMsg",
                //群里的所有消息
                new BasicDBObject("chatroomname", wxTroopUin));
        if (countWXChatroomMsg != null) {
            wxRoomMessageCount = (int) (countWXChatroomMsg / pageSize + 1);
        }
        wxRoomMessageMap.put("wxRoomMessageCount", wxRoomMessageCount);
        return wxRoomMessageMap;
    }

    @Override
    public Map<String, Object> findQQTroopUinMsgByQQTroopUin(String qqTroopUin, Integer page, Integer pageSize, Long startTime, Long endTime, Integer timeSelectType, String searchContent, String startDate, String endDate) {
        if (StringUtils.isEmpty(qqTroopUin)) {
            throw new RuntimeException("对应的qq群号为null");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("分页条件有误");
        }
        qqTroopUin = qqTroopUin.trim();
        Map<String, Object> queryTroopMessageMap = new HashMap<>(5);
        // 搜索条件
        BasicDBObject query = new BasicDBObject();
        query.append("troopuin", qqTroopUin.trim());
        BasicDBObject timeQuery = TimeUtils.getSearchStartDateAndEndDateByTimeSelectType(
                // 时间筛选条件
                timeSelectType, startTime, endTime);
        if (!timeQuery.isEmpty()) {
            query.append("msgtime", timeQuery);
        }
        contentFilter(searchContent, query, "msgdata");
//        if (!StringUtils.isEmpty(searchContent)) {
//            // 根据word的全切分算法分词方式分解关键词
//            String search = WordUtil.seg(searchContent, SegmentationAlgorithm.MaximumMatching);
//            String[] searchs = search.split(" ");
//            BasicDBList queryList = new BasicDBList();
//            for (int i = 0; i < searchs.length; i++) {
//                BasicDBObject qObject = new BasicDBObject();
//                Pattern pattern = Pattern.compile("^.*" + searchs[i].trim() + ".*$", Pattern.CASE_INSENSITIVE);
//                qObject.append("msgdata", pattern);
//                queryList.add(qObject);
//            }
//            query.append("$or", queryList);
//        }
        // 时间段筛选处理
        BasicDBObject tQuery = TimeUtils.getTimeNode(startDate, endDate);
        if (!tQuery.isEmpty()) {
            query.append("timeNode", tQuery);
        }
        // 群消息
        List<Document> qqTroopMsg = deviceMongoDao.findInfoByGatherNameAndQuery(
                "infoData",
                "qqTroopMsg",
                query,
                new BasicDBObject("msgtime", 1),
                page,
                pageSize);
        // 获取群成员昵称
        for (Document doc : qqTroopMsg) {
            // 发送者的qq
            String uin = (String) doc.get("senderuin");
            List<Document> findqq = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                    "infoData2",
                    "t_qquser",
                    // 获取群成员的昵称
                    new BasicDBObject("uin", uin));
            String nickname = "";
            if (findqq != null && !findqq.isEmpty()) {
                Object nickObj = findqq.get(0).get("nickname");
                nickname = nickObj != null ? nickObj.toString() : "";
            }
            doc.append("nickname", nickname);

        }
        queryTroopMessageMap.put("troopMsg", qqTroopMsg);
        // 查询群名称、群号、群公告
        List<Document> findTroopName = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2",
                "t_qq_troop",
                new BasicDBObject("troopuin", qqTroopUin));
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
            queryTroopMessageMap.put("troopName", troopName);
            queryTroopMessageMap.put("troopNumber", troopNumber);
            queryTroopMessageMap.put("troopMemo", troopMemo);
            // 分页
            int qqTrooMsgCount = 0;
            Long allQQTroopMsg = deviceMongoDao.countByGatherNameAndDBNameAndQuery(
                    "infoData",
                    "qqTroopMsg",
                    new BasicDBObject("troopuin", qqTroopUin));
            if (allQQTroopMsg != null) {
                qqTrooMsgCount = (int) (allQQTroopMsg / pageSize + 1);
            }
            queryTroopMessageMap.put("qqTrooMsgCount", qqTrooMsgCount);
        }
        return queryTroopMessageMap;
    }

    @Override
    public List<Document> findCountBydeviceUnique(String deviceUnique) {
        // 查询设备下的QQ账号
        BasicDBObject qqQuery = new BasicDBObject("device_unique", deviceUnique);
        List<Document> qqUin = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_qquser", qqQuery);
        if (!CollectionUtils.isEmpty(qqUin)) {
            qqUin.forEach(t -> {
                // 查询对应QQ号的好友聊天信息
               /* BasicDBList qqQueryList= new BasicDBList();
                qqQueryList.add(new BasicDBObject("receiveruin",t.get("uin")));
                qqQueryList.add(new BasicDBObject("senderuin",t.get("uin")));
                BasicDBObject qqMsgQuery = new BasicDBObject("$or",qqQueryList);
                List<Document> qqMsg = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData", "qqmsg", qqMsgQuery);*/
                List<String> c2cs = new ArrayList<>();
                BasicDBObject query;
                BasicDBObject querySearch = new BasicDBObject().append("uin", t.getString("uin"));
                List<Document> qqUserFriends = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                        "infoData2", "t_qquser_friend", querySearch);
                if (!CollectionUtils.isEmpty(qqUserFriends)) {
                    qqUserFriends.forEach(t1 -> {
                        String c2cmsg = Utils.NumberStringUniqueMD5(getStringForLong(t.getString("uin")), getStringForLong(t1.getString("fuin")));
                        c2cs.add(c2cmsg);
                    });
                }
                query = new BasicDBObject().append("c2cmsg_mark",
                        new BasicDBObject(QueryOperators.IN, c2cs.toArray(new String[]{})));
                List<Document> qqMsg = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData", "qqmsg", query);
                t.append("count", qqMsg.size());
            });
        }
        return qqUin;
    }
}
