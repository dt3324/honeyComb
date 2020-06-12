package com.hnf.honeycomb.util;

import com.hnf.honeycomb.service.user.UserDeviceService;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisUtilNewImplTest {

    @Autowired
    private RedisUtilNew redisUtilNew;

    @Autowired
    private UserDeviceService userDeviceService;

    @Test
    public void s(){
        Map<String, Object> map = userDeviceService.aggregateCaseDevicePersonInfo(null, "51000000000",
                1, 1, "", "", "", "", null, null);
        System.out.println(map.get("detail"));
        System.out.println(map.get("count"));

    }
    @Test
    public void lPush() {
        redisUtilNew.remove("张三","李四");
        List<Object> list = redisUtilNew.lRange("张三", 0, 3);
        System.out.println(list+"111111111111111111");
//        redisUtilNew.lPush("张三","1");
//        redisUtilNew.lPush("张三","1");
//        redisUtilNew.lPush("张三","2");
//        if(redisUtilNew.lRange("张三", 2, 2) == null) {
//            redisUtilNew.lPush("张三", "2");
//        }
    }

    @Test
    public void lRange() {
        MongoCredential mongoCredential = MongoCredential.createCredential("root", "admin", "gb.2312".toCharArray());
        MongoCollection<Document> collection = new MongoClient(new ServerAddress("192.168.99.14", 27017),
                Collections.singletonList(mongoCredential)).
                getDatabase("infoData2").getCollection("t_person");
        BasicDBObject match = new BasicDBObject("$match", new BasicDBObject("usernumber", "513423198304078276"));
        BasicDBObject unwind = new BasicDBObject("$unwind", "$device_unique");
        BasicDBObject query = new BasicDBObject();
        BasicDBObject lookup = new BasicDBObject("$lookup",
                new BasicDBObject("from", "fetchlog")
                        .append("localField", "device_unique")
                        .append("foreignField", "device_unique")
                        .append("as", "temp"));
        BasicDBObject unwind1 = new BasicDBObject("$unwind", "$temp");
//        BasicDBObject query1 = new BasicDBObject("temp.department_code","5555555");
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "").append(
                "count", new BasicDBObject("$sum",1)));
//
//
//        BasicDBObject lookup1 = new BasicDBObject("$lookup",
//                new BasicDBObject("from", "t_case")
//                        .append("localField", "case_unique")
//                        .append("foreignField", "caseuniquemark")
//                        .append("as", "temp1"));
        AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(match, unwind, lookup, unwind1,group));

        for (Document document : aggregate) {
            System.out.println("------------------------ " + document);
        }
    }
}
