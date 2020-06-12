package com.hnf.honeycomb.service;

import com.mongodb.DBObject;
import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 人员数据业务层接口
 *
 * @author zhouhong
 */
public interface DevicePersonService {

    /**
     * 查询人员列表
     *
     * @param pageNumber     页码数
     * @param personName     人员姓名
     * @param userNumber     证件号
     * @param departmentCode 单位编码
     * @param caseTypeId     要查询的案件类型范围
     * @return
     */
    Map<String, Object> personList(Integer pageNumber, String personName, String userNumber, List caseTypeId, String departmentCode);

    /**
     * 查询人员相关的案件和设备信息
     *
     * @param caseUnique   案件唯一标识
     * @param deviceUnique 设备唯一标识
     * @return
     */
    Map queryByUnique(String caseUnique, String deviceUnique);

    /**
     * 通过设备唯一标识查询人员信息
     *
     * @param deviceUnique 设备唯一标识
     * @return
     */
    List<Document> personQueryByDeviceUnique(String deviceUnique);


    /**
     * 通过对应的条件查询出对应的单位下的标采人员信息
     *
     * @param policeNumber       警号
     * @param searchPersonNumber 被采人员证件号
     * @param personName         被采人员姓名
     * @param personNumber       人员编号
     * @param isGenerateBCP      是否生成BCP包
     * @param isUploadSuccess    是否成功上传省标采
     * @param personSerialNum    警员身份证号
     * @param personType         人员类型
     * @param page               页码数
     * @param pageSize           每页显示的数目
     * @return
     */
    Map findSisPerson(String policeNumber, String searchPersonNumber, String personName
            , String personNumber, String isGenerateBCP, String isUploadSuccess, String personSerialNum
            , String personType, Integer page, Integer pageSize, String departmentCode, Integer departmentType);


    /**
     * 标采人员的统计量
     *
     * @param policeNum 警号
     * @param unitNum   单位类型编码
     * @param departNum 部门类型编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    Map<String, List<DBObject>> analysisSisPerson(String policeNumber, String departNum, Integer departmentType
            , Long startTime, Long endTime);

    /**
     * 按时间统计标采人员
     *
     * @param policeNum 警号
     * @param unitNum   单位类型编码
     * @param departNum 部门类型编码
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param type
     * @return
     */
    List<DBObject> analysisSisPersonPreDate(String policeNumber, String departNum, Integer departmentType
            , Long startTime, Long endTime, String type);


    /**
     * 查询上传省标采详情
     *
     * @param unitNum     对应的单位编码
     * @param departNum   对应的部门编码
     * @param personIdNum 对应的人员编号
     * @param scjgType    对应的上传结果类型
     * @return
     */
    List<Document> findUploadSisFalseDetail(String departNum, String personIdNum, Integer scjgType);

}
