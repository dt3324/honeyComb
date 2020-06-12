package com.hnf.honeycomb.service.user;

import java.util.Map;


/**
 * 审计管理业务层接口
 *
 * @author zhouhong
 */
public interface LogService {

    int PAGESIZE = 100;

    /**
     * 查询所有日志
     *
     * @param searchContent  搜索内容
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @param departmentCode 单位编码
     * @return
     */
    Map findAllLog(String searchContent, String startDate, String endDate, String departmentCode) throws Exception;


    /**
     * 根据类型编码查询各个模块日志
     *
     * @param type           类型编码
     * @param pageNumber     页码数
     * @param searchContent  搜索内容
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @param departmentCode 单位编码
     * @return
     */
    Map<String, Object> findLogByType(Integer type, Integer pageNumber, String searchContent, String startDate, String endDate, String departmentCode) throws Exception;
}
