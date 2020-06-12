package com.hnf.honeycomb.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;

/**
 * 采集基本信息Mongo
 *
 * @author yy
 */
public interface DeviceMongoDao {
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
     * @return
     */
    List<Document> findInfoByDBNameAndGatherNameAndQueryBcp(
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

    /**
     * 查询对应的数据不分页
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的集合名称
     * @param query      对应的查询条件
     * @return 返回查询结果
     */
    List<Document> findInfoByGatherNameAndQueryAll(String dbName, String gatherName, BasicDBObject query);

    /**
     * 对对应的数据库以及对应的集合进行聚合
     *
     * @param dbName     数据库名
     * @param gatherName 集合名
     * @param asList     对应的聚合条件
     * @return 返回聚合结果
     */
    List<Document> aggregateByGatheNameAndDBNameAndQuery(
            String dbName, String gatherName, List<BasicDBObject> asList);

    /**
     * 只查询数据库中的一些信息
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的集合名
     * @param query      对应的查询条件
     * @param fileds     只返回的字段
     * @return
     */
    List<Document> findSomeFiledsByGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query, BasicDBObject fileds);

    /**
     * 对对应的数据库以及对应的集合进行聚合统计
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的集合名
     * @param para       对应的查询条件
     * @return
     */
    Long countByQueryAndDBAndCollName(String dbName, String gatherName, List<BasicDBObject> para);

    /**
     * 通过对应的数据库名以及对应的数据库表名以及条件对对应的数据进行查询统计
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      对应的查询条件
     * @return
     */
    List<Document> findByDBNameAndGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query ,Long start , Long end);

//**********************************************标采人员相应接口

    /**
     * 通过数据库名以及对应的的相关条件从数据库中查询标采人员数据
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的集合名称
     * @param query      对应的查询条件
     * @param page       对应的页数
     * @param pageSize   对应的页码
     * @return 返回查询结果
     */
    List<Document> findSisInfoByGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query, Integer page, Integer pageSize);


    /**
     * 通过对应的数据库名以及对应的数据库表名以及条件对对应的标采人员数据进行查询统计
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      对应的查询条件
     * @return
     */
    Long countSisByDBNameAndGatherNameAndQuery(
            String dbName, String gatherName, BasicDBObject query);

    /**
     * 标采统计（超级管理员或者高级用户，默认查看全部）
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      条件
     * @return
     */
    List<Document> groupSisAllCount(String dbName, String gatherName, BasicDBObject query);

    /**
     * 标采统计（单位管理员）
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      条件
     * @return
     */
    List<Document> groupSisUnitCount(String dbName, String gatherName, BasicDBObject query);


    /**
     * 标采统计（部门管理员和普通用户）
     *
     * @param dbName         对应的数据库名
     * @param gatherName     对应的表名
     * @param query          条件
     * @param departmentCode 单位编码
     * @return
     */
    List<DBObject> groupSisPersonCount(String dbName, String gatherName, BasicDBObject query, String departmentCode);


    /**
     * 标采统计（按年）
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      条件
     * @return
     */
    List<DBObject> groupSisPersonPreYear(String dbName, String gatherName, BasicDBObject query);

    /**
     * 标采统计（按月）
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      条件
     * @return
     */
    List<DBObject> groupSisPersonPreMonth(String dbName, String gatherName, BasicDBObject query);


    /**
     * 标采统计（按日）
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @param query      条件
     * @return
     */
    List<DBObject> groupSisPersonPreDay(String dbName, String gatherName, BasicDBObject query);

    /**
     * 获取最大mongoDB的ID
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @return
     */
    Long maxId(String dbName, String gatherName);

    /**
     * 获取最大mongoDB的更新时间
     *
     * @param dbName     对应的数据库名
     * @param gatherName 对应的表名
     * @return
     */
    Long maxUpdateTime(String dbName, String gatherName);

    /**
     * 入库操作
     *
     * @param dbName     数据库名
     * @param gatherName 表名
     * @param docs       需要入库的数据
     */
    void insertDocs(String dbName, String gatherName, List<Document> docs);

//***********************设备相关接口

    /**
     * 对对应的数据库以及对应的集合进行聚合
     *
     * @param dbName     数据库名
     * @param gatherName 集合名
     * @param asList     对应的聚合条件
     * @return 返回聚合结果
     */
    List<Document> aggregateDeviceByGatheNameAndDBNameAndQuery(
            String dbName, String gatherName, List<BasicDBObject> asList);

    /**
     * 对对应的数据库进行统计
     *
     * @param dbName     数据库名
     * @param gatherName 集合名
     * @param query      对应的查询条件
     * @return
     */
    Long countByGatherNameAndDBNameAndQuery(String dbName, String gatherName, BasicDBObject query);


    /***
     * 查询通话记录
     * @param dbName 数据库名
     * @param gatherName 表名
     * @param deviceUnique 设备唯一标识
     * @return
     */
    BasicDBList record(String dbName, String gatherName, String deviceUnique);

    /**
     * 修改案件或设备或人员的单位信息
     *
     * @param dbName      数据库名
     * @param gatherName  表名
     * @param newDocument 修改后的单位信息
     * @param searchQuery 修改的地方
     */
    void updateFetchByDoc(String dbName, String gatherName, BasicDBObject searchQuery, BasicDBObject newDocument);

    /**
     * 插入操作日志
     *
     * @param dbName     数据库名
     * @param gatherName 表名
     * @param values     相关信息
     * @return
     */
    ObjectId insertOperationDocument(String dbName, String gatherName, HashMap<String, Object> values);

    /**
     * 插入单条数据
     *
     * @param database   数据库名
     * @param collection 集合名
     * @param document   需要插入的数据
     */
    void insertDocument(String database, String collection, Document document);

    /**
     * 修改文档
     *
     * @param dbName     数据库名
     * @param gatherName 表名
     * @param queryDoc   查询doc
     * @param newDoc     更新doc
     * @return 获取修改的计数
     */
    Long updateDocument(String dbName, String gatherName, Document queryDoc, Document newDoc);

    void insertExcel(String dbname, String fileorder, List<Document> account_orderList);
}
