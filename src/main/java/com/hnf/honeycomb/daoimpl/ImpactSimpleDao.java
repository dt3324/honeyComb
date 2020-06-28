package com.hnf.honeycomb.daoimpl;


import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.mapper.DeviceMapper;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.util.CollectionUtils;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hnf.honeycomb.util.ObjectUtil.getString;
import static com.hnf.honeycomb.util.StringUtils.legalString;

/**
 * @author lsj
 */
@Component
public class ImpactSimpleDao {

    private static Logger logger = LoggerFactory.getLogger(ImpactSimpleDao.class);
    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient;
    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient212;
    @Autowired
    private DeviceMapper deviceMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    /**
     * 调用时才new 对象 效率极低
     * 改为注入方式,启动程序时创建好对象
     */
    public List<DBObject> impactByNumsAndType(List<String> mainQQNumbers, String type) {
        DBCollection coll;
        DB db;
        String queryNum;
        String queryFNum;
        db = mongoClient212.getDB("infoData2");
        switch (type) {
            case "QQ":
                queryNum = "uin";
                queryFNum = "fuin";
                coll = db.getCollection("t_qquser_friend");
                break;
            case "WX":
                queryNum = "username";
                queryFNum = "fusername";
                coll = db.getCollection("t_wxuser_friend");
                break;
            case "PHONE":
                queryNum = "phonenumself";
                queryFNum = "phonenum";
                coll = db.getCollection("t_contact_phonenum");
                break;
            default:
                return null;
        }

        BasicDBObject query = new BasicDBObject(queryNum,
                new BasicDBObject(QueryOperators.IN, mainQQNumbers.toArray(new String[]{})));
        logger.debug("query:" + query);
        logger.info("query: " + query);
        String mapStr = "function (){  \n" + "	emit(this." + queryFNum + ",this." + queryNum + ");  \n" + "};";
        String reduceStr = "function (key, values){\n"
                + "	return {rst:values};\n"
                + "};";

        String finalizeStr = "function(key, reducedVal) {\n" + "	"
                + "var hs = reducedVal.rst;\n"
                + "	if(typeof(hs) != \"undefined\"){\n"
                + "         return hs;\n"
                + "     }\n"
                + "};";

        // 取代之前的集合
        MapReduceCommand cmd = new MapReduceCommand(coll, mapStr, reduceStr, null,
                // 生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE,
                query);
        cmd.setFinalize(finalizeStr);
        MapReduceOutput out = coll.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new LinkedList();
        getResultToList(ite, result);
        return result;
    }

    /**
     * 把value 变成数组的方式储存
     * 因为返回的数据结构有多层嵌套、这里用递归去读取处理，重写放入value list
     *
     * @param ite
     * @param result
     */
    private void getResultToList(Iterable<DBObject> ite, List<DBObject> result) {
        ite.forEach(t -> {
            //logger.debug("t>>>>>>>>>>>:"+t);
            if (t.get("value") != null) {
                // 碰撞后主控qq与共同好友有关系的主控号码集合
                //神逻辑？？？？？？？？？？？
                BasicDBList aList = (BasicDBList) toNeedBasicDBList((t.get("value")));
                t.put("value", aList);
                result.add(t);
            }
        });
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
            /**
             * 由于Map reduce 的结果有嵌套结果集 rst 所以需要递归调用，取出数组中的qq 封装到一个数组中
             */
            if (a instanceof BasicDBObject) {
                BasicDBObject aObject = (BasicDBObject) a;
                BasicDBList aaa = toNeedBasicDBList(aObject.get("rst"));
                result.addAll(aaa);
            }
        }
        return result;
    }

    public List<DBObject> impactQQtroopByMainQQs(List<String> mainQQNumbers) {
        DBCollection coll2;
        DB db;
        db = mongoClient.getDB("infoData2");
        coll2 = db.getCollection("t_troop_qquser");
        BasicDBObject query = new BasicDBObject("uin",
                new BasicDBObject(QueryOperators.IN, mainQQNumbers.toArray(new String[]{})));
        String mapStr = "function (){  \n" + "	emit(this." + "troopuin" + ",this." + "uin" + ");  \n" + "};";

        String reduceStr = "function (key, values){\n" + "	return {rst:values};\n" + "};";

        String finalizeStr = "function(key, reducedVal) {\n" + "	var hs = reducedVal.rst;\n"
                + "	if(typeof(hs) != \"undefined\"){\n" + "         var rst = hs;\n" + "         return rst;\n"
                + "     }\n" + "};";

        MapReduceCommand cmd = new MapReduceCommand(coll2, mapStr, reduceStr, null, // 生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE, // 取代之前的集合
                query);
        cmd.setFinalize(finalizeStr);
        MapReduceOutput out = coll2.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new LinkedList();
        ite.forEach(t -> {
            if (t.get("value") != null && t.get("_id") != null) {
                result.add(t);
            }
        });
        return result;
    }

    public List<DBObject> impactWXtroopByMainWXs(List<String> mainWXNumbers) {
        DBCollection coll2;
        DB db;
        db = mongoClient.getDB("infoData2");
        coll2 = db.getCollection("t_chatroom_wxuser");
        BasicDBObject query = new BasicDBObject("username",
                new BasicDBObject(QueryOperators.IN, mainWXNumbers.toArray(new String[]{})));
        String mapStr = "function (){  \n" + "	emit(this." + "chatroomname" + ",this." + "username" + ");  \n" + "};";

        String reduceStr = "function (key, values){\n" + "	return {rst:values};\n" + "};";

        String finalizeStr = "function(key, reducedVal) {\n" + "	var hs = reducedVal.rst;\n"
                + "	if(typeof(hs) != \"undefined\"){\n" + "         var rst = hs;\n" + "         return rst;\n"
                + "     }\n" + "};";

        MapReduceCommand cmd = new MapReduceCommand(coll2, mapStr, reduceStr, null, // 生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE, // 取代之前的集合
                query);
        cmd.setFinalize(finalizeStr);
        // cmd.setFinalize(finalzStr + recursionStr);
        MapReduceOutput out = coll2.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new LinkedList();
        ite.forEach(t -> {
            if (t.get("value") != null) {

                result.add(t);
            }
        });
        return result;
    }


    /**
     * //通过设备唯一标识查询 此设备的机主号码  和  通讯录号码
     *
     * @param device_unique
     * @return
     */
    public List<Document> findPhoneBydevice(String device_unique) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (device_unique != null) {
            Document query = new Document();
            query.append("device_unique", device_unique);
            mongoDatabase = mongoClient.getDatabase("infoData2");
            //collection = mongoDatabase.getCollection("t_contact_phonenum");
            collection = mongoDatabase.getCollection("t_person");
            // 将最后转化成的json放到list里
            List<Document> list = new ArrayList<Document>();
            // 查询到结果
            FindIterable<Document> resultDoc = collection.find(query);
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    //查询设备的详细信息
    public List<Document> deviceFindOne(String deviceUnique) {
        Document query = new Document("device_unique", deviceUnique);
        // 将最后转化成的json放到list里
        List<Document> list = new ArrayList<>();
        // 查询到结果
        FindIterable<Document> resultDoc
                = mongoClient.getDatabase("infoData2").getCollection("t_device").find(query);
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }


    public List<DBObject> impactByMainPhonesIsNotFriend2Record(List<String> allPhones,
                                                               List<String> deviceUniques, BasicDBObject timeQuery) {
        DBCollection coll4;
        DB db;
        db = mongoClient.getDB("infoData");
        coll4 = db.getCollection("record");
        BasicDBObject query = new BasicDBObject("phonenum",
                new BasicDBObject(QueryOperators.NIN, allPhones.toArray(new String[]{}))).append("deviceUnique",
                new BasicDBObject(QueryOperators.IN, deviceUniques.toArray(new String[]{})));
        //String mapStr = "function (){  \n" + "	emit(this." + "phonenum" + ",this." + "deviceUnique" + ");  \n" + "};";
        if (timeQuery != null
                && !timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        String mapStr = "function () {\n" +
                "     emit(this.phonenum,{\"deviceUnique\":this.deviceUnique,cnt:1})\n" +
                "}\n";
        // String reduceStr = "function (key, values){\n" + "	return {rst:values};\n" + "};";
        String reduceStr = "function (key, values) {\n" +
                "    var count = 0;\n" +
                "     values.forEach(t=>{\n" +
                "  \t \tcount += t.cnt;\n" +
                "      })\n" +
                "   return ( {rst: values, \"count\": count} );\n" +
                "}\n";
        String finalizeStr = "function (key, reducedValue) {\n" +
                "   var hs = reducedValue.rst;\n" +
                "   var count = reducedValue.count;\n" +
                "   if(typeof(hs)!=\"undefined\"){\n" +
                "   \tvar rst = hs;\n" +
                "   \treturn {\"rst\":rst,\"count\":count};\n" +
                "   }\n" +
                "}\n";
        // 生成的集合名对应时间，以免重复
        MapReduceCommand cmd = new MapReduceCommand(coll4, mapStr, reduceStr, null,
                // 取代之前的集合
                MapReduceCommand.OutputType.INLINE,
                query);
        cmd.setFinalize(finalizeStr);
        // cmd.setFinalize(finalzStr + recursionStr);
        MapReduceOutput out = coll4.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        // ite.forEach(t -> {
        // //logger.debug("t:" + t);
        // });
        List<DBObject> result = new LinkedList();
        ite.forEach(t -> {
            if (t.get("value") != null) {
                //logger.debug("aaaaaaaaaaaaaaaaaaaaaa:"+t);
                result.add(t);
            }
        });
        return result;
    }

//    public List<Document> impactByMainPhones(List<String> mainPhones) {
//        BasicDBObject query = new BasicDBObject("PHNUM",
//                new BasicDBObject(QueryOperators.IN, mainPhones.toArray(new String[]{}))).append("PHFNUM",
//                new BasicDBObject(QueryOperators.IN, mainPhones.toArray(new String[]{})));
//        mongoDatabase = mongoClient.getDatabase("QQREMcs");
//        collection = mongoDatabase.getCollection("PHREM");
//        List<Document> list = new ArrayList<>(); // 将最后转化成的json放到list里
//        FindIterable<Document> resultDoc = collection.find(query); // 查询到结果
//        //PHREM 字段 为机主号码  PHFREM为通讯录号码
//        for (Document d : resultDoc) {
//            list.add(d);
//        }
//        return list;
//
//    }

    public List<DBObject> impactByMainPhonesMapReduce(List<String> mainPhones) {
        DBCollection coll3;
        DB db;
        db = mongoClient212.getDB("infoData2");
        coll3 = db.getCollection("t_contact_phonenum");
        BasicDBObject query = new BasicDBObject("phonenumSelf", new BasicDBObject(QueryOperators.IN, mainPhones.toArray(new String[]{})));
        String mapStr = "function (){  \n"
                + "	emit(this." + "phonenum" + ",this." + "phonenumSelf" + ");  \n"
                + "};";

        String reduceStr = "function (key, values){\n"
                + "	return {rst:values};\n"
                + "};";

        String finalizeStr = "function(key, reducedVal) {\n"
                + "	var hs = reducedVal.rst;\n"
                + "	if(typeof(hs) != \"undefined\"){\n"
                + "         var rst = hs;\n"
                + "         return rst;\n"
                + "     }\n"
                + "};";

        MapReduceCommand cmd = new MapReduceCommand(
                coll3,
                mapStr,
                reduceStr,
                null, //生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE, //取代之前的集合
                query);
        cmd.setFinalize(finalizeStr);
        //logger.debug("查询条件:"+query);
        //	        cmd.setFinalize(finalzStr + recursionStr);
        MapReduceOutput out = coll3.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        //	        ite.forEach(t -> {
        //	            //logger.debug("t:" + t);
        //	        });
        List<DBObject> result = new LinkedList();
        getResultToList(ite, result);
        return result;
    }

    /**
     * 获取设备相应的虚拟号码
     *
     * @param type        查询账号类型
     * @param uniquesList 设备unique List
     * @return
     */
    public List<Document> extractNumbersByType(String type, List<String> uniquesList) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> list = new LinkedList<>();
        switch (type) {
            case "QQ":
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_qquser");
                BasicDBObject query = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, uniquesList.toArray(new String[]{})));
                if (uniquesList.size() > 0) {
                    FindIterable<Document> callBack = collection.find(query);
                    for (Document d : callBack) {
                        list.add(d);
                    }
                }
                break;
            case "WX":
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_wxuser");
                BasicDBObject querywx = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, uniquesList.toArray(new String[]{})));
                if (uniquesList.size() > 0) {
                    FindIterable<Document> callBack = collection.find(querywx);
                    for (Document d : callBack) {
                        list.add(d);
                    }
                }
                break;
            case "PHONE":
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_contact_phonenum");
                BasicDBObject queryPhone = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, uniquesList.toArray(new String[]{})));
                if (uniquesList.size() > 0) {
                    FindIterable<Document> callBack = collection.find(queryPhone);
                    for (Document d : callBack) {
                        list.add(d);
                    }
                }
                break;
            default:
        }
        return list;
    }

    public List<Document> deviceFindByDeviceUnique(List<String> list) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> listResult = new LinkedList();
        BasicDBObject query = new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, list.toArray(new String[]{})));
        // list = Common.commonFind(query, "t_device");
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_device");
        //将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
        for (Document d : resultDoc) {
            listResult.add(d);
        }
        return listResult;
    }

    public List<Document> findDeviceByName(String deviceName) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> list = new LinkedList();
        if (deviceName != null) {
            BasicDBObject query = new BasicDBObject();
            Pattern pattern = Pattern.compile("^.*" + deviceName + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("devicename", pattern);
            //list = Common.commonFind(query, "t_device");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_device");
            //将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
        }
        return list;
    }

    public List<Document> personFindOne(String deviceUniques) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = new Document();
        query.append("device_unique", deviceUniques);
        //         listResult = Common.commonFind(query, "t_person");
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_person");
        List<Document> list = new ArrayList<Document>();            //将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> personFindOneNew(String usernumber) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = new Document();
        query.append("usernumber", usernumber);
        //         listResult = Common.commonFind(query, "t_person");
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_person");
        List<Document> list = new ArrayList<Document>();            //将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> qquser(String deviceUnique) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = new Document();
        query.append("device_unique", deviceUnique);
        // listResult = Common.commonFind(query, "t_qquser");.
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_qquser");
        List<Document> list = new ArrayList<Document>();            //将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> wxuser(String deviceUnique) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = new Document();
        query.append("device_unique", deviceUnique);
        //listResult = Common.commonFind(query, "t_wxuser");
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_wxuser");
        List<Document> list = new ArrayList<Document>();            //将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> findInfoByGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query, BasicDBObject sort, Integer page, Integer pageSize) {
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new RuntimeException("对应的数据名名为空");
        }
        if (gatherName == null || gatherName.trim().isEmpty()) {
            throw new RuntimeException("对应的集合名为空");
        }
        if (sort != null && !sort.isEmpty()) {
            if (page == null || pageSize == null) {// 此时在程序中默认为不分页
                return findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, sort);
            }
            return findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, sort, page, pageSize);

        }
        if (page == null || pageSize == null) {// 此时在程序中默认为不分页
            return findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query);
        }
        return findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, page, pageSize);
    }

    public List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query, BasicDBObject sort) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> result = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase(dbName);
        collection = mongoDatabase.getCollection(gatherName);
        FindIterable<Document> ite = collection.find(query).sort(sort);
        for (Document doc : ite) {
            result.add(doc);
        }
        return result;
    }

    public List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                                BasicDBObject sort, Integer page, Integer pageSize) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> result = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase(dbName);
        collection = mongoDatabase.getCollection(gatherName);
        FindIterable<Document> ite = collection.find(query).sort(sort).skip((page - 1) * pageSize).limit(pageSize);
        for (Document doc : ite) {
            result.add(doc);
        }
        return result;
    }

    public List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> result = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase(dbName);
        collection = mongoDatabase.getCollection(gatherName);
        FindIterable<Document> ite = collection.find(query);
        for (Document doc : ite) {
            result.add(doc);
        }
        return result;
    }

    public List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query, Integer page, Integer pageSize) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> result = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase(dbName);
        collection = mongoDatabase.getCollection(gatherName);
        FindIterable<Document> ite = collection.find(query).skip((page - 1) * pageSize).limit(pageSize);
        for (Document doc : ite) {
            result.add(doc);
        }
        return result;
    }

    public List<Document> groupInfoBySomeTerms(String dbName, String gatherName, List<BasicDBObject> groups) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new RuntimeException("对应的数据库名为空");
        }

        if (gatherName == null || gatherName.trim().isEmpty()) {
            throw new RuntimeException("对应的集合名为空");
        }
        if (groups == null || groups.isEmpty()) {
            throw new RuntimeException("对应的聚合条件为空");
        }
        List<Document> result = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase(dbName);
        collection = mongoDatabase.getCollection(gatherName);
        AggregateIterable<Document> iterable = collection.aggregate(groups).allowDiskUse(true);
        for (Document d : iterable) {
            result.add(d);
        }
        return result;


    }

    public Map countRecord(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$deviceUnique")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "record", Arrays.asList(new BasicDBObject("$match", query), group));
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("record");
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;
    }

    public Map countMessage(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$deviceUnique")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "message", Arrays.asList(new BasicDBObject("$match", query), group));
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("message");

        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;
    }

    public List<Document> findqq(String uin) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (uin != null) {
            Document query = new Document();
            query.append("uin", uin);
            //listResult = Common.commonFind(query, "t_qquser");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_qquser");
            List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    public List<Document> qquserFriend(String uin, String fuin) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (uin != null) {
            Document query = new Document();
            query.append("uin", uin);
            query.append("fuin", fuin);
            //listResult = Common.commonFind(query, "t_qquser_friend");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_qquser_friend");
            List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    public Map countQQFriendMsg(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmsg_mark")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "qqmsg", Arrays.asList(new BasicDBObject("$match", query), group));
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("qqmsg");
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;

    }

    public List<Document> findQQTroop(String troopuin) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (troopuin != null) {
            Document query = new Document();
            query.append("troopuin", troopuin);
            //listResult = Common.commonFind(query, "t_qq_troop");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_qq_troop");
            List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    public Map countQQTroopMsg(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$troopuin")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "qqTroopMsg", Arrays.asList(new BasicDBObject("$match", query), group));

        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("qqTroopMsg");
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;
    }

    public List<Document> wxuserFriend(String username, String fusername) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (username != null) {
            Document query = new Document();
            query.append("username", username);
            query.append("fusername", fusername);
            //listResult = Common.commonFind(query, "t_wxuser_friend");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_wxuser_friend");
            List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    public List<Document> findwx(String username) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = null;
        if (username != null) {
            query = new Document();
            query.append("username", username);
        }
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_wxuser");

        List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }

        return list;

    }

    public Map countWxFriendMsg(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$c2cmark")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "wxmsg", Arrays.asList(new BasicDBObject("$match", query), group));
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("wxmsg");

        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }

        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;
    }

    public List<Document> findwxchatroom(String chatroomname) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (chatroomname != null) {
            Document query = new Document();
            query.append("chatroomname", chatroomname);
            //listResult = Common.commonFind(query, "t_wxchatroom");
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_wxchatroom");
            List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
            FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }

    public Map countWxTroopMsg(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, Integer> result = new HashMap<>();
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$chatroomname")
                .append("count", new BasicDBObject("$sum", 1))
        );
        //return Common.aggregateFriendMsg("infoData", "wxChatroomMsg", Arrays.asList(new BasicDBObject("$match", query), group));
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("wxChatroomMsg");

        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(new BasicDBObject("$match", query), group)).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        if (!CollectionUtils.isEmpty(results)) {
            results.forEach(t -> {
                result.put(t.getString("_id"), t.getInteger("count"));
            });
        }
        return result;
    }

    public List<Document> qquserFriend(String uin) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = null;
        if (uin != null) {
            query = new Document();
            query.append("uin", uin);
            //listResult = Common.commonFind(query, "t_qquser_friend");
        }
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_qquser_friend");
        List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public Map<String, String> findNickAndUin(List<String> qqUins) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, String> results = new HashMap<>();
        BasicDBObject query = new BasicDBObject("uin", new BasicDBObject(QueryOperators.IN, qqUins.toArray(new String[]{})));
        BasicDBObject fileds = new BasicDBObject("uin", 1).append("nickname", 1).append("_id", 0);
        //return Common.findSomeFileds("infoData2", "t_qquser", query, fileds);
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_qquser");
        List<Document> result = new ArrayList<>();
        //        //logger.debug("query:" + query);
        FindIterable<Document> ite = collection.find(query)
                .projection(fileds);
        for (Document doc : ite) {
            result.add(doc);
        }
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(t -> {
                results.put(t.getString("uin"), t.getString("nickname"));
            });
        }
        return results;
    }

    public Map<String, String> findWXUin2Nick(List<String> wxUins) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, String> results = new HashMap<>();
        BasicDBObject query = new BasicDBObject("username", new BasicDBObject(QueryOperators.IN, wxUins.toArray(new String[]{})));
        BasicDBObject fileds = new BasicDBObject("username", 1).append("nickname", 1).append("_id", 0);
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_wxuser");
        List<Document> result = new ArrayList<>();
        //        //logger.debug("query:" + query);
        FindIterable<Document> ite = collection.find(query)
                .projection(fileds);
        for (Document doc : ite) {
            result.add(doc);
        }
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(t -> {
                results.put(t.getString("username"), t.getString("nickname"));
            });
        }
        return results;
    }

    public List<Document> troopQquser(String uin) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = null;
        if (uin != null) {
            query = new Document();
            query.append("uin", uin);
        }
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_troop_qquser");
        List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public Map<String, String> getTroopUinToTroopName(List<String> troopUins) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, String> map = new HashMap<>();
        BasicDBObject query = new BasicDBObject("troopuin", new BasicDBObject(QueryOperators.IN, troopUins.toArray(new String[]{})));
        BasicDBObject fileds = new BasicDBObject("troopuin", 1).append("troopname", 1).append("_id", 0);
        //return Common.findSomeFileds("infoData2", "t_qq_troop", query, fileds);
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_qq_troop");
        List<Document> result = new ArrayList<>();
        //        //logger.debug("query:" + query);
        FindIterable<Document> ite = collection.find(query)
                .projection(fileds);
        for (Document doc : ite) {
            result.add(doc);
        }
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(t -> {
                map.put(t.getString("troopuin"), t.getString("troopname"));
            });
        }
        return map;
    }

    public List<Document> wxuserFriend(String username) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = null;
        if (username != null) {
            query = new Document();
            query.append("username", username);
            //listResult = Common.commonFind(query, "t_wxuser_friend");
        }

        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_wxuser_friend");
        //ListResult listResult = new ListResult();
        List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> chatroomWxuser(String username) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Document query = null;
        if (username != null) {
            query = new Document();
            query.append("username", username);
            //listResult = Common.commonFind(query, "t_chatroom_wxuser");
        }
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_chatroom_wxuser");
        //ListResult listResult = new ListResult();
        List<Document> list = new ArrayList<Document>();            // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = collection.find(query);    // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public Map<String, String> getChatroomUin2TroopName(List<String> chatroomUins) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        Map<String, String> results = new HashMap<>();
        BasicDBObject query = new BasicDBObject("chatroomname", new BasicDBObject(QueryOperators.IN, chatroomUins.toArray(new String[]{})));
        BasicDBObject fileds = new BasicDBObject("chatroomname", 1).append("chatroomnickname", 1).append("_id", 0);
        //return Common.findSomeFileds("infoData2", "t_wxchatroom", query, fileds);
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_wxchatroom");
        List<Document> result = new ArrayList<>();
        //        //logger.debug("query:" + query);
        FindIterable<Document> ite = collection.find(query)
                .projection(fileds);
        for (Document doc : ite) {
            result.add(doc);
        }
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(t -> {
                results.put(t.getString("chatroomname"), t.getString("chatroomnickname"));
            });
        }
        return results;
    }

    public List<DBObject> impactByAllPhoneIsNotFriend2Message(List<String> allPhones, List<String> deviceUniques, BasicDBObject timeQuery) {
        DBCollection coll5;
        DB db;
        db = mongoClient.getDB("infoData");
        coll5 = db.getCollection("message");
        BasicDBObject query = new BasicDBObject("phonenum",
                new BasicDBObject(QueryOperators.NIN, allPhones.toArray(new String[]{}))).append("deviceUnique",
                new BasicDBObject(QueryOperators.IN, deviceUniques.toArray(new String[]{})));
        //String mapStr = "function (){  \n" +
        //      "	emit(this." + "phonenum" + ",this." + "deviceUnique" + ");  \n"
        //    + "};";
        if (timeQuery != null && !timeQuery.isEmpty()) {
            query.append("time", timeQuery);
        }
        String mapStr = "function () {\n" +
                "     emit(this.phonenum,{\"deviceUnique\":this.deviceUnique,cnt:1})\n" +
                "}";


//        String reduceStr = "function (key, values){\n" +
//                "	return {rst:values};\n" +
//                "};";
        String reduceStr = "function (key, values) {\n" +
                "    var count = 0;\n" +
                "     values.forEach(t=>{\n" +
                "  \t \tcount += t.cnt;\n" +
                "      })\n" +
                "   return ( {rst: values, \"count\": count} );\n" +
                "}";

//        String finalizeStr = "function(key, reducedVal) {\n" +
//                "	var hs = reducedVal.rst;\n"
//                + "	if(typeof(hs) != \"undefined\"){\n" +
//                "         var rst = hs;\n" +
//                "         return rst;\n"
//                + "     }\n" + "};";
        String finalizeStr = "function (key, reducedValue) {\n" +
                "   var hs = reducedValue.rst;\n" +
                "   var count = reducedValue.count;\n" +
                "   if(typeof(hs)!=\"undefined\"){\n" +
                "   \tvar rst = hs;\n" +
                "   \treturn {\"rst\":rst,\"count\":count};\n" +
                "   }\n" +
                "}";

        MapReduceCommand cmd = new MapReduceCommand(coll5, mapStr, reduceStr, null, // 生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE, // 取代之前的集合
                query);
        cmd.setFinalize(finalizeStr);
        // cmd.setFinalize(finalzStr + recursionStr);
        MapReduceOutput out = coll5.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        // ite.forEach(t -> {
        // //logger.debug("t:" + t);
        // });
        List<DBObject> result = new LinkedList();
        ite.forEach(t -> {
            if (t.get("value") != null) {

                result.add(t);
            }
        });
        return result;
    }

    /**
     * //通过设备唯一标识查询 此设备的机主号码  和  通讯录号码（通讯录好友的手机号码）
     *
     * @param deviceUnique
     * @return
     */
    public List<Document> findContactPhoneByDeviceUnqiue(String deviceUnique) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        if (deviceUnique != null) {
            Document query = new Document();
            query.append("device_unique", deviceUnique);
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_contact_phonenum");
            // 将最后转化成的json放到list里
            List<Document> list = new ArrayList<Document>();
            // 查询到结果
            FindIterable<Document> resultDoc = collection.find(query);
            for (Document d : resultDoc) {
                list.add(d);
            }
            return list;
        }
        return null;
    }


    /**
     * 通过对应的条件对对应的结果进行过滤
     *
     * @param count 所需的消息条数
     * @param query 对应的筛选条件k
     * @return 返回统计的结果
     */
    public List<Document> filterImpactQQTroopResultByCountAndQuery(Integer count, BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> resultList = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase("infoData");
        collection = mongoDatabase.getCollection("qqTroopMsg");
        logger.debug("query:" + query);
        BasicDBObject match = new BasicDBObject("$match", query);
        BasicDBObject group = new BasicDBObject("$group",
                new BasicDBObject("_id", new BasicDBObject("uin", "$troopuin").append("fuin", "$senderuin"))
                        .append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", count)));
        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
        for (Document doc : result) {
            resultList.add(doc);
        }
        return resultList;
    }

    ;


    //通过案件或者人员查询人员
    public List<Document> findPersonByPersonOrCase(BasicDBObject query) {
        MongoDatabase mongoDatabase;
        MongoCollection<Document> collection;
        List<Document> list = new ArrayList<>();
        mongoDatabase = mongoClient.getDatabase("infoData2");
        collection = mongoDatabase.getCollection("t_person");
        BasicDBObject sort = new BasicDBObject("_id", -1);
        FindIterable<Document> resultDoc = collection.find(query).sort(sort); // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }


    //通过设备唯一标识查设备详情
    public List<Document> findDeviceByUnique(BasicDBObject query) {
        FindIterable<Document> resultDoc = mongoClient.getDatabase("infoData2")
                .getCollection("fetchlog").find(query).sort(new BasicDBObject("fetchtime",-1)).limit(1);
        List<Document> list = new ArrayList<>();
        for (Document d : resultDoc) {
            String departmentName = d.getString("department_name");
            if (departmentName == null || "".equals(departmentName)) {
                String departmentCode = d.getString("department_code");
                DepartmentBean departmentBean = departmentMapper.findByDepartmentCode(departmentCode);
                if (departmentBean == null) {
                    throw new RuntimeException(departmentCode + " 不存在，请先添加该部门");
                }
                d.put("department_name", departmentBean.getDepartmentName());
            }
            list.add(d);
        }
        return list;
    }

    //通过设备唯一标识查
    public List<String> findDeviceByUnique(String device) {
        List<String> l = deviceMapper.findDepartmentCodeByUnique(device);
        String departmentCode = l.get(0);
        List<String> list = new ArrayList<>();
        if(departmentCode != null){
            DepartmentBean departmentBean = departmentMapper.findByDepartmentCode(departmentCode);
            if (departmentBean == null) {
                    throw new RuntimeException(departmentCode + " 不存在，请先添加该部门");
                }
            list.add(departmentBean.getDepartmentName());
        }
        return list;
    }

    /**
     * 通过多个设备唯一标识查询其对应的人员姓名以及身份证号
     *
     * @param deviceUniques 对应的多个设备唯一标识
     * @return 返回查询的身份证号  设备对应的 人员名成和身份证号码 ,以及手机号码
     */
    public Map<String, String[]> findDeviceUnique2PersonNameAndNumber(String[] deviceUniques) {
        Map<String, String[]> unique2PersonNameAndNumber = new HashMap<>(deviceUniques.length);
        if(deviceUniques.length == 0){
            return unique2PersonNameAndNumber;
        }
        FindIterable<Document> allDevices
                = mongoClient.getDatabase("infoData2").getCollection("t_person")
                .find(
                        new BasicDBObject("device_unique", new BasicDBObject(QueryOperators.IN, Arrays.asList(deviceUniques)))
                                .append("phone", new BasicDBObject("$exists", 1))
                );
        List<String> empty = new ArrayList<>();
        for (Document doc : allDevices) {
            List<String> findDeviceUniques = doc.get("device_unique", empty).stream().filter(
                    r-> Arrays.asList(deviceUniques).contains(r)
            ).collect(Collectors.toList());
            List<String> phones = doc.get("phone", empty);
            String[] nameCardPhones = new String[2 + phones.size()];
            //前两位存名字和身份证号码， 剩下的位置存电话号码
            nameCardPhones[0] = getString(doc.getString("personname"), " ");
            nameCardPhones[1] = getString(doc.getString("usernumber"), " ");
            for (int i = 0; i < phones.size(); i++) {
                nameCardPhones[i + 2] = phones.get(i);
            }
            //设备相应人员的名称和号码存入map
            findDeviceUniques.forEach(t ->
                    unique2PersonNameAndNumber.put(t, nameCardPhones)
            );

        }
        return unique2PersonNameAndNumber;
    }

        /**
         * 对碰撞出的WX结果进行再次筛选
         *
         * @param countLimit  对应的限制条件
         * @param filterQuery 对应的过滤条件
         * @return 返回对应的记过
         */
        public List<Document> filterImpactWXTroopResultByCountAndQuery (Integer countLimit, BasicDBObject filterQuery){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> resultList = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("wxChatroomMsg");
            BasicDBObject match = new BasicDBObject("$match", filterQuery);
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", new BasicDBObject("uin", "$chatroomname").append("fuin", "$username"))
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            for (Document doc : result) {
                resultList.add(doc);
            }
            return resultList;
        }

        /**
         * 筛选是碰撞出的共同通讯录好友的消息条数以及时间范围
         *
         * @param countLimit  对应的总数限制条件
         * @param filterQuery 对应的过滤条件
         * @return 返回结果
         */
        public List<Document> filterRecordImpactIsFriendResultByCountAndQuery (Integer countLimit,
                BasicDBObject filterQuery){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> resultList = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("record");
            BasicDBObject match = new BasicDBObject("$match", filterQuery);
            //按照设备和好友分组，在sum求和就可以得到该设备与该号码通话的次数。//id 为分组键
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", new BasicDBObject("fuin", "$deviceUnique").append("uin", "$phonenum"))
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            for (Document doc : result) {
                resultList.add(doc);
            }
            return resultList;
        }

        /**
         * 筛选是碰撞出的共同通讯录好友的消息条数以及时间范围
         *
         * @param countLimit  对应的总数限制条件
         * @param filterQuery 对应的过滤条件
         * @return 返回结果
         */
        public List<Document> filterMessageImpactIsFriendResultByCountAndQuery (Integer countLimit,
                BasicDBObject filterQuery){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> resultList = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("message");
            BasicDBObject match = new BasicDBObject("$match", filterQuery);
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", new BasicDBObject("fuin", "$deviceUnique").append("uin", "$phonenum"))
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            for (Document doc : result) {
                resultList.add(doc);
            }
            return resultList;
        }

        /**
         * 对QQ碰撞出的共同好友进行条件筛选
         *
         * @param countLimit  对应的条数限制条件
         * @param filterQuery 对应的过滤范围
         * @return 返回对应的结果
         */
        public List<Document> filterQQMsgImpactIsFriendResultByCountAndQuery ( int countLimit, BasicDBObject filterQuery)
        {
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> resultList = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("qqmsg");
            BasicDBObject match = new BasicDBObject("$match", filterQuery);
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", "$c2cmsg_mark")
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            for (Document doc : result) {
                resultList.add(doc);
            }
            return resultList;
        }

        /**
         * 对QQ碰撞出的共同好友进行条件筛选
         *
         * @param countLimit  对应的条数限制条件
         * @param filterQuery 对应的过滤范围
         * @return 返回对应的结果
         */
        public List<Document> filterWXMsgImpactIsFriendResultByCountAndQuery ( int countLimit, BasicDBObject filterQuery)
        {
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> resultList = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("wxmsg");
            BasicDBObject match = new BasicDBObject("$match", filterQuery);
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", "$c2cmark")
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            for (Document doc : result) {
                resultList.add(doc);
            }
            return resultList;
        }

        /**
         * 直接关系碰撞
         *
         * @param countLimit 联系次数
         * @param filter     查询条件
         * @return
         */
        public List<Document> filterPhoneStraightRalationByCountAndQuery (Integer countLimit, BasicDBObject filter){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> listResult = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("record");
            BasicDBObject match = new BasicDBObject("$match", filter);
            BasicDBObject group = new BasicDBObject("$group",
                    new BasicDBObject("_id", "$phonenum")
                            .append("count", new BasicDBObject("$sum", 1)));
            BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
            BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", countLimit)));
            AggregateIterable<Document> result = collection.aggregate(Arrays.asList(match, group, sort, having));
            result.iterator().forEachRemaining(listResult::add);
            return listResult;
        }

        public List<Document> filterPhoneAndDeviceUniqueStraightRelation (BasicDBObject query, BasicDBObject filter){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            List<Document> listResult = new ArrayList<>();
            mongoDatabase = mongoClient.getDatabase("infoData");
            collection = mongoDatabase.getCollection("record");
            if (null != filter) {
                AggregateIterable<Document> aggregateIterable = collection.aggregate(Arrays.asList(query, filter));
                aggregateIterable.iterator().forEachRemaining(listResult::add);
            }
            FindIterable<Document> findIterable = collection.find(query);
            if (findIterable != null) {
                findIterable.iterator().forEachRemaining(listResult::add);
            }
            return listResult;
        }


        /**
         * 通过多个设备QQ号以及好友QQ号查询对应的备注名
         *
         * @param selfQQUins   对应的设备QQ号
         * @param friendQQUins 对应的好友QQ号
         * @return 返回对应的自己QQ号与好友QQ号与备注名的关系
         */
        public Map<String, String> findQQRemarkBySelfQQUinsAndFriendUins (
                List < String > selfQQUins, List < String > friendQQUins){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            Map<String, String> selfUinAndFriendUin2Remark = new HashMap<>();
            BasicDBObject query = new BasicDBObject();
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_qquser_friend");
            query.append("uin", new BasicDBObject(QueryOperators.IN,
                    selfQQUins)).append("fuin", new BasicDBObject(QueryOperators.IN, friendQQUins));
            FindIterable<Document> ite = collection.find(query);
            for (Document doc : ite) {
                String selfUin = doc.getString("uin");
                String friendUin = doc.getString("fuin");
                String remark = doc.getString("friendremarkname");
                selfUinAndFriendUin2Remark.put(selfUin + "," + friendUin, remark);
            }
            return selfUinAndFriendUin2Remark;
        }


        /**
         * 通过多个设备WX号以及好友WX号查询对应的备注名
         *
         * @param selfWXUins   对应的设备WX号
         * @param friendWXUins 对应的好友WX号
         * @return 返回对应的自己WX号与好友WX号与备注名的关系
         */
        public Map<String, String> findWXRemarkBySelfWXUinsAndFriendUins
        (List < String > selfWXUins, List < String > friendWXUins){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            Map<String, String> selfUinAndFriendUin2Remark = new HashMap<>();
            BasicDBObject query = new BasicDBObject();
            mongoDatabase = mongoClient.getDatabase("infoData2");
            collection = mongoDatabase.getCollection("t_qquser_friend");
            query.append("username", new BasicDBObject(QueryOperators.IN,
                    selfWXUins)).append("fusername", new BasicDBObject(QueryOperators.IN, friendWXUins));
            FindIterable<Document> ite = collection.find(query);
            for (Document doc : ite) {
                String selfUin = doc.getString("username");
                String friendUin = doc.getString("fusername");
                String remark = doc.getString("friendremarkname");
                selfUinAndFriendUin2Remark.put(selfUin + "," + friendUin, remark);
            }
            return selfUinAndFriendUin2Remark;
        }

        /**
         * 通过案件唯一标识查询案件信息
         *
         * @param caseuniquemark 案件唯一标识
         * @return
         */
        public List<Document> finCase (String caseuniquemark){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            if (caseuniquemark != null) {
                Document query = new Document();
                query.append("caseuniquemark", caseuniquemark);
                //listResult = Common.commonFind(query, "t_qquser");
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_case");
                List<Document> list = new ArrayList<Document>();            //将最后转化成的json放到list里
                FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
                for (Document d : resultDoc) {
                    list.add(d);
                }
                return list;
            }
            return null;
        }

        /**
         * 通过设备唯一标识查询案件信息
         *
         * @param deviceUnique 设备唯一标识
         * @return
         */
        public List<Document> finDevice (String deviceUnique){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            if (deviceUnique != null) {
                Document query = new Document();
                query.append("device_unique", deviceUnique);
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_device");
                //将最后转化成的json放到list里
                List<Document> list = new ArrayList<>();
                //查询到结果 t_device表中设备唯一识别号是唯一的
                FindIterable<Document> resultDoc = collection.find(query).limit(1);
                for (Document d : resultDoc) {
                    list.add(d);
                }
                return list;
            }
            return null;
        }

        /**
         * 通过证件号查询人员信息
         *
         * @param usernumber 证件号
         * @return
         */
        public List<Document> finPerson (String usernumber){
            MongoDatabase mongoDatabase;
            MongoCollection<Document> collection;
            if (usernumber != null) {
                Document query = new Document();
                query.append("usernumber", usernumber);
                //listResult = Common.commonFind(query, "t_qquser");
                mongoDatabase = mongoClient.getDatabase("infoData2");
                collection = mongoDatabase.getCollection("t_person");
                List<Document> list = new ArrayList<>();            //将最后转化成的json放到list里
                FindIterable<Document> resultDoc = collection.find(query);    //查询到结果
                for (Document d : resultDoc) {
                    list.add(d);
                }
                return list;
            }
            return null;
        }

        /**
         * 通过证件号查询人员信息
         *
         * @param userNumber 证件号
         * @return
         */
        public List<Document> finPersons (List<String> userNumber){
            MongoCollection<Document> collection;
            if (userNumber != null && !userNumber.isEmpty()) {
                Document query = new Document();
                query.append("usernumber", new BasicDBObject(QueryOperators.IN,userNumber));
                collection = mongoClient.getDatabase("infoData2").getCollection("t_person");
                //将最后转化成的json放到list里
                List<Document> list = new ArrayList<>();
                //查询到结果
                FindIterable<Document> resultDoc = collection.find(query);
                for (Document d : resultDoc) {
                    if(!list.contains(d)){
                        list.add(d);
                    }
                }
                return list;
            }
            return null;
        }

        /**
         * 短消息直接关系
         *
         * @param devices
         * @param phones
         * @param countLimit
         * @param timeQuery
         * @return
         */
        public List<DBObject> impactByMessageStraight (List < String > devices, List < String > phones, Integer
        countLimit, BasicDBObject timeQuery){
            DBCollection coll4;
            DB db;
            BasicDBObject query = new BasicDBObject();
            countLimit = countLimit != null ? countLimit : 0;
            db = mongoClient.getDB("infoData");
            coll4 = db.getCollection("message");
            query.append("deviceUnique", new BasicDBObject("$in", devices.toArray(new String[]{})))
                    .append("phonenum", new BasicDBObject("$nin", phones.toArray(new String[]{})));
            if (timeQuery != null && !timeQuery.isEmpty()
            ) {
                query.append("time", query);
            }
            String map = "function () {\n" +
                    "\tvar value = {device:this.deviceUnique,cnt:1};\n" +
                    "           emit(this.phonenum, value);\n" +
                    "}\n";
            String reduce = "function (keyPhone, values) {\n" +
                    "\tvar count =0;\n" +
                    "\tvalues.forEach(t=>{\n" +
                    "\t\tcount += t.cnt;\n" +
                    "\t});\n" +
                    "\treturn ({rst:values,\"count\":count})\n" +
                    "                }";

            String finalnize = "function (key, reducedValue) {\n" +
                    "                var  hs = reducedValue.rst;\n" +
                    "                var count = reducedValue.count;\n" +
                    "                if(typeof(hs)!='undefined'){\n" +
                    "                  var rst = hs;\n" +
                    "                   return {\"rst\":rst,\"count\":count};\n" +
                    "                } \n" +
                    "                return {\"rst\":[{\"device\":reducedValue.device}],\"count\":1} \n" +
                    "             \n" +
                    "}\n";
            // 生成的集合名对应时间，以免重复
            MapReduceCommand cmd = new MapReduceCommand(coll4, map, reduce, null,
                    // 取代之前的集合
                    MapReduceCommand.OutputType.INLINE,
                    query);
            cmd.setFinalize(finalnize);
            MapReduceOutput out = coll4.mapReduce(cmd);
            Iterable<DBObject> ite = out.results();
            // ite.forEach(t -> {
            // //logger.debug("t:" + t);
            // });
            List<DBObject> result = new LinkedList();
            Integer finalCountLimit = countLimit;
            ite.forEach(t -> {
                if (t.get("value") != null) {
                    BasicDBObject dbObject = (BasicDBObject) t.get("value");
                    Integer count = dbObject.getInt("count");
                    if (count > finalCountLimit) {
                        result.add(t);
                    }
                }
            });
            return result;
        }

    }



