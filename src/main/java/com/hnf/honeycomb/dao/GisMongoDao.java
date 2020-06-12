package com.hnf.honeycomb.dao;

import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.List;

/**
 * 用于MongoDB访问数据库方法
 *
 * @author yy
 */
public interface GisMongoDao {
    /**
     * 通过对应的数据库名以及对应的数据库表名以及条件对对应的数据进行查询
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      对应的查询条件
     * @return
     */
    List<Document> findInfoByDBNameAndGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query);

    /**
     * 通过对应的数据库名以及对应的数据库表名以及条件对对应的数据进行查询
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      对应的查询条件
     * @param sort       排序条件
     * @return
     */
    List<Document> findInfoByDBNameAndGatherNameAndQueryAndSort(
            String dbName, String gatherName, BasicDBObject query, BasicDBObject sort);

    /**
     * mapReduce
     *
     * @return
     */
    List<Document> mapReduceByDBNameAndGatherNameAndQuery(
        String dbName, String gatherName, String map, String reduce, BasicDBObject query
    );

}
