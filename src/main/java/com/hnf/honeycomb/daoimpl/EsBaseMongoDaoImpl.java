package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.dao.EsBaseMongoDao;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hnf
 */
@Repository
public class EsBaseMongoDaoImpl implements EsBaseMongoDao {

    @Resource(name = MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient;


    //通过数据库以及集合名及条件查询对应的数据
    @Override
    public List<Document> findInfoByDBNameAndGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query) {
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

    @Override
    public long findInfoByGatherNameAndQueryCount(String dbName, String gatherName, BasicDBObject query) {
        return mongoClient.getDatabase(dbName).getCollection(gatherName).count(query);
    }

    //只查询，不分页
    private List<Document> findInfoByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query,
                                                                 Integer page, Integer pageSize) {
        FindIterable<Document> resultIte =
                mongoClient.getDatabase(dbName).getCollection(gatherName).find(query)
                        .skip((page - 1) * pageSize).limit(pageSize);
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
                mongoClient.getDatabase(dbName).getCollection(gatherName).find(query).sort(sort)
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
                mongoClient.getDatabase(dbName).getCollection(gatherName).find(query).sort(sort);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

}
