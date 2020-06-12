package com.hnf.honeycomb.dao;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author hnf
 * @version 1.0
 * @date 2019/7/19 13:40
 */
public interface BaseDao {
    /**
     * mongoDB简单查询
     *
     * @param dbName  数据库名称
     * @param colName 集合名称
     * @param query   查询条件
     * @return
     */
    List<Document> listQuery(String dbName, String colName, Bson query);

    /**
     * mongoDB简单查询
     *
     * @param dbName  数据库名称
     * @param colName 集合名称
     * @param query   查询条件
     * @return
     */
    List<Document> listQuery(String dbName, String colName, Bson query, Bson sort);

    /**
     * mongoDB查询加分页和排序
     *
     * @param dbName   数据库名称
     * @param colName  集合名称
     * @param query    查询条件
     * @param sort     排序条件
     * @param page     页码
     * @param pageSize 页容量
     * @return
     */
    List<Document> listQuery(String dbName, String colName, Bson query, Bson sort, int page, int pageSize);

    /**
     * mongoDB分页查询
     *
     * @param dbName
     * @param colName
     * @param query
     * @param page
     * @param pageSize
     * @return
     */
    List<Document> listQuery(String dbName, String colName, Bson query, int page, int pageSize);

    /**
     * mongoDB 聚合查询
     *
     * @param dbName             数据库名称
     * @param colName            集合名称
     * @param aggregateOperation 聚合查询操作符
     * @return
     */
    List<Document> listQueryAggregate(String dbName, String colName, List<Bson> aggregateOperation);

    /**
     * 查询，返回指定字段
     *
     * @param dbName
     * @param colName
     * @param query
     * @param fields
     * @return
     */
    List<Document> queryFields(String dbName, String colName, Bson query, Bson fields);

    /**
     * 查询并计数
     *
     * @param dbName
     * @param colName
     * @param query
     * @return
     */
    long countWithQuery(String dbName, String colName, Bson query);

    /**
     * 聚合查询并计数
     *
     * @param dbName             对应的数据库名
     * @param colName            对应的集合名
     * @param aggregateOperation 对应的查询条件
     * @return 查询结果文档数
     */
    long countWithAggregate(String dbName, String colName, List<Bson> aggregateOperation);

    /**
     * 入库操作
     *
     * @param dbName  数据库名
     * @param colName 表名
     * @param docs    需要入库的数据
     */
    void insertDocuments(String dbName, String colName, List<Document> docs);

    /**
     * 更新操作
     *
     * @param dbName  数据库名
     * @param colName 集合名称
     * @param query
     * @param update
     */
    void updateDocuments(String dbName, String colName, Bson query, Bson update);

    /**
     * mongoDb 数据库表连接获取
     *
     * @param dbName
     * @param colName
     * @return
     */
    MongoCollection<Document> connect(String dbName, String colName);

    /**
     * Iterable To ArrayList
     *
     * @param iterable
     * @param <T>
     * @return
     */
    <T> List<T> toList(Iterable<T> iterable);
}
