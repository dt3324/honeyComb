package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.remote.virtual.VirtualCall;
import com.hnf.honeycomb.service.InsertLogService;
import com.hnf.honeycomb.service.VirtualRemarkService;
import com.hnf.honeycomb.util.Base64;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lsj
 */
@Service("remarkService")
public class VirtualRemarkServiceImpl implements VirtualRemarkService {

    @Resource
    private InsertLogService insertLogService;
    @Resource
    private VirtualCall virtualCall;

    private final MongoClient mongoClient;

    private final MongoClient mongoClient212;

    private MongoDatabase db = null;
    private MongoCollection<Document> collection = null;

    @Autowired
    public VirtualRemarkServiceImpl(@Qualifier(MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient, @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient212) {
        this.mongoClient = mongoClient;
        this.mongoClient212 = mongoClient212;
    }

    private static void accept(Document t) {
        String remark = t.getString("QQFREM");
        remark = recodeRemark(remark);
        t.append("QQFREM", remark);
    }

    /**
     * 查找备注
     */
    @Override
    public Map<String, Object> findRemark(Integer userId, String searchNum, String type, String place) {
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("对应的搜索账号为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("对应备注的类型为空");
        }
        searchNum = searchNum.trim();
        //此处对对应的日志文件进行操作
        //对应的结果
        Map<String, Object> result = new HashMap<>(4);
        //存储自己的账号
        Map<String, Object> self = new HashMap<>(5);
        self.put("usernum", searchNum);
        self.put("Username", "");
        self.put("Device_unique", new ArrayList<>());
        BasicDBObject personQuery = new BasicDBObject();
        switch (type) {
            case "qq":
                List<Document> qqDetail = new ArrayList<>();
                db = mongoClient.getDatabase("infoData2");
                Document query = new Document();
                query.append("uin", searchNum);
                collection = db.getCollection("t_qquser");
                boolean notNull = Optional.ofNullable(collection.find(query)).isPresent();
                //查询到结果存入qq详细信息list
                if (notNull) {
                    collection.find(query).iterator().forEachRemaining(qqDetail::add);
                }
                if (!CollectionUtils.isEmpty(qqDetail) && null != qqDetail.get(0)) {
                    self.put("Username", qqDetail.get(0).getString("nickname"));
                    self.put("Device_unique", qqDetail.get(0).get("device_unique"));
                }
                getQQRemark(searchNum, result);
                break;
            case "wx":
                List<Document> wxDetail = new ArrayList<>();
                db = mongoClient.getDatabase("infoData2");
                collection = db.getCollection("t_wxuser");

                Document queryWX = new Document();
                queryWX.append("username", searchNum);
                //查询到结果
                FindIterable<Document> resultDocWx = collection.find(queryWX);
                for (Document d : resultDocWx) {
                    wxDetail.add(d);
                }
                if (!CollectionUtils.isEmpty(wxDetail) && wxDetail.get(0) != null) {
                    self.put("Username", wxDetail.get(0).getString("nickname"));
                    self.put("Device_unique", wxDetail.get(0).get("device_unique"));
                }
                getWXRemark(searchNum, result);
                break;
            case "phone":
                /*
                 * 需要修改数据库
                 */
                //查询到结果
                List<Document> persons = virtualCall.findPhoneMsg(searchNum);
                if (!CollectionUtils.isEmpty(persons) && persons.get(0) != null) {
                    self.put("Username", persons.get(0).getString("personName"));
                    self.put("Device_unique", persons.get(0).get("device_unique"));
                }
                getPhoneRemark(searchNum, result);
                break;
            default:
                break;
        }
        result.put("UserInfo", self);
        return result;
    }

    /**
     * 查询电话号码的备注
     */
    private void getPhoneRemark(String searchNum, Map<String, Object> result) {
        List<Document> remarks = virtualCall.getPhoneRemarkNew(searchNum);
//        if (!CollectionUtils.isEmpty(remarks)) {
//            remarks.forEach(t -> {
//                String remark = t.getString("PHFNAME");
//                remark = recodeRemark(remark);
//                t.append("PHFNAME", remark);
//            });
//        }
//
//        if (!CollectionUtils.isEmpty(remarks)) {
//            //为其赋初始值
//            remarks.forEach(t -> {
//                t.append("selfNick", "");
//                String phoneNum = t.getString("PHFNUM");
//                List<Document> persons = virtualCall.findPhoneMsg(phoneNum);
//                if (!CollectionUtils.isEmpty(persons)) {
//                    t.append("selfNick", persons.get(0).getString("personName"));
//                }
//            });
//        }
        result.put("Friend", remarks);
    }

    /**
     * 查询WX的备注
     */
    private void getWXRemark(String searchNum, Map<String, Object> result) {
        //查询条件
        List<Document> remarks = virtualCall.getWXRemark(searchNum);
        if (!CollectionUtils.isEmpty(remarks)) {
            remarks.forEach(t -> {
                String remark = t.getString("WXFREM");
                remark = recodeRemark(remark);
                t.append("WXFREM", remark);
            });
        }
        if (!CollectionUtils.isEmpty(remarks)) {
            List<String> wxUin = remarks.stream().map(t -> t.getString("WXNUM")).collect(Collectors.toList());
            List<Document> msgCounts = new ArrayList<>();
            Map<String, String> uin2Nick = new HashMap<>(2);

            BasicDBObject fileds = new BasicDBObject("username", 1).append("nickname", 1).append("_id", 0);
            db = mongoClient.getDatabase("infoData2");
            collection = db.getCollection("t_wxuser");
            Document wxQuery = new Document();
            wxQuery.append("username", new Document(QueryOperators.IN, wxUin));
            FindIterable<Document> ite = collection.find(wxQuery)
                    .projection(fileds);
            ite.iterator().forEachRemaining(msgCounts::add);
            if (!CollectionUtils.isEmpty(msgCounts)) {
                uin2Nick = msgCounts.stream().collect(Collectors.toMap(t -> t.getString("username"), t -> t.getString("nickname"), (a, b) -> b));
            }

            Map<String, String> finalUin2Nick = uin2Nick;
            remarks.forEach(t -> {
                String nick = finalUin2Nick.get(t.getString("WXNUM")) != null ? finalUin2Nick.get(t.getString("WXNUM")) : "";
                t.append("selfNick", nick);
            });
        }
        result.put("Friend", remarks);
    }

    /**
     * 查询QQ好友的备注
     * @param result    已经包含的结果
     */
    private void getQQRemark(String searchNum, Map<String, Object> result) {
//        List<Document> remarks;
        Document query = new Document();
        query.append("QQFNUM", searchNum);
        List<Document> remarks = virtualCall.getQQRemark(searchNum);
        if (!CollectionUtils.isEmpty(remarks)) {
            //将每个好友的备注解密并存入原list Document中
            remarks.forEach(VirtualRemarkServiceImpl::accept);
        }
        //查询账号自己的昵称
        if (!CollectionUtils.isEmpty(remarks)) {
            List<String> qqUin = remarks.stream().map(remark -> remark.getString("QQNUM")).collect(Collectors.toList());
            Map<String, String> uin2nick = new HashMap<>(qqUin.size());
            BasicDBObject fileds = new BasicDBObject("uin", 1).append("nickname", 1).append("_id", 0);
            db = mongoClient.getDatabase("infoData2");
            collection = db.getCollection("t_qquser");
            List<Document> msgCounts = new ArrayList<>();
            //添加各个字段的统计条数
            Document qqQuery = new Document();
            qqQuery.append("uin", new Document(QueryOperators.IN, qqUin));
            collection.find(qqQuery).projection(fileds)
                    .iterator().forEachRemaining(msgCounts::add);
            if (!CollectionUtils.isEmpty(msgCounts)) {
                //存入qq和对应的备注信息到map中
                for (Document t : msgCounts) {
                    uin2nick.put(t.getString("uin"), t.getString("nickname"));
                }
            }
            remarks.forEach(t -> {
                String nick = uin2nick.get(t.getString("QQNUM")) != null ? uin2nick.get(t.getString("QQNUM")) : "";
                t.append("selfNick", nick);
            });
        }
        result.put("Friend", remarks);
        //查询群备注
        List<Document> troopRemarks = virtualCall.getQQFlockRemark(searchNum);
        //查询到结果
        //转化Document->json
        if (!CollectionUtils.isEmpty(troopRemarks)) {
            troopRemarks.forEach(t -> {
                String remark = t.getString("QQNUMREM");
                remark = recodeRemark(remark);
                t.append("QQNUMREM", remark);
            });
        }

        if (!CollectionUtils.isEmpty(troopRemarks)) {
            Map<String, String> troopUin2Name = new HashMap<>(2);
            BasicDBObject fileds = new BasicDBObject("troopuin", 1).append("troopname", 1).append("_id", 0);
            List<Document> msgCounts = new ArrayList<>();
            db = mongoClient.getDatabase("infoData2");
            collection = db.getCollection("t_qq_troop");
            FindIterable<Document> ite = collection.find(query)
                    .projection(fileds);
            for (Document doc : ite) {
                msgCounts.add(doc);
            }
            if (!CollectionUtils.isEmpty(msgCounts)) {
                troopUin2Name = msgCounts.stream().collect(Collectors.toMap(t -> t.getString("troopuin"), t -> t.getString("troopname"), (a, b) -> b));
            }

            Map<String, String> finalTroopUin2Name = troopUin2Name;
            troopRemarks.forEach(t -> {
                String troopName = finalTroopUin2Name.get(t.getString("QQGNUM"));
                t.append("troopName", troopName != null ? troopName : "");
            });
        }
        result.put("Group", troopRemarks);
    }

    /**通过对应的账号搜索的基本人员数据*/
    @Override
    public List<Map<String, Object>> findPersonExtendsInfoBySearchNum(Integer userId,
                                                                      String searchNum,
                                                                      String type,
                                                                      String place) {
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("对应的备注账号为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("对应的搜索类型为空");
        }
        //执行对应的备注日志信息入库操作
        insertLogService.insertRemarkLog(userId, searchNum, type, place);
        List<Map<String, Object>> result = new ArrayList<>();
        List<Document> persons = new ArrayList<>();
        searchNum = searchNum.trim();
        switch (type) {
            case "qq":
                persons = virtualCall.findQqMsg(searchNum);
                break;
            case "wx":
                persons = virtualCall.findWxMsg(searchNum);
                break;
            case "phone":
                persons = virtualCall.findPhoneMsg(searchNum);
                break;
            case "papers":
                persons = virtualCall.findPapersMsg(searchNum);
                break;
            default:
                break;
        }
        //对于未搜索到对应的人员数据的情况
        if (CollectionUtils.isEmpty(persons)) {
            Map<String, Object> one = new HashMap<>(2);
            String remarkFieldName = getRemarkFiledNameBySearchType(type);
            one.put("text", remarkFieldName);
            one.put("nodes", Arrays.asList(
                    new Document().append("text", searchNum).append("type", type)));
            result.add(one);
            return result;
        }
        //对于能搜索到人的信息时
        List<String> qqUins = new ArrayList<>();
        List<String> wxUins = new ArrayList<>();
        List<String> phones = new ArrayList<>();
        List<String> idNumbers = new ArrayList<>();
        persons.forEach(t -> {
            String idNumber = t.getString("personIdCard");
            if (!StringUtils.isEmpty(idNumber)) {
                if (!idNumbers.contains(idNumber)) {
                    idNumbers.add(idNumber);
                }
            }
            Object personPhones = t.get("phoneNUM");
            addNumbers(phones, personPhones);
            Object qqs = t.get("QQNUM");
            addNumbers(qqUins, qqs);
            Object wxs = t.get("WXNUM");
            addNumbers(wxUins, wxs);
        });
        if (!CollectionUtils.isEmpty(idNumbers)) {
            result.add(getRemarkNodexByNumbersAndType(idNumbers, "papers"));
        }
        if (!CollectionUtils.isEmpty(qqUins)) {
            result.add(getRemarkNodexByNumbersAndType(qqUins, "qq"));
        }
        if (!CollectionUtils.isEmpty(wxUins)) {
            result.add(getRemarkNodexByNumbersAndType(wxUins, "wx"));
        }
        if (!CollectionUtils.isEmpty(phones)) {
            result.add(getRemarkNodexByNumbersAndType(phones, "phone"));
        }
        return result;
    }

    /**
     * 添加号码到集合中
     */
    private void addNumbers(List<String> phones, Object personPhones) {
        if (personPhones != null) {
            ArrayList<?> onePersonPhones = (ArrayList) personPhones;
            if (!CollectionUtils.isEmpty(onePersonPhones)) {
                onePersonPhones.stream().filter(Objects::nonNull).filter(t1 -> !phones.contains(t1.toString())).forEach(t1 -> phones.add(t1.toString()));
            }
        }
    }

    /**
     * 通过对应账号以及搜索类型获取其对应的节点信息
     *
     * @param iDNumbers 对应的多个账号
     * @param string    对应的类型
     */
    private Map<String, Object> getRemarkNodexByNumbersAndType(List<String> iDNumbers, String string) {
        Map<String, Object> one = new HashMap<>(2);
        if (!CollectionUtils.isEmpty(iDNumbers)) {
            List<Document> numbers;
            one.put("text", getRemarkFiledNameBySearchType(string));
            numbers = iDNumbers.stream().map(t -> new Document().append("text", t).append("type", string)).collect(Collectors.toCollection(LinkedList::new));
            one.put("nodes", numbers);
        }
        return one;
    }

    private String getRemarkFiledNameBySearchType(String type) {
        String remarkFieldName = "";
        switch (type) {
            case "qq":
                remarkFieldName = "QQ账号";
                break;
            case "wx":
                remarkFieldName = "微信账号";
                break;
            case "phone":
                remarkFieldName = "手机账号";
                break;
            case "papers":
                remarkFieldName = "个人信息";
                break;
            default:
                break;
        }
        return remarkFieldName;
    }

    @Override
    public Document findPersonBaseInfoByUNumber(String num) {
        //备注中查询对应的人员基本信息
        if (StringUtils.isEmpty(num)) {
            throw new RuntimeException("对应人员的身份证号为空");
        }
        Document result = new Document();
        BasicDBObject query = new BasicDBObject("usernumber", num.trim());
        db = mongoClient.getDatabase("infoData2");
        collection = db.getCollection("t_person");
        List<Document> persons = new ArrayList<>();
        FindIterable<Document> ite = collection.find(query).skip(0).limit(1);
        boolean present = Optional.ofNullable(ite).isPresent();
        if (present) {
            ite.iterator().forEachRemaining(persons::add);
        }
        if (!CollectionUtils.isEmpty(persons)) {
            result = persons.get(0);
        }
        return result;
    }

    /**
     * 对对应的备注进行解密
     */
    private static String recodeRemark(String remark) {
        if (StringUtils.isEmpty(remark)) {
            return "";
        }
        byte[] value;
        Base64 base = new Base64();
        try {
            value = base.decode(remark);
            remark = new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            remark = "";
        }
        return remark;
    }
}
