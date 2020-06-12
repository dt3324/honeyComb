package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.config.MongoBcpClientClusterConfig;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class DeviceMongoDaoImpl implements DeviceMongoDao {

    @Autowired
    @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoBaseClient;

    @Autowired
    @Qualifier(MongoBcpClientClusterConfig.MONGO_SIS_BCP)
    private MongoClient mongoClient;


	/*public DeviceMongoDaoImpl(DatasourceConfig dc) {
        MongoClientOptions.Builder build = new MongoClientOptions.Builder();
		build.connectionsPerHost(50);   //与目标数据库能够建立的最大connection数量为50
		//            build.autoConnectRetry(true);   //自动重连数据库启动
		build.threadsAllowedToBlockForConnectionMultiplier(50); //如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
		*//*
     * 一个线程访问数据库的时候，在成功获取到一个可用数据库连接之前的最长等待时间为2分钟
     * 这里比较危险，如果超过maxWaitTime都没有获取到这个连接的话，该线程就会抛出Exception
     * 故这里设置的maxWaitTime应该足够大，以免由于排队线程过多造成的数据库访问失败
     *//*
		build.maxWaitTime(1000*60*2);
		build.connectTimeout(1000*60*1);    //与数据库建立连接的timeout设置为1分钟

		MongoClientOptions myOptions = build.build();
		try {
			ServerAddress serverAddress = new ServerAddress(dc.getBaseInfo().getHost()
					, dc.getBaseInfo().getPort());
			ServerAddress serverAddress2 = new ServerAddress(dc.getSisInfo().getHost()
					, dc.getSisInfo().getPort());
			//数据库连接实例
			mongoBaseClient = new MongoClient(serverAddress, myOptions);
			mongoBaseClient = new MongoClient(serverAddress2, myOptions);
		} catch (MongoException e){
			e.printStackTrace();
			throw new IllegalStateException("系统启动失败，无法连接到Mongo数据库");
		}
	}*/

    //通过数据库以及集合名及条件查询对应的数据
    @Override
    public List<Document> findInfoByDBNameAndGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query) {
        FindIterable<Document> resultIte = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }
    @Override
    public List<Document> findInfoByDBNameAndGatherNameAndQueryBcp(String dbName, String gatherName, BasicDBObject query) {
        FindIterable<Document> resultIte = mongoClient.getDatabase(dbName).getCollection(gatherName).find(query);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }


    //查询结果
    @Override
    public List<Document> findInfoByGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                       BasicDBObject sort, Integer page, Integer pageSize) {
        if (sort != null && !sort.isEmpty()) {
            if (page == null || pageSize == null) {//此时在程序中默认为不分页
                return this.findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, sort);
            }
            return this.findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, sort, page, pageSize);

        }
        if (page == null || pageSize == null) {//此时在程序中默认为不分页
            return this.findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query);
        }
        return this.findInfoByGatherNameAndDBNameAndQuery(dbName, gatherName, query, page, pageSize);
    }

    //只查询，不分页
    private List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                                 Integer page, Integer pageSize) {
        FindIterable<Document> resultIte =
                mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query)
                        .skip((page - 1) * pageSize).limit(pageSize);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }


    @Override
    public List<Document> findInfoByGatherNameAndQueryAll(String dbName, String gatherName, BasicDBObject query) {
        FindIterable<Document> resultIte =
                mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

    //直接查询 不分页
    private List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName,
                                                                 BasicDBObject query) {
        return this.findInfoByDBNameAndGatherNameAndQuery(dbName, gatherName, query);
    }

    //查询包含分页及排序
    private List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                                 BasicDBObject sort, Integer page, Integer pageSize) {
        FindIterable<Document> resultIte =
                mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query).sort(sort)
                        .skip((page - 1) * pageSize).limit(pageSize);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

    //查询包含排序
    private List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                                 BasicDBObject sort) {
        FindIterable<Document> resultIte =
                mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query).sort(sort);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

    //对应对应数据库下的对应集合进行聚合
    @Override
    public List<Document> aggregateByGatheNameAndDBNameAndQuery(String dbName, String gatherName,
                                                                List<BasicDBObject> asList) {
        AggregateIterable<Document> iterable = mongoBaseClient.getDatabase(dbName).getCollection(gatherName)
                .aggregate(asList).allowDiskUse(true);
        List<Document> results = new ArrayList<>();
        for (Document d : iterable) {
            results.add(d);
        }
        return results;
    }

    //对于查询数据库只返回指定的字段
    @Override
    public List<Document> findSomeFiledsByGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                             BasicDBObject fileds) {
        FindIterable<Document> ite = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(query).projection(fileds);
        List<Document> results = new ArrayList<>();
        for (Document d : ite) {
            results.add(d);
        }
        return results;
    }

    @Override
    public Long countByQueryAndDBAndCollName(String dbName, String gatherName, List<BasicDBObject> para) {
        // TODO Auto-generated method stub
        AggregateIterable<Document> iterable = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).aggregate(para).allowDiskUse(true);
        Long i = 0L;
        for (Document d : iterable) {
            i++;
        }
        return i;
    }

    //对对应的数据库进行统计
    @Override
    public Long countByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query) {
        return mongoBaseClient.getDatabase(dbName).getCollection(gatherName).count(query);
    }

    @Override
    public List<Document> findByDBNameAndGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query,Long start , Long end) {
        // TODO Auto-generated method stub
        FindIterable<Document> limit = mongoBaseClient.getDatabase(dbName).getCollection(gatherName)
                .find()
                .skip(Integer.valueOf(start.toString()))
                .limit(Integer.valueOf(end.toString()));
        ArrayList<Document> documents = new ArrayList<>();
        for (Document document : limit) {
            documents.add(document);
        }
        return documents;
    }

    @Override
    public List<Document> findSisInfoByGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                          Integer page, Integer pageSize) {
        // TODO Auto-generated method stub
        List<Document> result = new ArrayList<>();
        FindIterable<Document> resultIte;
        if (page != null && pageSize != null) {
            resultIte = mongoClient.getDatabase(dbName).getCollection(gatherName).find(query)
                            .skip((page - 1) * pageSize).limit(pageSize);
        }else {
            resultIte = mongoClient.getDatabase(dbName).getCollection(gatherName).find(query);
        }
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

    @Override
    public Long countSisByDBNameAndGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
        return mongoClient.getDatabase(dbName).getCollection(gatherName).count(query);
    }

    @Override
    public List<Document> groupSisAllCount(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
        String mapStr = "function(){\n"
                + "	var jydwdm = this.JYDWDM;\n"
                + "	if(jydwdm === null){\n"
                + "		emit(\"-1\",1);\n"
                + "             return; "
                + "	};\n"
                + "	if(jydwdm.startsWith(\"510700\")){\n"
                + "		emit(jydwdm.substring(0,8),1);\n"
                + "             return; "
                + "	}\n"
                + "	emit(jydwdm.substring(0,6),1);\n"
                + "};";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        MapReduceIterable<Document> documents = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).mapReduce(mapStr, reduceStr);
        List<Document> result = new ArrayList<>();
        for (Document document : documents) {
            result.add(document);
        }
        return result;
    }

    @Override
    public List<Document> groupSisUnitCount(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
        String mapStr = "function(){\n"
                + "	var jydwdm = this.JYDWDM;\n"
                + "	emit(jydwdm,1);\n"
                + "};";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        MapReduceIterable<Document> documents = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).mapReduce(mapStr, reduceStr);
        List<Document> result = new ArrayList<>();
        for (Document document : documents) {
            result.add(document);
        }
        return result;
    }

    @Override
    public List<DBObject> groupSisPersonCount(String dbName, String gatherName, BasicDBObject query, String departmentCode) {
        // TODO Auto-generated method stub
        int codeLength = departmentCode.length();
        String finalMapStr;
        String jySfMapStr = "function(){\n"
                + "	var jydwdm = this.JYXM;\n"
                + "	emit(jydwdm,1);\n"
                + "};";
        String mapStr = "function(){\n"
                + "	var jydwdm = this.JYDWDM;\n"
                + "	if(jydwdm === null){\n"
                + "		emit(\"-1\",1);\n"
                + "             return; "
                + "	};\n"
                + "	if(jydwdm.startsWith(\"selfDepartmmentCode\")){\n"
                + "		emit(jydwdm.substring(0,subLength),1);\n"
                + "             return; "
                + "	}\n"
                + "	}\n";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        //对单位代码进行判定，执行不同的map语句
        switch (codeLength) {
            //当为最下一级时，为对对应的警号进行统计
            case 11:
                finalMapStr = jySfMapStr;
                break;//当为最下一级时，为对对应的警号进行统计
            case 6:
                finalMapStr = mapStr.replace("selfDepartmmentCode", departmentCode)
                        .replaceAll("subLength", codeLength + 5 + "");
                break;
            //其他情况时
            default:
                finalMapStr = mapStr.replace("selfDepartmmentCode", departmentCode)
                        .replaceAll("subLength", codeLength + 2 + "");
                break;
        }
//        MapReduceIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(gatherName).mapReduce(finalMapStr, reduceStr);
        MapReduceCommand cmd = new MapReduceCommand(mongoClient.getDB(dbName).getCollection(gatherName), finalMapStr, reduceStr,
                null, MapReduceCommand.OutputType.INLINE, query);
        MapReduceOutput out = mongoClient.getDB(dbName).getCollection(gatherName).mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new ArrayList<>();
        for (DBObject obj : ite) {
//            if("".equals(obj.get("_id"))){
//                continue;
//            }
            result.add(obj);
        }
//        List<Document> result = new ArrayList<>();
//        for (Document document : documents) {
//            result.add(document);
//        }
        return result;
    }

    @Override
    public List<DBObject> groupSisPersonPreYear(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
        String mapStr = "function(){\n"
                + "	var time = this.UPDATETIME;\n"
                + "	if(time === null){\n"
                + "		time = 0;\n"
                + "	}\n"
                + "	var date = new Date(time);	\n"
                + "	checkDate(date);\n"
                + "}\n"
                + "\n"
                + "function checkDate(date){\n"
                + "	var year = date.getFullYear();\n"
                + "	var newDate = new Date(year,0);\n"
                + "	emit(newDate,1);\n"
                + "}";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
//        MapReduceIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(gatherName).mapReduce(mapStr, reduceStr);
        MapReduceCommand cmd = new MapReduceCommand(mongoClient.getDB(dbName).getCollection(gatherName), mapStr, reduceStr,
                null, MapReduceCommand.OutputType.INLINE, query);
        MapReduceOutput out = mongoClient.getDB(dbName).getCollection(gatherName).mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new ArrayList<>();
        for (DBObject obj : ite) {
//            if("".equals(obj.get("_id"))){
//                continue;
//            }
            result.add(obj);
        }
        return result;
    }

    @Override
    public List<DBObject> groupSisPersonPreMonth(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
        String mapStr = "function(){\n"
                + "	var time = this.UPDATETIME;\n"
                + "	if(time === null){\n"
                + "		time = 0;\n"
                + "	}\n"
                + "	var date = new Date(time);	\n"
                + "	checkDate(date);\n"
                + "}\n"
                + "\n"
                + "function checkDate(date){\n"
                + "	var year = date.getFullYear();\n"
                + "	var month = date.getMonth();\n"
                + "	var newDate = new Date(year,month);\n"
                + "	emit(newDate,1);\n"
                + "}";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
//        MapReduceIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(gatherName).mapReduce(mapStr, reduceStr);
        MapReduceCommand cmd = new MapReduceCommand(mongoClient.getDB(dbName).getCollection(gatherName), mapStr, reduceStr,
                null, MapReduceCommand.OutputType.INLINE, query);
        MapReduceOutput out = mongoClient.getDB(dbName).getCollection(gatherName).mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new ArrayList<>();
        for (DBObject obj : ite) {
//            if("".equals(obj.get("_id"))){
//                continue;
//            }
            result.add(obj);
        }
        return result;
    }

    @Override
    public List<DBObject> groupSisPersonPreDay(String dbName, String gatherName, BasicDBObject query) {
        // TODO Auto-generated method stub
 /*       String mapStr = "function(){\n"
                + "	var time = this.UPDATETIME;\n"
                + "	if(time === null){\n"
                + "		time = 0;\n"
                + "	}\n"
                + "	var date = new Date(time);	\n"
                + "	checkDate(date);\n"
                + "}\n"
                + "\n"
                + "function checkDate(date){\n"
                + "	var year = date.getFullYear();\n"
                + "	var month = date.getMonth();\n"
                + "	var day = date.getDate();\n"
                + "	var newDate = new Date(year,month,day);\n"
                + "	emit(newDate,1);\n"
                + "}";*/

        String mapStr = "var time = this.UPDATETIME;\n" +
                "\tif(time === null){\n" +
                "\t\ttime = 0;\n" +
                "\t}\n" +
                "\tvar date = new Date(time);\t\n" +
                "\tvar year = date.getFullYear();\n" +
                "\tvar month = date.getMonth();\n" +
                "\tvar day = date.getDate();\n" +
                "\tvar newDate = new Date(year,month,day);\n" +
                "\temit(newDate,1);";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
//        MapReduceIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(gatherName).mapReduce(mapStr, reduceStr);
        MapReduceCommand cmd = new MapReduceCommand(mongoClient.getDB(dbName).getCollection(gatherName), mapStr, reduceStr,
                null, MapReduceCommand.OutputType.INLINE, query);
        MapReduceOutput out = mongoClient.getDB(dbName).getCollection(gatherName).mapReduce(cmd);
        Iterable<DBObject> ite = out.results();
        List<DBObject> result = new ArrayList<>();
        for (DBObject obj : ite) {
//            if("".equals(obj.get("_id"))){
//                continue;
//            }
            result.add(obj);
        }
        return result;
    }

    @Override
    public Long maxId(String dbName, String gatherName) {
        FindIterable<Document> resultDoc = mongoBaseClient.getDatabase(dbName)
                .getCollection(gatherName).find().sort(new BasicDBObject("id", -1)).limit(1);
        // 查询到结果  -1是倒序，1是正序

        for (Document d : resultDoc) {
            return d.getLong("id");
        }
        return 0L;
    }

    @Override
    public Long maxUpdateTime(String dbName, String gatherName) {
        FindIterable<Document> resultDoc = mongoBaseClient.getDatabase(dbName)
                .getCollection(gatherName).find().sort(new BasicDBObject("_updateTime", -1)).limit(1);
        // 查询到结果  -1是倒序，1是正序

        for (Document d : resultDoc) {
            return d.getLong("_updateTime");
        }
        return 0L;
    }

    @Override
    public void insertDocs(String dbName, String gatherName, List<Document> docs) {
        InsertManyOptions option = new InsertManyOptions();
        //设置报错依然继续执行的选项
        option.ordered(false);
        List<Document> insertDoc = new ArrayList<>();
        Long i = 0L;
        for (Document document : docs) {
            i++;
            insertDoc.add(document);
            //设置每2000条或者其为list最后的时候进行入库操作
            if (i % 2000 == 0 || i == docs.size()) {
                mongoBaseClient.getDatabase(dbName).getCollection(gatherName).insertMany(insertDoc, option);
                insertDoc = new ArrayList<>();
            }
        }
    }

    @Override
    public List<Document> aggregateDeviceByGatheNameAndDBNameAndQuery(String dbName, String gatherName,
                                                                      List<BasicDBObject> asList) {
        // TODO Auto-generated method stub
        AggregateIterable<Document> iterable = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).aggregate(asList).allowDiskUse(true);
        BasicDBObject match = (BasicDBObject) asList.get(0).get("$match");
        List<Document> resultDoc = new ArrayList<>();
        for (Document d : iterable) {
            match.append("device_unique", d.getString("_id")).append("fetchtime", d.getLong("fetchtime"));
            FindIterable<Document> documents = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).find(match).limit(1);
            for (Document document : documents) {
                resultDoc.add(document);
            }
        }

        return resultDoc;
    }

    /**
     * 用于聚合后对应数据的查询(只针对设备)
     *
     * @param deviceUnique 设备的deviceUnique
     * @param fetchTime    对应其的采集时间
     * @return 返回对应的数据
     */
    private Document findDeviceByFetchTimeAndUnique(String deviceUnique, Long fetchTime) {
        Document query = new Document();
        query.append("device_unique", deviceUnique).append("fetchtime", fetchTime);
        FindIterable<Document> ite = mongoBaseClient.getDatabase("infoData2").getCollection("fetchlog").find(query);
        for (Document d : ite) {
            return d;
        }

        return null;
    }

    @Override
    public BasicDBList record(String dbName, String gatherName, String deviceUnique) {
        // TODO Auto-generated method stub
        List<Document> listResult = new ArrayList<>();
        //分组查询
        String[] key = new String[]{"phonenum"};
        BasicDBObject cond = new BasicDBObject();
        cond.append("deviceUnique", deviceUnique);
        BasicDBObject initial = new BasicDBObject();
        initial.append("count", 0);
        String reduce = "function (" + "key, values) { " + "     values.count++;   " + "} ";
        DBObject key1 = new BasicDBObject();
        for (int i = 0; i < key.length; i++) {
            key1.put(key[i], true);
        }
        cond = (cond == null) ? new BasicDBObject() : cond;
        if (initial == null) {      //定义一些初始变量
            initial = new BasicDBObject();
            for (int i = 0; i < key.length; i++) {
                DBObject index = new BasicDBObject();
                index.put("count", 0);
                index.put("sum", 0);
                index.put("max", 0);
                index.put("min", 0);
                index.put("avg", 0);
                index.put("self", "");
                initial.put(key[i], index);
            }
        }
        BasicDBList result = (BasicDBList) mongoBaseClient.getDB(dbName).getCollection(gatherName).group(key1, cond, initial, reduce, null);

        //排序------java8自带的
        result.sort((soft1, soft2) -> {
            BasicDBObject object1 = (BasicDBObject) soft1;
            BasicDBObject object2 = (BasicDBObject) soft2;
            Double soft1Count = (Double) object1.get("count");
            Double soft2Count = (Double) object2.get("count");
            return -soft1Count.compareTo(soft2Count);
        });

        return result;
    }

    // 日志相关接口
    @Override
    public void updateFetchByDoc(String dbName, String gatherName, BasicDBObject searchQuery, BasicDBObject newDocument) {
        mongoBaseClient.getDB(dbName).getCollection(gatherName).updateMulti(searchQuery, newDocument);
    }

    @Override
    public ObjectId insertOperationDocument(String dbName, String gatherName, HashMap<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        mongoBaseClient.getDatabase(dbName).getCollection(gatherName).insertOne(doc);
        ObjectId oid = doc.getObjectId("_id");
        return oid;
    }

    @Override
    public void insertDocument(String database, String collection, Document document) {
        try {
            mongoBaseClient.getDatabase(database).getCollection(collection).insertOne(document);
        } catch (Exception e) {

        }
    }

    @Override
    public Long updateDocument(String dbName, String gatherName, Document queryDoc, Document newDoc) {
        UpdateResult updateResult = mongoBaseClient.getDatabase(dbName).getCollection(gatherName).updateOne(queryDoc,
                new Document("$set", newDoc),new UpdateOptions().upsert(true));
        return updateResult.getModifiedCount();
    }

    @Override
    public void  insertExcel(String dbName, String gatherName, List<Document> docs) {
        InsertManyOptions option = new InsertManyOptions();
        // 设置报错依然继续执行的选项
        option.ordered(false);
        List<Document> insertDoc = new ArrayList<>();
        Long i = 0L;
        for (Document document : docs) {
            i++;
            insertDoc.add(document);
            // 设置每2000条或者其为list最后的时候进行入库操作
            if (i % 2000 == 0 || i == docs.size()) {
                try {
                    mongoBaseClient.getDatabase(dbName).getCollection(gatherName).insertMany(insertDoc,
                            option);
                } catch (Exception e) {
                    //错误流程 不进行操作
                }
                insertDoc = new ArrayList<>();
            }
        }
    }
}
