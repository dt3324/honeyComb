/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hnf.honeycomb.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author hnf
 */
@Component
public class NoSqlOperation {
    private MongoCollection<Document> userLoginColl;
    private MongoCollection<Document> searchColl;
    private MongoCollection<Document> spaceColl;
    private MongoCollection<Document> qqRemarkColl;
    private MongoCollection<Document> relationColl;
    private MongoCollection<Document> userLogoutColl;

    @Autowired
    public NoSqlOperation(@Qualifier(MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase("logData");
        userLoginColl = mongoDatabase.getCollection("userlogin");
        searchColl = mongoDatabase.getCollection("search");
        spaceColl = mongoDatabase.getCollection("space");
        qqRemarkColl = mongoDatabase.getCollection("qqremark");
        relationColl = mongoDatabase.getCollection("relation");
        userLogoutColl = mongoDatabase.getCollection("userlogout");
    }

    public ObjectId insertDocumentLogin(HashMap<String, Object> values) {
//        HashMap<String, Object> coord = (HashMap)values.get("coordinate");
//        values.remove("coordinate");
//        Document doc = new Document();
//        doc.putAll(values);
//        List coordList = null;
//        if(null != coord)
//        {
//            coordList = Arrays.asList(Double.valueOf(coord.get("longitude").toString().trim()), Double.valueOf(coord.get("latitude").toString().trim()));
//            HashMap<String,Object> loc = new HashMap<>();
//            loc.put("type", "Point");
//            loc.put("coordinate", coordList);
//            doc.append("loc", loc);
//        }
        Document doc = new Document();
        doc.putAll(values);
        userLoginColl.insertOne(doc);
        ObjectId oid = doc.getObjectId("_id");
        return oid;
    }

    public String insertDocumentSearch(HashMap<String, Object> values) throws Exception {
//        HashMap<String, Object> coord = (HashMap)values.get("coordinate");
//        values.remove("coordinate");
//        Document doc = new Document();
//        doc.putAll(values);
//        List coordList = null;
//        if(null != coord) {
//            coordList = Arrays.asList(Double.valueOf(coord.get("longitude").toString().trim()), Double.valueOf(coord.get("latitude").toString().trim()));
//            HashMap<String,Object> loc = new HashMap<>();
//            loc.put("type", "Point");
//            loc.put("coordinate", coordList);
//            doc.append("loc", loc);
//        }
        String string = "";
        try {
            Document doc = new Document();
            doc.putAll(values);
            searchColl.insertOne(doc);
        } catch (Exception e) {
            string = "{\"state\":1,\"message\":\"服务器出错\"}";
        }
        return string;
    }

    public void insertDocumentSpace(HashMap<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        spaceColl.insertOne(doc);
    }

    public void insertDocumentQqRemark(HashMap<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        qqRemarkColl.insertOne(doc);
    }

    public void insertDocumentRelation(HashMap<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        relationColl.insertOne(doc);
    }

    public void insertDocumentUserLogout(HashMap<String, Object> values) {
        Document doc = new Document();
        doc.putAll(values);
        userLogoutColl.insertOne(doc);
    }
}
