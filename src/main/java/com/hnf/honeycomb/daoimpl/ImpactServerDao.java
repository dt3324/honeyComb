package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.config.ProjectLevelConstants;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ImpactServerDao {
    private static final Logger logger = LoggerFactory.getLogger(ImpactSimpleDao.class);

    private Map<String, MongoCollection<Document>> establishedCollections;
    private DBCollection coll;

    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    @Lazy
    private MongoClient mongoClient;

    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    @Lazy
    private MongoClient mongoClient212;

    public ImpactServerDao() {
        this.establishedCollections = new LinkedHashMap<>(0b1000, ProjectLevelConstants.CUSTOM_LOAD_FACTOR);
    }

    public long insertHistory(Document query) {
        final MongoCollection<Document> impactDevice;
        impactDevice = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_device");
        try {
            impactDevice.insertOne(query);
        } catch (MongoWriteException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public long insertOldHistory(Document query) {
        final MongoCollection<Document> impactHistory = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_history");
        try {
            impactHistory.insertOne(query);
        } catch (MongoWriteException e) {
            e.printStackTrace();
        }

        return 0;
    }


    public List<Document> findHistory(Document query) {
        final MongoCollection<Document> impactDevice = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_device");
        FindIterable<Document> resultDoc = impactDevice.find(query).sort(new Document("time", -1));
        List<Document> result = new ArrayList<Document>();
        for (Document d : resultDoc) {
            result.add(d);
        }
        return result;

    }

    public List<Document> findOldHistory(Document query) {
        final MongoCollection<Document> impactHistory = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_history");
        FindIterable<Document> resultDoc = impactHistory.find(query).sort(new Document("time", -1));
        List<Document> result = new ArrayList<Document>();
        for (Document d : resultDoc) {
            result.add(d);
        }
        return result;

    }


    public List<Document> findqq(String str) {
        Document query = new Document();
        query.append("uin", str);
        final MongoCollection<Document> qquser = getDocumentMongoCollection(mongoClient, "infoData2", "t_qquser");
        List<Document> list = new ArrayList<Document>(); // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = qquser.find(query); // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> findwx(String str) {
        Document query = new Document();
        query.append("username", str);
        final MongoCollection<Document> wxuser = getDocumentMongoCollection(mongoClient, "infoData2", "t_wxuser");
        List<Document> list = new ArrayList<Document>(); // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = wxuser.find(query); // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> findPersonExtendInfoByDoc(String searchField, BasicDBObject personQuery) {
        List<Document> result = new ArrayList<>();
        logger.debug("mongoClient:" + mongoClient212);
        final MongoCollection<Document> personBaseInfo = getDocumentMongoCollection(mongoClient212, "virtual", "t_personBaseInfo");
        logger.debug("query:" + personQuery);
        FindIterable<Document> resultDoc = personBaseInfo.find(personQuery).skip(0).limit(20); // 查询到结果
        for (Document d : resultDoc) {
            result.add(d);
        }

        if (searchField != null && !searchField.trim().isEmpty() && result != null && !result.isEmpty()) {
            List<Document> newResult = new ArrayList<>();
            String searchContent = personQuery.getString(searchField).replace("^", "").replace(".*$", "");
            result.forEach(t -> {
                String personName = t.getString("personName");
                String personIDNumber = t.getString("personIdCard");
                ArrayList<?> fieldValues = ArrayList.class.cast(t.get(searchField));
                newResult.addAll(fieldValues.stream().map((t1) -> {
                    Document doc = new Document();
                    if (t1.toString().startsWith(searchContent)) {
                        doc.append("personName", personName);
                        doc.append("personIdCard", personIDNumber);
                        doc.append(searchField, t1);
                        return doc;
                    }
                    return null;
                }).filter((t2) -> {
                    return t2 != null;
                }).collect(Collectors.toList()));
            });
            result = newResult;
        }

        return result;
    }

    public Integer updateHistory(Document query, Document newDoc) {
        int count = 0;
        final MongoCollection<Document> impactDevice = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_device");
        BasicDBObject newDocument = new BasicDBObject("$set", newDoc);// 修改的字段
        UpdateResult upodateResult = impactDevice.updateMany(query, newDocument);
        count = (int) upodateResult.getModifiedCount();
        return count;
    }

    public Integer updateOldHistory(Document query, Document newDoc) {
        int count = 0;
        final MongoCollection<Document> impactHistory = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_history");
        BasicDBObject newDocument = new BasicDBObject("$set", newDoc);// 修改的字段
        UpdateResult upodateResult = impactHistory.updateMany(query, newDocument);
        count = (int) upodateResult.getModifiedCount();
        return count;
    }

    public Integer deleteHistory(Document query) {
        int count = 0;
        final MongoCollection<Document> impactDevice = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_device");
        DeleteResult deleteResult = impactDevice.deleteMany(query);
        count = (int) deleteResult.getDeletedCount();
        logger.debug("********" + count + deleteResult);
        return count;
    }

    public Integer deleteOldHistory(Document query) {
        int count = 0;
        final MongoCollection<Document> impactHistory = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_history");
        DeleteResult deleteResult = impactHistory.deleteMany(query);
        count = (int) deleteResult.getDeletedCount();
        logger.debug("********" + count + deleteResult);
        return count;
    }


    public List<DBObject> impactByNumsAndType(ArrayList<String> arrayList, String type) {
        String queryNum = null;
        String queryFNum = null;
        DB db = mongoClient212.getDB("QQREMcs");//FIXME
        switch (type) {
            case "QQ":
                queryNum = "QQNUM";
                queryFNum = "QQFNUM";
                coll = db.getCollection("QQFREM");
                break;
            case "WX":
                queryNum = "WXNUM";
                queryFNum = "WXFNUM";
                coll = db.getCollection("WXFREM");
                break;
            case "PHONE":
                queryNum = "PHNUM";
                queryFNum = "PHFNUM";
                coll = db.getCollection("PHREM");
                break;
            default:
                break;
        }

        BasicDBObject query = new BasicDBObject(queryNum,
                new BasicDBObject(QueryOperators.IN, arrayList.toArray(new String[]{})));
        logger.debug("query:" + query);
        String mapStr = "function (){  \n" + "	emit(this." + queryFNum + ",this." + queryNum + ");  \n" + "};";
        String reduceStr = "function (key, values){\n" + "	return {rst:values};\n" + "};";

        String finalizeStr = "function(key, reducedVal) {\n" + "	var hs = reducedVal.rst;\n"
                + "	if(typeof(hs) != \"undefined\"){\n" + "         var rst = hs;\n" + "         return rst;\n"
                + "     }\n" + "};";
        // MapReduceCommand
        MapReduceCommand cmd = new MapReduceCommand(coll, mapStr, reduceStr, null, // 生成的集合名对应时间，以免重复
                MapReduceCommand.OutputType.INLINE, // 取代之前的集合
                query);
        cmd.setFinalize(finalizeStr);
        // cmd.setFinalize(finalzStr + recursionStr);
        MapReduceOutput out = coll.mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        // ite.forEach(t -> {
        // logger.debug("t:" + t);
        // });
        List<DBObject> result = new LinkedList<>();
        ite.forEach(t -> {
            if (t.get("value") != null) {

                result.add(t);
            }
        });
        return result;
    }

    public List<Document> findImpactMsg(ArrayList<String> arrayList, int i) {
        BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("username",
                new BasicDBObject(QueryOperators.IN, arrayList.toArray(new String[]{}))));
        BasicDBObject group = new BasicDBObject("$group",
                new BasicDBObject("_id", "$fusername").append("count", new BasicDBObject("$sum", 1)));
        BasicDBObject having = new BasicDBObject("$match", new BasicDBObject("count", new BasicDBObject("$gte", i)));
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
        List<BasicDBObject> query = Arrays.asList(match, group, having, sort);
        final MongoCollection<Document> wxuserFriend = getDocumentMongoCollection(mongoClient, "infoData2", "t_wxuser_friend");
        AggregateIterable<Document> iterable = wxuserFriend.aggregate(query).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }

        return results;
    }

    public List<Document> findimpactResultByImpactWXAndMWX(ArrayList<String> arrayList, String impactWX) {
        BasicDBObject query = new BasicDBObject("username",
                new BasicDBObject(QueryOperators.IN, arrayList.toArray(new String[]{}))).append("fusername",
                impactWX.trim());
        final MongoCollection<Document> wxuserFriend = getDocumentMongoCollection(mongoClient, "infoData2", "t_wxuser_friend");
        List<Document> list = new ArrayList<>(); // 将最后转化成的json放到list里
        FindIterable<Document> resultDoc = wxuserFriend.find(query); // 查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    public List<Document> impactStgtReaBySearchNumsAndType(BasicDBObject strightReaWXQuery, String string) {
        List<Document> result = new ArrayList<>();
        final MongoCollection<Document> qqreMcs = getDocumentMongoCollection(mongoClient212, "QQREMcs", string);
        FindIterable<Document> ite = qqreMcs.find(strightReaWXQuery);
        for (Document doc : ite) {
            result.add(doc);
        }
        return result;
    }

    public List<Document> likePersonPhone(String phone) {

        if (phone != null) {
            Document query = new Document();
            /*
             * 这里的正则？
             */
            Pattern pattern = Pattern.compile("^" + phone.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("phone", pattern);
            // listResult = Common.likePersonPhone(query, "t_person", 20);
            List<Document> list = new ArrayList<>(); // 将最后转化成的json放到list里
            final MongoCollection<Document> person = getDocumentMongoCollection(mongoClient, "infoData2", "t_person");
            FindIterable<Document> resultDoc = person.find(query).limit(20); // 查询到结果
            //logger.debug("Collection " +COLLECTION.getNamespace());
            //logger.debug(query);
            for (Document d : resultDoc) {
                list.add(d);
            }
            logger.debug(String.valueOf(list));
            return list;
        }
        return null;

    }

    public List<String> getPhoneFromPerson(List<Document> personPhone) {
        List<String> phones = new ArrayList<>();
        if (personPhone == null || personPhone.isEmpty()) {
            return phones;
        }
        personPhone.forEach(t -> {
            Object phoneObj = t.get("phone");
            ArrayList<?> phoneList = ArrayList.class.cast(phoneObj);
            if (phoneList != null && !phoneList.isEmpty()) {
                phoneList.forEach(phone -> {
                    phones.add(phone.toString());
                });
            }
        });
        return phones;
    }

    public List<String> likePhonenum(String phone) {
        if (phone != null) {
            // 模糊查询条件 正则
            Pattern pattern = Pattern.compile("^" + phone + ".*$", Pattern.CASE_INSENSITIVE);
            BasicDBObject query = new BasicDBObject();
            query.put("phonenum", pattern);
            // listResult = Common.commonLike(query, "t_contact_phonenum");
            final MongoCollection<Document> contactPhoneNum = getDocumentMongoCollection(mongoClient, "infoData2", "t_contact_phonenum");
            FindIterable<Document> resultDoc = contactPhoneNum.find(query).limit(50); // 查询到结果

            // 去重复
            List<String> resultList = new ArrayList<>();
            for (Document doc : resultDoc) {
                if (!resultList.contains(doc.get("phonenum").toString())) {
                    resultList.add(doc.get("phonenum").toString());
                }
            }
            return resultList;
        }

        return null;
    }

    public List<String> likeWxUsername(String wx) {
        if (wx != null) {
            //模糊查询条件 正则
            Pattern pattern = Pattern.compile("^" + wx + ".*$", Pattern.CASE_INSENSITIVE);
            BasicDBObject query = new BasicDBObject();
            query.put("username", pattern);
            //listResult = Common.commonLike(query, "t_wxuser");
            final MongoCollection<Document> wxuser = getDocumentMongoCollection(mongoClient, "infoData2", "t_wxuser");
            FindIterable<Document> resultDoc = wxuser.find(query).limit(50);    //查询到结果
            List<String> resultList = new ArrayList<>();
            for (Document doc : resultDoc) {
                if (!resultList.contains(doc.get("username").toString())) {
                    resultList.add(doc.get("username").toString());
                }
            }
            return resultList;
        }


        return null;
    }

    public List<String> likeQqUin(String qq) {
        if (qq != null) {
            //模糊查询条件 正则
            Pattern pattern = Pattern.compile("^" + qq + ".*$", Pattern.CASE_INSENSITIVE);
            BasicDBObject query = new BasicDBObject();
            query.put("uin", pattern);
            //  listResult = Common.commonLike(query, "t_qquser");
            final MongoCollection<Document> qquser = getDocumentMongoCollection(mongoClient, "infoData2", "t_qquser");
            FindIterable<Document> resultDoc = qquser.find(query).limit(50);    //查询到结果
            List<String> resultList = new ArrayList<>();
            for (Document doc : resultDoc) {
                if (!resultList.contains(doc.get("uin").toString())) {
                    resultList.add(doc.get("uin").toString());
                }

            }
            return resultList;
        }
        return null;
    }

    public List<Document> findDeviceByName(String deviceName) {
        List<Document> list = new ArrayList<>();
        final MongoCollection<Document> impactDeviceColl = getDocumentMongoCollection(mongoClient, "impact_correlation", "impact_device");
        if (deviceName != null) {
            BasicDBObject query = new BasicDBObject();
            Pattern pattern = Pattern.compile("^.*" + deviceName + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("devicename", pattern);
            //将最后转化成的json放到list里
            FindIterable<Document> resultDoc = impactDeviceColl.find(query);    //查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
        }
        return list;
    }

    public List<Document> findPersonByName(String personName) {
        List<Document> list = new ArrayList<Document>();
        final MongoCollection<Document> person = getDocumentMongoCollection(mongoClient, "infoData2", "t_person");
        if (personName != null) {
            BasicDBObject query = new BasicDBObject();
            Pattern pattern = Pattern.compile("^.*" + personName + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("personname", pattern);
            //将最后转化成的json放到list里
            FindIterable<Document> resultDoc = person.find(query);    //查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
        }
        return list;
    }


    public List<Document> findPersonByIDnumber(String idNumber) {
        List<Document> list = new ArrayList<>();
        final MongoCollection<Document> person = getDocumentMongoCollection(mongoClient, "infoData2", "t_person");
        if (idNumber != null) {
            BasicDBObject query = new BasicDBObject();
            query.append("usernumber", idNumber);
            //将最后转化成的json放到list里
            FindIterable<Document> resultDoc = person.find(query);    //查询到结果
            for (Document d : resultDoc) {
                list.add(d);
            }
        }
        return list;
    }


    public List<Document> findDevice(Document query) {
        List<Document> list = new ArrayList<>();
        final MongoCollection<Document> deviceImpactColl = getDocumentMongoCollection(
            mongoClient, "impact_correlation", "impact_device"
        );
        FindIterable<Document> resultDoc = deviceImpactColl.find(query);    //查询到结果
        for (Document d : resultDoc) {
            list.add(d);
        }
        return list;
    }

    private MongoCollection<Document> getDocumentMongoCollection(MongoClient datasourceClient, String dbName, String collectionName) {
        MongoCollection<Document> impactDevice;
        impactDevice = Optional.ofNullable(establishedCollections.get(collectionName)).orElseGet(
                () -> {
                    final MongoCollection<Document> collection = Optional.ofNullable(
                            datasourceClient.getDatabase(dbName)
                    ).map(db -> db.getCollection(collectionName))
                            .orElseThrow(
                                    () -> new IllegalStateException("cannot contact to MongoDB collection: impact_device")
                            );
                    establishedCollections.putIfAbsent(collectionName, collection);
                    return collection;
                }
        );
        return impactDevice;
    }
}
