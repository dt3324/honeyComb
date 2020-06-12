package com.hnf.honeycomb.dao;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.util.TokenUtil;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDriverInformation;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.jmx.export.metadata.ManagedOperation;
import org.springframework.jmx.export.metadata.ManagedOperationParameter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/24 14:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoTest {

    @Autowired
    @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient;
    @Autowired
    TokenUtil tokenUtil;
    public MongoClient getMongoClient() {
        List<ServerAddress> addresses = new LinkedList<>();
        addresses.add(new ServerAddress("192.168.1.11", getInteger(27017)));
        MongoClientOptions options = MongoClientOptions.builder().build();
        MongoDriverInformation information = MongoDriverInformation.builder().build();
            MongoCredential credential = MongoCredential.createCredential(
                    "root", "admin", "gb.2312".toCharArray()
            );
            return new MongoClient(addresses, credential, options, information);
    }
    @Test
    public void mongoId(){
        List<ServerAddress> addresses = new LinkedList<>();
        addresses.add(new ServerAddress("192.168.1.11", getInteger(27017)));
        MongoClientOptions options = MongoClientOptions.builder().build();
        MongoDriverInformation information = MongoDriverInformation.builder().build();
        MongoCredential credential = MongoCredential.createCredential(
                "root", "admin", "gb.2312".toCharArray()
        );
        MongoClient mongoClient =  new MongoClient(addresses, credential, options, information);

        MongoCollection<Document> collection = mongoClient.getDatabase("infoData2").getCollection("fetchlog");
        FindIterable<Document> limit = collection.find().limit(1);
        for (Document document : limit) {
            System.out.println(document);
            ObjectId id = document.getObjectId("_id");
            System.out.println(id.getDate().getTime());
            System.out.println(id.getTimestamp());
            System.out.println(document.getLong("_updateTime"));
        }
    }


    @Test
    public void subStrGroup(){
        MongoCollection<Document> collection = mongoClient.getDatabase("infoData2").getCollection("fetchlog");
        Pattern compile = Pattern.compile("^" + "51" + ".*$");
        //查询 的条件
        BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("department_code", compile));
        //查询的字段 以及字段的形式
        BasicDBObject project = new BasicDBObject(
                "$project",
                new BasicDBObject("department_code", 1)
                        .append("app", new BasicDBObject("$max", Arrays.asList("$wx", "$qq")))
                        .append("msg", "$shortMessage")
                        .append("callog", 1)
                        .append("phone", 1)
        );
        //字符串截取 及分组
        BasicDBList concat = new BasicDBList();
        BasicDBList subStr = new BasicDBList();
        subStr.add("$department_code");
        subStr.add(0);
        subStr.add(3);
        concat.add(new BasicDBObject("$substr", subStr));
        BasicDBList pow = new BasicDBList();
        pow.add(10);
        pow.add(6);
        new BasicDBObject("$toString", new BasicDBObject("$toInt", new BasicDBObject("$pow", pow)));
        concat.add("0000000");

        BasicDBObject group = new BasicDBObject(
                "$group",
//                new BasicDBObject("_id", "$department_code")
                new BasicDBObject("_id", new BasicDBObject("$concat", concat))
                        .append("count", new BasicDBObject("$sum", 1))
                        .append("app", new BasicDBObject("$sum", "$app"))
                        .append("msg", new BasicDBObject("$sum", "$msg"))
                        .append("callLog", new BasicDBObject("$sum", "$callLog"))
                        .append("phone", new BasicDBObject("$sum", "$phone"))
        );
        FindIterable<Document> limit = collection.find().limit(10);
        for (Document document : limit) {
            System.out.println(0000000000000000000000000000000000000);
            System.out.println(document);
        }
        AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(match, project, group));
        for (Document document : aggregate) {
            System.out.println(document);
        }
    }


    @Test
    public void addPersonKeyWords() {
        MongoCollection<Document> collection = mongoClient.getDatabase("infoData2").getCollection("t_person");
//        FindIterable<Document> documents = collection.find(new BasicDBObject("device_unique", "EA448B668AA60410699B6D4884B395D3"));
        ArrayList<Map<String, String>> maps = new ArrayList<>();
        getMap(maps);
//        for (Document document : documents) {
            collection.updateMany(Filters.eq("device_unique", "DA4E6DBDBD4DBA7FC6519ADB6DBD977F"),
                    new Document("$set", new Document("personKeyWords", maps)));
//        }
    }
    public void getMap(ArrayList<Map<String, String>> maps){
        HashMap<String, String> map = new HashMap<>();
        map.put("flag", "1");
        map.put("type_id", "3");
        map.put("name", "禁毒");
        map.put("id", "4");
        map.put("time", "1566960872720");
        maps.add(map);
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("flag", "1");
        map1.put("type_id", "4");
        map1.put("name", "网安");
        map1.put("id", "8");
        map1.put("time", "1566960872721");
        maps.add(map1);
    }
}
