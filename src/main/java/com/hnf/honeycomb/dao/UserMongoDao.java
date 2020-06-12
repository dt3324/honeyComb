package com.hnf.honeycomb.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * 采集基本信息Mongo
 *
 * @author yy
 */
public interface UserMongoDao {


    /**
     * 通过对应的key_id（大蜜蜂为加密狗号,小蜜蜂为警号）
     *
     * @param keyId         大蜜蜂为加密狗号,小蜜蜂为警号
     * @param newDepartCode 修改后对应的单位代码
     */
    void updataFetchByKeyId(String keyId, String newDepartCode);

    /**
     * 修改案件或设备或人员的单位信息
     *
     * @param newCode 修改后的单位信息
     * @param oldCode 修改的地方
     */
    void updateFetchByDoc(String newCode, String oldCode);

    /**
     * 插入操作日志
     *
     * @param dbName     数据库名
     * @param gatherName 表名
     * @param values     相关信息
     * @return
     */
    ObjectId insertOperationDocument(String dbName, String gatherName, Map<String, Object> values);

    /**
     * 查询日志
     *
     * @param dbName     数据库名
     * @param gatherName 集合名
     * @param query      条件
     * @param page       页码数
     * @param pageSize   每页显示数目
     * @param sort       排序
     * @return
     */
    List<DBObject> find(String dbName, String gatherName, BasicDBObject query, Integer page, Integer pageSize,
                        BasicDBObject sort);

    /**
     * 统计日志总数
     *
     * @param gatherName 集合名
     * @param query      条件
     * @param dbName
     * @return
     */
    Long count(String dbName, String gatherName, BasicDBObject query);


    /**
     * 统计QQ、微信等的百分比
     *
     * @param query      查询条件
     * @param dbName
     * @param gatherName
     * @param mapStr
     * @param reduceStr
     * @param query
     * @return
     */
    List<DBObject> mapReduce(String dbName, String gatherName, String mapStr, String reduceStr, BasicDBObject query);

    /**
     * 查询用户登录时间
     *
     * @param dbName
     * @param gatherName
     * @param query
     * @param sort
     * @param pageSize
     * @return
     */
    List<DBObject> findActiveTime(String dbName, String gatherName, BasicDBObject query, BasicDBObject sort, Integer pageSize);

    /**
     * 通过条件查询所有数据
     * @param query 条件
     * @return 查询数据
     */
    AggregateIterable<Document> findInfoData2(String dbName, String tableName, List<BasicDBObject> query);

    /**
     * 通过条件查询所有数据
     * @param query 条件
     * @param projection
     * @return 查询数据
     */
    List<Document> findInfoData2(String dbName, String tableName, BasicDBObject query, BasicDBObject projection);
}
