package com.hnf.honeycomb.service.user;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Map;

/**
 * 设备业务层接口
 *
 * @author zhouhong
 */
public interface UserDeviceService {

    /**
     * 通过用户ID以及对应的时间查询统计对应的采集的安卓版本号
     *
     * @param departmentCode    对应的用户部门Code
     * @param startTime 对应搜索采集的设备的开始时间
     * @param endTime   对应搜索采集设备的结束时间
     * @return
     */
    Map<String, Object> aggregateDeviceInfo(String departmentCode, Long startTime, Long endTime);
    /**
     * 统计案件 设备 人员详情
     * @return
     * @ userId
     * @ startTime
     * @ endTime
     */
    Map<String, Object> aggregateCaseDevicePersonInfo(String personKeyWord,String departmentCode, int pageNum, int pageSize, String wx,
                                                      String qq, String phone, String gps, Long startTime, Long endTime);

    /**
     * 统计QQ WX 通讯录的百分比
     * @param map 请求参数
     * @return 返回结果
     */
    Map<String, Object> fetchPercentage(Map<String, Object> map);

    /**
     * @param map 条件
     * @return 结果
     */
    Map<String, Object> fetchEntiretyPercentage(Map<String, Object> map) throws ParseException;

    /**
     * 采集质量导出excel
     * @param path 导出的位置
     * @param departmentCode 单位代码
     * @param pageNum 第几页
     * @param pageSize 每页条数
     * @param wx 是否筛选微信
     * @param qq 是否筛选QQ
     * @param fetchPhone 电话筛选
     * @param gps 定位筛选
     * @param startTime 开始i时间
     * @param endTime 结束时间
     * @param response 相应
     * @return 导出结果
     */
    Map<String, String> fetchQualityExportExcel(String personKeyWord,String path, String departmentCode, Integer pageNum, Integer pageSize, String wx,
                                 String qq, String fetchPhone, String gps, Long startTime, Long endTime,
                                 HttpServletResponse response);

    /**
     * 采集数据详情
     * @param unit 单位代码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> aggregateDataInfo(String unit, Long startTime, Long endTime);


    /**
     * 下载excel
     * @param fileName 文件名字
     * @param response 响应
     */
    void downloadExcel(String fileName, HttpServletResponse response);
}
