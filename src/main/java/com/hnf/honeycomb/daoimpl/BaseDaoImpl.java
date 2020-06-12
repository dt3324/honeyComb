package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.dao.BaseDao;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/6 9:11
 */
@Repository
public class BaseDaoImpl implements BaseDao {

    @Autowired
    @Qualifier(MongoBaseClientClusterConfig.MONGO_BASE)
    private MongoClient mongoClient;

    @Override
    public List<Document> listQuery(String dbName, String colName, Bson query) {
        FindIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(colName).find(query);
        return toList(documents);
    }

    @Override
    public List<Document> listQuery(String dbName, String colName, Bson query, Bson sort) {
        FindIterable<Document> documents = mongoClient.getDatabase(dbName).getCollection(colName)
                .find(query).sort(sort);
        return toList(documents);
    }

    @Override
    public List<Document> listQuery(String dbName, String colName, Bson query, Bson sort, int page, int pageSize) {
        FindIterable<Document> limit = mongoClient.getDatabase(dbName).getCollection(colName)
                .find(query).sort(sort).skip(page * pageSize - pageSize).limit(pageSize);
        return toList(limit);
    }

    @Override
    public List<Document> listQuery(String dbName, String colName, Bson query, int page, int pageSize) {
        FindIterable<Document> limit = mongoClient.getDatabase(dbName).getCollection(colName)
                .find(query).skip(page * pageSize - page).limit(pageSize);
        return toList(limit);
    }

    @Override
    public List<Document> listQueryAggregate(String dbName, String colName, List<Bson> aggregateOperation) {
        AggregateIterable<Document> documents = connect(dbName, colName).aggregate(aggregateOperation).allowDiskUse(true);
        return toList(documents);
    }

    @Override
    public List<Document> queryFields(String dbName, String colName, Bson query, Bson fields) {
        FindIterable<Document> projection = connect(dbName, colName).find(query).projection(fields);
        return toList(projection);
    }

    @Override
    public long countWithQuery(String dbName, String colName, Bson query) {
        return connect(dbName, colName).count(query);
    }

    @Override
    public long countWithAggregate(String dbName, String colName, List<Bson> aggregateOperation) {
        return 0L;
    }

    @Override
    public void insertDocuments(String dbName, String colName, List<Document> docs) {
        connect(dbName, colName).insertMany(docs);
    }

    @Override
    public void updateDocuments(String dbName, String colName, Bson query, Bson update) {
        connect(dbName, colName).updateMany(query, update);
    }

    @Override
    public MongoCollection<Document> connect(String dbName, String colName) {
        return mongoClient.getDatabase(dbName).getCollection(colName);
    }

    @Override
    public <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
