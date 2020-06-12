package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.dao.GisMongoDao;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB数据库访问层连接配置
 *
 * @author yy
 */
@Repository("gisMongoDao")
public class GisMongoDaoImpl implements GisMongoDao {
    /**
     * Inject from constructor
     */
    private final MongoClient mongoClient;

    @Autowired
    public GisMongoDaoImpl(@Qualifier(value = MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    //通过数据库名以及集合名以及查询条件对数据进行查询
    @Override
    public List<Document> findInfoByDBNameAndGatherNameAndQuery(String dbName, String gatherName, BasicDBObject query) {
        FindIterable<Document> resultIte = mongoClient.getDatabase(dbName).getCollection(gatherName).find(query);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

    //通过数据库名及集合名对数据进行mapResult
    @Override
    public List<Document> mapReduceByDBNameAndGatherNameAndQuery(
        String dbName, String gatherName, String map, String reduce, BasicDBObject query
    ) {
        final List<Document> result = new ArrayList<>();
        mongoClient.getDatabase(dbName).getCollection(gatherName).mapReduce(
            map,reduce
        ).filter(query).iterator().forEachRemaining(
            result::add
        );
        return result;
    }

    //排序查询
    @Override
    public List<Document> findInfoByDBNameAndGatherNameAndQueryAndSort(String dbName, String gatherName,
                                                                       BasicDBObject query, BasicDBObject sort) {
        FindIterable<Document> resultIte = mongoClient.getDatabase(dbName).getCollection(gatherName).find(query).sort(sort);
        List<Document> result = new ArrayList<>();
        for (Document doc : resultIte) {
            result.add(doc);
        }
        return result;
    }

}
