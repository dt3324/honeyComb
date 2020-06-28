package com.hnf.honeycomb.serviceimpl;

import com.hnf.crypte.Utils;
import com.hnf.honeycomb.dao.EsBaseMongoDao;
import com.hnf.honeycomb.service.ElasticSearchService;
import com.hnf.honeycomb.service.EsDeviceService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.ESSearchUtil;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hnf.honeycomb.util.ObjectUtil.getStringForLong;

/**
 * @author hnf
 */
@Service
public class EsDeviceServiceImpl implements EsDeviceService {

    @Resource
    private EsBaseMongoDao ESBaseMongoDao;

    @Resource
    private ElasticSearchService elasticSearchService;

    //通过设备deviceUnique查询对应的设备名称
    @Override
    @Cacheable(keyGenerator = "keyGenerator", value = "findDeviceUnique2DeviceName")
    public Map<String, String> findDeviceUnique2DeviceName(List<String> deviceUniqes) {
        if (CollectionUtils.isEmpty(deviceUniqes)) {
            throw new RuntimeException("对应的设备唯一标识为NULL");
        }
        Map<String, String> unique2Name = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique",
                new BasicDBObject(QueryOperators.IN, deviceUniqes.toArray(new String[]{})));
        List<Document> devices =
                ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", query);
        if (!CollectionUtils.isEmpty(devices)) {
            unique2Name = devices.stream()
                    .collect(
                            Collectors.toMap(
                                    t -> t.getString("device_unique"),
                                    t -> t.getString("devicename"),
                                    (a, b) -> b,
                                    Hashtable::new)
                    );
        }
        return unique2Name;
    }

    //通过对应的设备deviceUnique查询设备基本信息
    @Override
    @Cacheable(value = "FindDeviceInfoByDeviceUnique", keyGenerator = "keyGenerator")
    public List<Document> findDeviceInfoByDeviceUnqiue(String deviceUnqiue) {
        if (StringUtils.isEmpty(deviceUnqiue)) {
            throw new RuntimeException("DEVICEUNIQUE为NULL");
        }
        BasicDBObject query = new BasicDBObject("device_unique", deviceUnqiue.trim());
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", query);
    }

    //查询设备与人员姓名与电话的对应关系
    @Override
    @Cacheable(value = "findDeviceUnique2PersonNameAndPhone", keyGenerator = "keyGenerator")
    public Map<String, String[]> findDeviceUnique2PersonNameAndPhone(List<String> deviceUniques) {
        if (CollectionUtils.isEmpty(deviceUniques)) {
            throw new RuntimeException("对应的设备唯一标识为NULL");
        }
        Map<String, String[]> unique2phoneAndName = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique",
                new BasicDBObject(QueryOperators.IN, deviceUniques.toArray(new String[]{})));
        List<Document> persons =
                ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_person", query);
        if (!CollectionUtils.isEmpty(persons)) {
            for (Document t : persons) {
                String[] strs = new String[2];
                String personPhone = "";
                String personName = "";
                personName = t.getString("personname");
                ArrayList<?> phoneObj = ArrayList.class.cast(t.get("phone"));
                if (phoneObj != null && phoneObj.size() != 0) {
                    personPhone = phoneObj.get(0) != null ? phoneObj.get(0).toString() : "";
                }
                strs[0] = personName;
                strs[1] = personPhone;
                ArrayList<?> personDeviceUniques = ArrayList.class.cast(t.get("device_unique"));
                if (!CollectionUtils.isEmpty(personDeviceUniques)) {
                    for (Object t1 : personDeviceUniques) {
                        String a = t1 != null ? t1.toString() : "";
                        unique2phoneAndName.put(a, strs);
                    }
                }
            }
        }
        return unique2phoneAndName;
    }

    //查询通讯录的名称
    @Override
    @Cacheable(value = "findContactNameByDeviceUniqueAndPhone", keyGenerator = "keyGenerator")
    public String findContactNameByDeviceUniqueAndPhone(String deviceUnique, String phone) {
        if (StringUtils.isEmpty(deviceUnique) || StringUtils.isEmpty(phone)) {
            return "";
        }
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique", deviceUnique.trim());
        query.append("phonenum", phone.trim());
        List<Document> contacts = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_contact_phonenum", query);
        if (CollectionUtils.isEmpty(contacts)) {
            return "";
        }
        Document contact = contacts.get(0);
        if (contact == null) {
            return "";
        }
        return contact.getString("personname");
    }

    //qq账号与昵称的对应关系
    @Override
    @Cacheable(value = "findQQNumber2QQNickByQQNums", keyGenerator = "keyGenerator")
    public Map<String, String> findQQNumber2QQNickByQQNums(List<String> qqNumber) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(qqNumber)) {
            return result;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("uin",
                new BasicDBObject(QueryOperators.IN, qqNumber.toArray(new String[]{})));
        List<Document> qqs = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser", query);
        if (!CollectionUtils.isEmpty(qqs)) {
            result = qqs.stream().collect(Collectors.toMap(t ->
                            t.getString("uin"),
                    t -> t.getString("nickname"),
                    (a, b) -> b, Hashtable::new));
        }
        return result;
    }

    //wx账号与昵称的对应关系
    @Override
    @Cacheable(value = "findWXNumber2WXNickByWXNums", keyGenerator = "keyGenerator")
    public Map<String, String> findWXNumber2WXNickByWXNums(List<String> wxNumbers) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(wxNumbers)) {
            return result;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("username",
                new BasicDBObject(QueryOperators.IN, wxNumbers.toArray(new String[]{})));
        List<Document> wxs = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxuser", query);
        if (!CollectionUtils.isEmpty(wxs)) {
            Map<String, String> map = new HashMap<>();
            for (Document t : wxs) {
                map.put(t.getString("username"), t.getString("nickname"));
            }
            result = map;
        }
        return result;
    }

    //wx群号与昵称的对应关系
    @Override
    @Cacheable(value = "findWXChatNumber2WXChatNickByWXChats", keyGenerator = "keyGenerator")
    public Map<String, String> findWXChatNumber2WXChatNickByWXChats(List<String> wxChats) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(wxChats)) {
            return result;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("chatroomname",
                new BasicDBObject(QueryOperators.IN, wxChats.toArray(new String[]{})));
        List<Document> wxs = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxchatroom", query);
        if (!CollectionUtils.isEmpty(wxs)) {
            Map<String, String> map = new HashMap<>();
            for (Document t : wxs) {
                map.put(t.getString("chatroomname"), t.getString("chatroomnickname"));
            }
            result = map;
        }
        return result;
    }

    //qq群号与昵称的对应关系
    @Override
    @Cacheable(value = "findQQTroopNumber2QQTroopNumberByQQTroops", keyGenerator = "keyGenerator")
    public Map<String, String> findQQTroopNumber2QQTroopNumberByQQTroops(List<String> qqTroops) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(qqTroops)) {
            return result;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("troopuin",
                new BasicDBObject(QueryOperators.IN, qqTroops.toArray(new String[]{})));
        List<Document> wxs = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qq_troop", query);
        if (!CollectionUtils.isEmpty(wxs)) {
            Map<String, String> map = new HashMap<>();
            for (Document t : wxs) {
                map.put(t.getString("troopuin"), t.getString("troopname"));
            }
            result = map;
        }
        return result;
    }

    //案件唯一标识查询所有相关信息
    @SuppressWarnings("unchecked")
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map<String, Object> findRelationDeviceAndPersonInfoByCaseUnqiue(String caseUnique) {
        if (StringUtils.isEmpty(caseUnique)) {
            throw new RuntimeException("设备唯一标识为NULL");
        }
        Map<String, Object> result = new HashMap<>();
        List<Document> cases =
                this.findCaseInfoByCaseUniques(Arrays.asList(caseUnique));
        List<Document> persons = new ArrayList<>();
        List<Document> devices = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cases)) {
            Document oneCase = cases.get(0);
            List<String> deviceUniques = new ArrayList<>();
            List<String> personUniques = new ArrayList<>();
            if (oneCase != null) {
                deviceUniques = ArrayList.class.cast(oneCase.get("device_unique"));
                personUniques = ArrayList.class.cast(oneCase.get("usernumber"));
            }
            persons = this.findPersonInfoByPersonUniques(personUniques);
            devices = this.findDeviceInfoByDeviceUniques(deviceUniques);
        }
        result.put("caseInfo", cases);
        result.put("deviceInfo", devices);
        result.put("personInfo", persons);
        return result;
    }

    //人员唯一标识查询相关联的所有信息
    @SuppressWarnings("unchecked")
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map<String, Object> findRelationDeviceAndCaseInfoByPersonUnqiue(String personUnique) {
        if (StringUtils.isEmpty(personUnique)) {
            throw new RuntimeException("人员身份证号为null");
        }
        Map<String, Object> result = new HashMap<>();
        List<Document> persons =
                this.findPersonInfoByPersonUniques(Arrays.asList(personUnique));
        List<Document> cases = new ArrayList<>();
        List<Document> devices = new ArrayList<>();
        if (!CollectionUtils.isEmpty(persons)) {
            Document onePerson = persons.get(0);
            List<String> deviceUniques = new ArrayList<>();
            List<String> caseUniques = new ArrayList<>();
            if (onePerson != null) {
                deviceUniques = ArrayList.class.cast(onePerson.get("device_unique"));
                caseUniques = ArrayList.class.cast(onePerson.get("caseuniquemark"));
            }
            cases = this.findCaseInfoByCaseUniques(caseUniques);
            devices = this.findDeviceInfoByDeviceUniques(deviceUniques);
        }
        result.put("caseInfo", cases);
        result.put("deviceInfo", devices);
        result.put("personInfo", persons);
        return result;
    }

    //设备唯一标识查询所有相关信息
    @SuppressWarnings("unchecked")
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map<String, Object> findRelationPersonAndCaseInfoByDeviceUnqiue(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique)) {
            throw new RuntimeException("案件唯一标识为NULL");
        }
        Map<String, Object> result = new HashMap<>();
        List<Document> devices =
                this.findDeviceInfoByDeviceUniques(Arrays.asList(deviceUnique));
        List<Document> cases = new ArrayList<>();
        List<Document> persons = new ArrayList<>();
        if (!CollectionUtils.isEmpty(devices)) {
            Document device = devices.get(0);
            List<String> personUniques = new ArrayList<>();
            List<String> caseUniques = new ArrayList<>();
            if (device != null) {
                personUniques = ArrayList.class.cast(device.get("usernumber"));
                caseUniques = ArrayList.class.cast(device.get("caseuniquemark"));
            }
            cases = this.findCaseInfoByCaseUniques(caseUniques);
            persons = this.findPersonInfoByPersonUniques(personUniques);
        }
        result.put("caseInfo", cases);
        result.put("deviceInfo", devices);
        result.put("personInfo", persons);
        return result;
    }

    //通过多个设备唯一标识查询设备相关信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findDeviceInfoByDeviceUniques(List<String> deviceUniques) {
        if (CollectionUtils.isEmpty(deviceUniques)) {
            return null;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique",
                new BasicDBObject(QueryOperators.IN, deviceUniques.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_device", query);
    }

    //通过多个案件唯一标识查询案件相关信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findCaseInfoByCaseUniques(List<String> caseUniques) {
        if (CollectionUtils.isEmpty(caseUniques)) {
            return null;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("caseuniquemark",
                new BasicDBObject(QueryOperators.IN, caseUniques.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_case", query);

    }

    //通过人员唯一标识查询人员相关信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findPersonInfoByPersonUniques(List<String> personUniques) {
        if (CollectionUtils.isEmpty(personUniques)) {
            return null;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("usernumber",
                new BasicDBObject(QueryOperators.IN, personUniques.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_person", query);
    }

    //查看短消息上下文
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findMessageContextByOneMsgInfo(String deviceUnique, String phone, String unique) {
        if (deviceUnique == null || deviceUnique.trim().isEmpty()) {
            throw new RuntimeException("设备unique为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("对应的电话为空");
        }
        if (unique == null || unique.trim().isEmpty()) {
            throw new RuntimeException("对应的unique为空");
        }
        phone = ESSearchUtil.hightLightStrToNormal(phone);
        List<Document> msgs = new ArrayList<>();
        BasicDBObject msgQuery = new BasicDBObject();
        msgQuery.append("uniseq", unique);
        List<Document> thisMsg = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData", "message", msgQuery);
        if (!CollectionUtils.isEmpty(thisMsg)) {
            Document oneThisMsg = thisMsg.get(0);
            Date time = Date.class.cast(oneThisMsg.get("time"));
            BasicDBObject contextQuery = new BasicDBObject();
            contextQuery.append("deviceUnique", deviceUnique);
            contextQuery.append("phonenum", phone);
            contextQuery.append("time", new BasicDBObject("$gte", time));//.append("$lte", dates.get(1)) gte大于等于（去掉e是大于）
            BasicDBObject sort = new BasicDBObject("time", 1);
            List<Document> doc1 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "message", contextQuery, sort, 1, 100);
            contextQuery.append("time", new BasicDBObject("$lt", time));//.append("$lte", dates.get(1)) gte大于等于（去掉e是大于）
            sort = new BasicDBObject("time", -1);
            List<Document> doc2 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "message", contextQuery, sort, 1, 100);
            doc1.addAll(doc2);
            msgs = doc1;
        }
        //		List<Document> msgs = MessageRMIUtils.messageContext(deviceUnique, unique, phone);
        //		msgs.forEach(t->System.out.println("t:"+t));
        String personName = "未知";
        String personPhone = "未知";
        if (msgs == null || msgs.isEmpty()) {
            return null;
        }
        List<Document> persons = this.findPersonInfoByDeviceUnique(deviceUnique);
        if (persons != null && !persons.isEmpty()) {
            Document person = persons.get(0);
            personName = person.getString("personname");
            ArrayList<?> phoneObj = ArrayList.class.cast(person.get("phone"));
            if (phoneObj != null && !phoneObj.isEmpty()) {
                personPhone = phoneObj.get(0) != null ? phoneObj.get(0).toString() : "";
            }
        }
        String findContactName = this.findContactNameByDeviceUniqueAndPhone(deviceUnique, phone);
        for (Document doc : msgs) {
            doc.append("personName", personName);
            doc.append("personPhone", personPhone);
            doc.append("contactName", findContactName);
        }
        //		msgs.forEach(t->System.out.println("t:"+t));
        msgs.sort((msg1, msg2) -> {
            if (msg1.getDate("time").getTime() < msg2.getDate("time").getTime()) {
                return -1;
            }
            return 1;
        });
        return msgs;
    }

    //通过设备唯一标识查询对应的人员相关信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findPersonInfoByDeviceUnique(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique)) {
            return null;
        }
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique", deviceUnique);
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_person", query);
    }

    //通过qq账号查询对应的qq基本信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findQQUserInfoByQQNumbers(List<String> qqNumber) {
        if (CollectionUtils.isEmpty(qqNumber)) {
            throw new RuntimeException("qq账号为NULL");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("uin",
                new BasicDBObject(QueryOperators.IN, qqNumber.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qquser", query);
    }

    //单个QQ查询
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findQQUserInfoByQQNumber(String qqNumber) {
        if (StringUtils.isEmpty(qqNumber)) {
            throw new RuntimeException("QQ账号为null");
        }
        qqNumber = ESSearchUtil.hightLightStrToNormal(qqNumber);
        List<Document> qqs = this.findQQUserInfoByQQNumbers(Arrays.asList(qqNumber.trim()));
        for (Document document : qqs) {
            document.remove("qqfriend");
            document.remove("richbuffer");
            document.remove("_id");
            document.remove("device");
            document.remove("device_unique");
            document.remove("ispublicaccount");
            document.get("e-mail");
            document.remove("e-mail");
            document.append("email", document.get("e-mail"));
        }
        return qqs;
    }

    //通过多个wx账号查询wx账号
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findWXUserInfoByWXNumbers(List<String> wxNumber) {
        if (CollectionUtils.isEmpty(wxNumber)) {
            throw new RuntimeException("wx账号为NULL");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("username",
                new BasicDBObject(QueryOperators.IN, wxNumber.toArray(new String[]{})));
        List<Document> wxs = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxuser", query);
        return wxs;
    }

    //单个wx查询
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findWXUserInfoByWXNumber(String wxNumber) {
        if (StringUtils.isEmpty(wxNumber)) {
            throw new RuntimeException("wx账号为null");
        }
        wxNumber = ESSearchUtil.hightLightStrToNormal(wxNumber);
        List<Document> wxs = this.findWXUserInfoByWXNumbers(Arrays.asList(wxNumber.trim()));
        for (Document document : wxs) {
            document.remove("wxmember");
            document.remove("_id");
            document.remove("device_unique");
        }
        return wxs;
    }

    //查询多个群信息
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findQQTroopInfoByQQTroopNumbers(List<String> qqTroopNumber) {
        if (CollectionUtils.isEmpty(qqTroopNumber)) {
            throw new RuntimeException("qq群号为NULL");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("troopuin",
                new BasicDBObject(QueryOperators.IN, qqTroopNumber.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_qq_troop", query);
    }

    /**查询单个群信息*/
    @SuppressWarnings("unchecked")
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map<String, Object> findQQTroopInfoByQQTroopNumber(String qqTroopNumber) {
        if (StringUtils.isEmpty(qqTroopNumber)) {
            throw new RuntimeException("QQ群号为NULL");
        }
        qqTroopNumber = ESSearchUtil.hightLightStrToNormal(qqTroopNumber);
        //返回数据格式
        Map<String, Object> troopDetail = new HashMap<>();
        List<Document> troopList = this.findQQTroopInfoByQQTroopNumbers(
                Arrays.asList(qqTroopNumber.trim()));
        ArrayList<String> member;
        String owner;
        if (!CollectionUtils.isEmpty(troopList)) {
            Document document = troopList.get(0);
            member = ArrayList.class.cast(document.get("qqmember"));
            owner = (String) document.get("troopowner");
            document.remove("_id");
            document.remove("qqmember");
            //QQ群主昵称
            if (owner != null && !owner.isEmpty()) {
                List<Document> findOwner = this.findQQUserInfoByQQNumber(owner);
                for (Document doc1 : findOwner) {
                    doc1.remove("qqfriend");
                    doc1.remove("richbuffer");
                    doc1.remove("_id");
                    doc1.remove("device");
                    doc1.remove("device_unique");
                    doc1.remove("ispublicaccount");
                    doc1.remove("homeplace");
                    doc1.remove("sex");
                    doc1.remove("e-mail");
                    doc1.remove("alias");
                    doc1.remove("age");
                }
                document.append("findOwner", findOwner);
            }
            troopDetail.put("troopBaseInfo", document);
            if (!CollectionUtils.isEmpty(member)) {
                List<Document> resultMember = new ArrayList<>();
                List<Document> qqMembers = this.findQQUserInfoByQQNumbers(member);
                for (Document doc : qqMembers) {
                    doc.remove("qqfriend");
                    doc.remove("richbuffer");
                    doc.remove("_id");
                    doc.remove("device");
                    doc.remove("device_unique");
                    doc.remove("ispublicaccount");
                    doc.remove("homeplace");
                    doc.remove("sex");
                    doc.remove("e-mail");
                    doc.remove("alias");
                    doc.remove("age");
                    resultMember.add(doc);
                    //					troopList.add(doc);
                }
                ;
                troopDetail.put("memberInfo", resultMember);
            }
        }
        return troopDetail;
    }

    //通过wx群查询对应的群列表
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findWXTroopInfoByWXTroopNumbers(List<String> wxTroopNumber) {
        if (CollectionUtils.isEmpty(wxTroopNumber)) {
            throw new RuntimeException("wx群号为NULL");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("chatroomname",
                new BasicDBObject(QueryOperators.IN, wxTroopNumber.toArray(new String[]{})));
        return ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData2", "t_wxchatroom", query);
    }

    //查询对应wx群号的方法
    @SuppressWarnings("unchecked")
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map<String, Object> findWXTroopInfoByWXTroopNumber(String wxTroopNumber) {
        if (StringUtils.isEmpty(wxTroopNumber)) {
            throw new RuntimeException("WX群号为null");
        }
        wxTroopNumber = ESSearchUtil.hightLightStrToNormal(wxTroopNumber);
        Map<String, Object> result = new HashMap<>();
        List<Document> findwxchatroom =
                this.findWXTroopInfoByWXTroopNumbers(Arrays.asList(wxTroopNumber));
        ArrayList<String> member = new ArrayList<>();
        String owner = "";
        for (Document document : findwxchatroom) {
            member = (ArrayList.class.cast(document.get("wxmember")));
            owner = (String) document.get("chatroomowner");
            document.remove("_id");
            document.remove("wxmember");
            document.remove("chatroomowner");
            //微信群主
            if (owner != null && !owner.toString().isEmpty()) {
                List<Document> findOwner =
                        this.findWXUserInfoByWXNumbers(Arrays.asList(owner));
                for (Document doc2 : findOwner) {
                    doc2.remove("_id");
                    doc2.remove("loc2");
                    doc2.remove("wxmember");
                    doc2.remove("loc1");
                    doc2.remove("sex");
                    doc2.remove("sign");
                    doc2.remove("device_unique");
                    doc2.remove("bindphone");
                    doc2.remove("bindqq");
                    doc2.remove("extends");
                    doc2.remove("tweibo");
                    doc2.remove("email");
                }
                document.append("findOwner", findOwner);
            }
            result.put("troopBaseInfo", document);
        }
        List<Document> wxMembers = this.findWXUserInfoByWXNumbers(member);
        List<Document> resultMember = new ArrayList<>();
        //微信群成员
        for (Document doc1 : wxMembers) {
            doc1.remove("_id");
            doc1.remove("loc2");
            doc1.remove("wxmember");
            doc1.remove("loc1");
            doc1.remove("sex");
            doc1.remove("sign");
            doc1.remove("device_unique");
            doc1.remove("bindphone");
            doc1.remove("bindqq");
            doc1.remove("extends");
            doc1.remove("tweibo");
            doc1.remove("email");
            resultMember.add(doc1);
            //			findwxchatroom.add(doc1);
        }
        result.put("memberInfo", resultMember);

        return result;
    }

    //通过wx好友查询聊天详情
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findQQFriendMsgContextByOneMsgInfo(String qqSelfUin, String qqFriendUin, String unique) {
        if (qqSelfUin == null || qqSelfUin.trim().isEmpty()) {
            throw new RuntimeException("查询上下文自己的账号为空");
        }
        if (qqFriendUin == null || qqFriendUin.trim().isEmpty()) {
            throw new RuntimeException("查询qq聊天记录上下文的好友账号为空");
        }
        if (unique == null || unique.trim().isEmpty()) {
            throw new RuntimeException("当前消息的unique为空");
        }
        String c2cMark = Utils
                .NumberStringUniqueMD5(
                        getStringForLong(ESSearchUtil.hightLightStrToNormal(qqSelfUin))
                        , getStringForLong(ESSearchUtil.hightLightStrToNormal(qqFriendUin))
                );
        BasicDBObject query1 = new BasicDBObject();
        query1.append("uniseq", unique.trim());
        List<Document> thisMsg = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData", "qqmsg", query1);
        List<Document> doc = new ArrayList<>();
        if (!CollectionUtils.isEmpty(thisMsg)) {
            Document oneThisMsg = thisMsg.get(0);
            if (oneThisMsg != null) {
                Date time = (Date) oneThisMsg.get("msgtime");
                BasicDBObject contextQuery = new BasicDBObject();
                contextQuery.append("c2cmsg_mark", c2cMark);
                contextQuery.append("msgtime", new BasicDBObject("$gte", time));
                BasicDBObject sort = new BasicDBObject("msgtime", 1);
                doc = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData", "qqmsg", contextQuery, sort, 1, 100);
                contextQuery.append("msgtime", new BasicDBObject("$lt", time));
                sort = new BasicDBObject("msgtime", -1);
                List<Document> docc = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData", "qqmsg", contextQuery, sort, 1, 100);
                doc.addAll(docc);
                if (!CollectionUtils.isEmpty(doc)) {
                    Document firstDoc = doc.get(0);
                    List<String> seAndReUin = Arrays.asList(
                            firstDoc.getString("senderuin"), firstDoc.getString("receiveruin"));
                    Map<String, String> uin2Nicks = this.findQQNumber2QQNickByQQNums(seAndReUin);
                    for (Document t : doc) {
                        String senderUin = t.getString("senderuin");
                        String reciverUin = t.getString("receiveruin");
                        String senderNick = uin2Nicks.get(senderUin) != null ? uin2Nicks.get(senderUin) : "未知";
                        String reNick = uin2Nicks.get(reciverUin) != null ? uin2Nicks.get(reciverUin) : "未知";
                        t.append("sendernick", senderNick);
                        t.append("recivernick", reNick);
                    }
                }
                doc.sort((msg1, msg2) -> {
                    if (msg1.getDate("msgtime").getTime() <= msg2.getDate("msgtime").getTime()) {
                        return -1;
                    }
                    return 1;
                });
            }

        }
        return doc;
    }

    //查询wx好友聊天上下文
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findWXFriendMsgContextByOneMsgInfo(String wxUserName, String wxFriendName, String unique) {
        if (wxUserName == null || wxUserName.trim().isEmpty()) {
            throw new RuntimeException("自己的wx号为空");
        }
        if (wxFriendName == null || wxFriendName.trim().isEmpty()) {
            throw new RuntimeException("好友wx号为空");
        }
        if (unique == null || unique.trim().isEmpty()) {
            throw new RuntimeException("当前消息的unique为空");
        }
        List<Document> result = new ArrayList<>();
        String c2cmark = Utils.StringUniqueMD5(ESSearchUtil.hightLightStrToNormal(wxUserName)
                , ESSearchUtil.hightLightStrToNormal(wxFriendName));
        BasicDBObject query1 = new BasicDBObject();
        query1.append("uniseq", unique.trim());
        List<Document> thisMsgs = ESBaseMongoDao.
                findInfoByDBNameAndGatherNameAndQuery("infoData", "wxmsg", query1);
        if (!CollectionUtils.isEmpty(thisMsgs)) {
            Document thisMsg = thisMsgs.get(0);
            Date time = (Date) thisMsg.get("msgtime");
            BasicDBObject query = new BasicDBObject();
            query.append("c2cmark", c2cmark);
            query.append("msgtime", new BasicDBObject("$gte", time));
            BasicDBObject sort = new BasicDBObject("msgtime", 1);
            List<Document> docs = ESBaseMongoDao.
                    findInfoByGatherNameAndQuery("infoData", "wxmsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(docs)) {
                result.addAll(docs);
            }
            query.append("msgtime", new BasicDBObject("$lt", time));
            sort = new BasicDBObject("msgtime", -1);
            List<Document> docs1 = ESBaseMongoDao.
                    findInfoByGatherNameAndQuery("infoData", "wxmsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(docs1)) {
                result.addAll(docs1);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            Document doc = result.get(0);
            if (doc != null) {
                List<String> seAndReUins = Arrays.asList(
                        doc.getString("receivername"), doc.getString("sendername"));
                Map<String, String> uin2Nick = this.findWXNumber2WXNickByWXNums(seAndReUins);
                for (Document t : result) {
                    String reUin = doc.getString("receivername");
                    String seUin = doc.getString("sendername");
                    String reNick = uin2Nick.get(reUin) != null ? uin2Nick.get(reUin) : "未知";
                    String seNick = uin2Nick.get(seUin) != null ? uin2Nick.get(seUin) : "未知";
                    t.append("sendernick", seNick);
                    t.append("revicernick", reNick);
//					System.out.println(">>>>>>>>>>>>>"+t);}
                }
            }
            result.sort((msg1, msg2) -> {
                if (msg1.getDate("msgtime").getTime() <= msg2.getDate("msgtime").getTime()) {
                    return -1;
                }
                return 1;
            });
        }
        return result;
    }

    //查看qq群消息上下文
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findQQTroopMsgContextByOneMsgInfo(String troopUin, String unique) {
        if (troopUin == null || troopUin.trim().isEmpty()) {
            throw new RuntimeException("搜索上下文的群号为空");
        }
        if (unique == null || unique.trim().isEmpty()) {
            throw new RuntimeException("搜索上下文的消息unique为空");
        }
        troopUin = ESSearchUtil.hightLightStrToNormal(troopUin);
        List<Document> result = new ArrayList<>();
        BasicDBObject query1 = new BasicDBObject();
        query1.append("uniseq", unique.trim());
        List<Document> thisMsg = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData", "qqTroopMsg", query1);
        if (!CollectionUtils.isEmpty(thisMsg)) {
            Document oneMsg = thisMsg.get(0);
            Date time = (Date) oneMsg.get("msgtime");
            BasicDBObject query = new BasicDBObject();
            query.append("troopuin", troopUin);
            query.append("msgtime", new BasicDBObject("$gte", time));
            BasicDBObject sort = new BasicDBObject("msgtime", 1);
            List<Document> doc1 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "qqTroopMsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(doc1)) {
                result.addAll(doc1);
            }
            query.append("msgtime", new BasicDBObject("$lt", time));
            sort = new BasicDBObject("msgtime", -1);
            List<Document> doc2 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "qqTroopMsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(doc2)) {
                result.addAll(doc2);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            Document firstDoc = result.get(0);
            String troopNick = "未知";
            if (firstDoc != null) {
                String msgToopUin = firstDoc.getString("troopuin");
                Map<String, String> number2Nick =
                        this.findQQTroopNumber2QQTroopNumberByQQTroops(Arrays.asList(msgToopUin));
                if (number2Nick.get(msgToopUin) != null) {
                    troopNick = number2Nick.get(msgToopUin);
                }
            }
            String insertTroopNick = troopNick;
            HashSet<String> qqNumbers = new HashSet<>();
            for (Document document : result) {
                String senderuin = document.getString("senderuin");
                qqNumbers.add(senderuin);
            }
            List<String> searchQqNumbers = new ArrayList<>();
            searchQqNumbers.addAll(qqNumbers);
            Map<String, String> uin2Nick =
                    this.findQQNumber2QQNickByQQNums(searchQqNumbers);
            for (Document t : result) {
                String nick = "未知";
                String seUin = t.getString("senderuin");
                if (uin2Nick.get(seUin) != null) {
                    nick = uin2Nick.get(seUin);
                }
                t.append("troopNick", insertTroopNick);
                t.append("senderNick", nick);
            }
            result.sort((msg1, msg2) -> {
                if (msg1.getDate("msgtime").getTime() < msg2.getDate("msgtime").getTime()) {
                    return -1;
                }
                return 1;
            });
        }
        return result;
    }

    //wx群上下文
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findWXChatroomMsgContextByOneMsgInfo(String wxTroopUin, String unique) {
        if (wxTroopUin == null || wxTroopUin.trim().isEmpty()) {
            throw new RuntimeException("搜索上下文的wx群号为空");
        }
        if (unique == null || unique.trim().isEmpty()) {
            throw new RuntimeException("搜索上下文的unique为空");
        }
        //去除前端es多余的字符
        wxTroopUin = ESSearchUtil.hightLightStrToNormal(wxTroopUin);
        List<Document> result = new ArrayList<>();
        BasicDBObject query1 = new BasicDBObject();
        query1.append("uniseq", unique);
        List<Document> thisMsg = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                "infoData", "wxChatroomMsg", query1);
        if (!CollectionUtils.isEmpty(thisMsg)) {
            Document oneThisMsg = thisMsg.get(0);
            Date time = (Date) oneThisMsg.get("msgtime");
            BasicDBObject query = new BasicDBObject();
            query.append("chatroomname", wxTroopUin);
            query.append("msgtime", new BasicDBObject("$gte", time));
            BasicDBObject sort = new BasicDBObject("msgtime", 1);
            List<Document> doc1 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "wxChatroomMsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(doc1)) {
                result.addAll(doc1);
            }
            query.append("msgtime", new BasicDBObject("$lt", time));
            sort = new BasicDBObject("msgtime", -1);
            List<Document> doc2 = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                    "infoData", "wxChatroomMsg", query, sort, 1, 100);
            if (!CollectionUtils.isEmpty(doc2)) {
                result.addAll(doc2);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            Document firstDoc = result.get(0);
            String troopNick = "未知";
            if (firstDoc != null) {
                String msgToopUin = firstDoc.getString("chatroomname");
                Map<String, String> number2Nick =
                        this.findWXChatNumber2WXChatNickByWXChats(Arrays.asList(msgToopUin));
                if (number2Nick.get(msgToopUin) != null) {
                    troopNick = number2Nick.get(msgToopUin);
                }
            }
            String insertTroopNick = troopNick;
            HashSet<String> wxNumbers = new HashSet<>();
            for (Document document : result) {
                String nickname = document.getString("nickname");
                wxNumbers.add(nickname);
            }
            List<String> searchWxNumbers = new ArrayList<>(wxNumbers);
            Map<String, String> uin2Nick =
                    this.findWXNumber2WXNickByWXNums(searchWxNumbers);
            for (Document t : result) {
                String nick = "未知";
                String seUin = t.getString("senderuin");
                if (uin2Nick.get(seUin) != null) {
                    nick = uin2Nick.get(seUin);
                }
                t.append("chatroomnick", insertTroopNick);
                t.append("sendernick", nick);
            }
            result.sort((msg1, msg2) -> {
                if (msg1.getDate("msgtime").getTime() < msg2.getDate("msgtime").getTime()) {
                    return -1;
                }
                return 1;
            });
        }
        return result;
    }

    //模糊匹配账号
    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findNumInfo(String searchNumber, String searchType) {
        if (StringUtils.isEmpty(searchNumber)) {
            throw new RuntimeException("搜索账号为NULL");
        }
        if (StringUtils.isEmpty(searchType)) {
            throw new RuntimeException("搜索的类型为NULL");
        }
        searchNumber = searchNumber.trim();
        searchType = searchType.trim();
        String[] searchNumbers = StringUtils.bongDuplicateremove(searchNumber.split(","));
        List<Map<String, Object>> result = new ArrayList<>();// 每次搜索人员存储对应的信息
        BasicDBObject personQuery = new BasicDBObject();// 对应的人员查询条件
        HashSet<String> likePhone = new HashSet<>();
        HashSet<String> likeWx = new HashSet<>();
        HashSet<String> likeQq = new HashSet<>();
        HashSet<String> likeId = new HashSet<>();
        switch (searchType) {
            case "phone":
                for (String phone : searchNumbers) {
                    if (StringUtils.isEmpty(phone)) {
                        continue;
                    }
                    BasicDBObject query = new BasicDBObject();
                    Pattern pattern = Pattern.compile(
                            "^" + phone.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                    query.append("phone", pattern);
                    List<Document> persons = ESBaseMongoDao.findInfoByGatherNameAndQuery(
                            "infoData2", "t_person", query, null, 1, 10);
                    if (CollectionUtils.isEmpty(persons)) {
                        continue;
                    }
                    List<String> personPhones = getPhoneFromPerson(persons);
                    if (!CollectionUtils.isEmpty(personPhones)) {
                        for (String t : personPhones) {
                            if (t.startsWith(searchNumber)) {
                                likePhone.add(t);
                            }
                        }
                    }
                    likePhone.addAll(elasticSearchService.searchPreNumber(searchType, phone));
                    //对ES中的电话号码进行查询
                }
                if (CollectionUtils.isEmpty(likePhone)) {
                    break;
                }
                for (String str : likePhone) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("number", str);
                    map.put("name", "");
                    personQuery.append("phoneNUM", str);
                    List<Document> persons = findPersonExtendInfoByDoc(null, personQuery);
                    if (!CollectionUtils.isEmpty(persons)) {
                        map.put("name", persons.get(0).get("personName"));
                    }
                    result.add(map);
                }
                break;
            case "wx":
                for (String wx : searchNumbers) {
                    likeWx.addAll(
                            elasticSearchService.searchPreNumber(searchType, wx));
                }
                if (CollectionUtils.isEmpty(likeWx)) {
                    break;
                }
                for (String str : likeWx) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("number", str);
                    map.put("name", "");
                    personQuery.append("WXNUM", str);
                    List<Document> persons = findPersonExtendInfoByDoc(null, personQuery);
                    if (!CollectionUtils.isEmpty(persons)) {
                        map.put("name", persons.get(0).get("personName"));
                    }
                    result.add(map);
                }
                break;
            case "qq":
                for (String qq : searchNumbers) {
                    likeQq.addAll(
                            elasticSearchService.searchPreNumber(searchType, qq));
                }
                if (CollectionUtils.isEmpty(likeQq)) {
                    break;
                }
                if (!CollectionUtils.isEmpty(likeQq)) {
                    for (String str : likeQq) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("number", str);
                        map.put("name", "");
                        personQuery.append("QQNUM", str);
                        List<Document> persons = findPersonExtendInfoByDoc(null, personQuery);
                        if (!CollectionUtils.isEmpty(persons)) {
                            map.put("name", persons.get(0).getString("personName"));
                        }
                        result.add(map);
                    }
                }
                break;
            case "IDNumber":
                Pattern pattern = Pattern.compile("^" + searchNumber.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                personQuery.append("personIdCard", pattern);
                List<Document> persons = findPersonExtendInfoByDoc(null, personQuery);
                if (!CollectionUtils.isEmpty(persons)) {
                    for (Document doc : persons) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("number", doc.getString("personIdCard"));
                        map.put("name", doc.getString("personName"));
                        result.add(map);
                    }
                }
                break;
            default:
                throw new RuntimeException("对应的账号类型不合规");
        }
        return result;
    }

    /**
     * 将原来处理 virtual mongoDB数据 的方法移动至此
     *
     * @param searchField
     * @param query
     * @return List<Document>
     * @throws
     * @Title: findPersonExtendInfoByDoc
     */
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<Document> findPersonExtendInfoByDoc(String searchField, BasicDBObject query) {
        List<Document> result = ESBaseMongoDao.findInfoByGatherNameAndQuery("virtual", "t_personBaseInfo", query, null, null, null);
        try {
            // 当有删选条件以及结果不为空时,进行对应的筛选操作
            if (searchField != null && !searchField.trim().isEmpty() && result != null && !result.isEmpty()) {
                List<Document> newResult = new ArrayList<>();
                String searchContent = query.getString(searchField).replace("^", "").replace(".*$", "");
                for (Document t : result) {
                    String personName = t.getString("personName");
                    String personIdNumber = t.getString("personIdCard");
                    ArrayList<?> fieldValues = ArrayList.class.cast(t.get(searchField));
                    newResult.addAll(fieldValues.stream().map((t1) -> {
                        Document doc = new Document();
                        if (t1.toString().startsWith(searchContent)) {
                            doc.append("personName", personName);
                            doc.append("personIdCard", personIdNumber);
                            doc.append(searchField, t1);
                            return doc;
                        }
                        return null;
                    }).filter((t2) -> {
                        return t2 != null;
                    }).collect(Collectors.toList()));
                }
                result = newResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 通过对应的从MongoDB集合中获取对应的电话号码集合
     *
     * @param persons
     * @return 返回获取的电话号码
     */
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public List<String> getPhoneFromPerson(List<Document> persons) {
        List<String> phones = new ArrayList<>();
        if (persons == null || persons.isEmpty()) {
            return phones;
        }
        persons.stream().map(t -> {
            return t.get("phone");
        }).map(ArrayList.class::cast).filter(phoneList -> {
            return phoneList != null && !phoneList.isEmpty();
        }).forEach(phoneList -> {
            phoneList.forEach(phone -> {
                phones.add(phone.toString());
            });
        });
        return phones;
    }

    @Override
    @Cacheable(value = "findRelationDeviceAndPersonInfoByCaseUnqiue", keyGenerator = "keyGenerator")
    public Map findInfoByQqOrWx(String uin) {
        // TODO Auto-generated method stub
        if (StringUtils.isEmpty(uin)) {
            throw new RuntimeException("账号为空");
        }
        Map map = new HashMap<>();
        BasicDBObject query = new BasicDBObject();
        BasicDBList queryList = new BasicDBList();
        queryList.add(new BasicDBObject("QQNUM", uin));
        queryList.add(new BasicDBObject("WXNUM", uin));
        query.put("$or", queryList);
        //通过QQ或者微信查询对应的人员
        List<Document> person = ESBaseMongoDao.findInfoByGatherNameAndQuery("virtual", "t_personBaseInfo", query, null, null, null);
        BasicDBObject queryInfo = new BasicDBObject();
        List<Document> device = new ArrayList<>();
        List<Document> cases = new ArrayList<>();
        if (person != null && !person.isEmpty()) {
            queryInfo.append("usernumber", person.get(0).get("personIdCard"));
            //设备信息
            device = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", queryInfo);
            //案件信息
            cases = ESBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_case", queryInfo);
        }
        map.put("device", device);
        map.put("case", cases);
        map.put("person", person);
        return map;
    }
}
