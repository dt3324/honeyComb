package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImpactCaseDao {

    private final MongoDatabase infoData2Database;

    public ImpactCaseDao(
            @Autowired @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient

    ) {

        infoData2Database = mongoClient.getDatabase("infoData2");
    }

    public List<Document> findCase(BasicDBObject query) {
        List<Document> result = new ArrayList<>();
        MongoCollection<Document> tCaseCollection = infoData2Database.getCollection("t_case");
        FindIterable<Document> iterable = tCaseCollection.find(query);
        for (Document d : iterable) {
            result.add(d);
        }
        return result;
    }

    public List<Document> findDeviceByCaseName(Document query) {
        List<String> deviceList = new ArrayList<>();
        List<Document> resultList = new ArrayList<>();
        MongoCollection<Document> tCaseCollection = infoData2Database.getCollection("t_case");
        MongoCollection<Document> tDeviceCollection = infoData2Database.getCollection("t_device");
        FindIterable<Document> iterable = tCaseCollection.find(query);
        for (Document d : iterable) {
            deviceList.addAll(ArrayList.class.cast(d.get("device_unique")));
        }
        Document find = new Document("device_unique", new Document(QueryOperators.IN, deviceList));
        FindIterable<Document> result = tDeviceCollection.find(find);
        for (Document document : result) {
            resultList.add(document);
        }
        return resultList;
    }


    public List<Document> findCase(List<Document> query) {
        List<Document> result = new ArrayList<>();
        MongoCollection<Document> tCaseCollection = infoData2Database.getCollection("caseAbstract");
        AggregateIterable<Document> iterable = tCaseCollection.aggregate(query).allowDiskUse(true);
        for (Document d : iterable) {
            result.add(d);
        }
        return result;
    }

    public List<Document> findPerson(List<BasicDBObject> query) {
        List<Document> result = new ArrayList<>();
        MongoCollection<Document> tPersonCollection = infoData2Database.getCollection("personAbstract");
        AggregateIterable<Document> iterable = tPersonCollection.aggregate(query).allowDiskUse(true);
        for (Document d : iterable) {
            result.add(d);
        }
        return result;
    }

    public List<Document> findPersonParent(List<BasicDBObject> query,String phone) {
        List<Document> result = new ArrayList<>();
        MongoCollection<Document> tPersonCollection = infoData2Database.getCollection("t_person");
        AggregateIterable<Document> iterable = tPersonCollection.aggregate(query);
        for (Document d : iterable) {
            resultData(d,phone);
            result.add(d);
        }
        return result;
    }

    private void resultData(Document d,String phone) {
        //查询结果中电话是多个  需要找到匹配的那个电话
        List<String> list = d.get("phone", List.class);
        for (String s : list) {
            if(s.contains(phone)){
                d.put("phone", s);
            }
        }
        Document document = d.get("temp", Document.class);
        d.put("collType", document.get("collType"));
        d.put("departmentCode", document.get("departmentCode"));
        d.put("key_id",document.get("key_id") );
        d.put("personKeyWords", document.get("personKeyWords"));
        d.put("personName", document.get("personName"));
        d.put("personNumber", document.get("personNumber"));
        d.put("policeNumber", document.get("policeNumber"));
        d.remove("temp");
    }
}
