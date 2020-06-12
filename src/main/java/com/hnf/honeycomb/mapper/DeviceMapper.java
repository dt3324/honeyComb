package com.hnf.honeycomb.mapper;

import java.util.List;
import java.util.Map;

/**
 * 设备数据持久层
 *
 * @author zhouhong
 */
//@Mapper
//@Repository
public interface DeviceMapper {

    /**
     * 查询最大ID值
     *
     * @return
     */
    Integer findMaxFetchLogId();


    /**
     * 查询大蜜蜂采集日志
     *
     * @param id mongodb最大ID
     * @return
     */
    List<Map<String, Object>> findFetchLogMore(Long id);

    /**
     * 查询小蜜蜂采集日志
     *
     * @param id mongodb最大ID
     * @return
     */
    List<Map<String, Object>> findMoreSmallFetchLog(Long id);

    /**
     * 查询对应加密狗号采集的某部设备的采集次数
     *
     * @param map
     * @return
     */
    Integer countUpLoadTimeByDUniqueAndDogNum(Map map);


    /**
     * 通过设备id查询对应的小蜜蜂采集日志相关信息,包含对应的上传时间以及上传的单位名称以及上传的部门名称
     *
     * @param deviceUnique 对应的设备唯一标识
     * @return 返回查询的采集日志的map集合
     */
    List<Object> findAboutFetchLogByDeviceUnique(String deviceUnique);


    /**
     * 通过设备id查询对应的大蜜蜂采集日志相关信息,包含对应的上传时间以及上传的单位名称以及上传的部门名称
     *
     * @param deviceUnique 对应的设备唯一标识
     * @return 返回查询的采集日志的map集合
     */
    List<Object> findSBeeFetchDetailByDeviceUnique(String deviceUnique);

    List<String> findDepartmentCodeByUnique(String device);
}
