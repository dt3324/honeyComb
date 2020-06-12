package com.hnf.honeycomb.dao;

import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.List;

/**
 * 采集基本信息Mongo
 *
 * @author yy
 */
public interface GisBaseMongoDao {
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
     * 通过数据库名以及对应的的相关条件从数据库中查询数据
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的集合名称
     * @param query      对应的查询条件
     * @param sort       对应的排序条件
     * @param page       对应的页数
     * @param pageSize   对应的页码
     * @return 返回查询结果
     */
    List<Document> findInfoByGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query, BasicDBObject sort, Integer page, Integer pageSize);

}
