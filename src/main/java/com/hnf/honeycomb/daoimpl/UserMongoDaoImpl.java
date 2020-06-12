package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.dao.UserMongoDao;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
@Repository
public class UserMongoDaoImpl implements UserMongoDao {

    @Autowired
    @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient;


    @Override
    public void updataFetchByKeyId(String keyId, String newDepartCode) {
        if (!StringUtils.isEmpty(keyId)) {
            BasicDBObject query = new BasicDBObject("key_id", keyId);
            BasicDBObject setDocument = new BasicDBObject("department_code", newDepartCode);
            BasicDBObject newDocument = new BasicDBObject("$set", setDocument);
            mongoClient.getDatabase("infoData2").getCollection("fetchlog").updateMany(query, newDocument);
            mongoClient.getDatabase("infoData2").getCollection("personAbstract").updateMany(query, newDocument);
            mongoClient.getDatabase("infoData2").getCollection("caseAbstract").updateMany(query, newDocument);
        }
    }

    @Override
    public void updateFetchByDoc(String newCode, String oldCode) {
        if (!StringUtils.isEmpty(oldCode)) {
            BasicDBObject query = new BasicDBObject("department_code", oldCode);
            BasicDBObject setDocument = new BasicDBObject("department_code", newCode);
            BasicDBObject newDocument = new BasicDBObject("$set", setDocument);
            mongoClient.getDB("infoData2").getCollection("fetchlog").updateMulti(query, newDocument);
            mongoClient.getDB("infoData2").getCollection("personAbstract").updateMulti(query, newDocument);
            mongoClient.getDB("infoData2").getCollection("caseAbstract").updateMulti(query, newDocument);
        }

    }

    @Override
    public ObjectId insertOperationDocument(String dbName, String gatherName, Map<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        mongoClient.getDatabase(dbName).getCollection(gatherName).insertOne(doc);
        ObjectId oid = doc.getObjectId("_id");
        return oid;
    }

    @Override
    public List<DBObject> mapReduce(String dbName, String gatherName, String mapStr, String reduceStr, BasicDBObject query) {
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
    public Long count(String dbName, String gatherName, BasicDBObject query) {
        Long result = mongoClient.getDB(dbName).getCollection(gatherName).count(query);
        return result;
    }

    @Override
    public List<DBObject> find(String dbName, String gatherName, BasicDBObject query, Integer page, Integer pageSize, BasicDBObject sort) {
        List<DBObject> result = mongoClient.getDB(dbName).getCollection(gatherName).find(query).sort(sort).skip((page - 1) * pageSize).limit(pageSize).toArray();
        return result;
    }

    @Override
    public List<DBObject> findActiveTime(String dbName, String gatherName, BasicDBObject query, BasicDBObject sort, Integer pageSize) {
        // TODO Auto-generated method stub
        List<DBObject> result = mongoClient.getDB(dbName).getCollection(gatherName).find(query).sort(sort).skip(0).limit(pageSize).toArray();
        return result;
    }

    @Override
    public AggregateIterable<Document> findInfoData2(String dbName, String tableName, List<BasicDBObject> query) {
        MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(tableName);
        return collection.aggregate(query);
    }


    @Override
    public List<Document> findInfoData2(String dbName, String tableName, BasicDBObject query, BasicDBObject projection) {
        List<Document> result = new ArrayList<>();
        FindIterable<Document> documents;
        MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(tableName);
        if(projection != null){
            documents = collection.find(query).projection(projection);
        }else {
            documents = collection.find(query);
        }
        for (Document document : documents) {
            result.add(document);
        }
        return result;
    }

}
