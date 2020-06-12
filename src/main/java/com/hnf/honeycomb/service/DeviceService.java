package com.hnf.honeycomb.service;

import java.util.List;
import java.util.Map;

/**
 * 设备数据业务层接口
 *
 * @author zhouhong
 */
public interface DeviceService {

    /**
     * @param page           页码数
     * @param deviceName     设备名称
     * @param deviceType     设备类型Android或者ios
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param policeNumber   警号
     * @param departmentCode 部门code
     * @param mineOnly       仅查看个人采集数据
     * @param qq
     * @param wx
     * @param phone
     * @param gps
     * @return
     */
    Map<String, Object> findDeviceBySomeTerms(Integer page, String deviceName, Integer deviceType
            , String startTime, String endTime
            , String policeNumber, String departmentCode, Integer mineOnly
            , Integer qq, Integer wx, Integer phone, Integer gps, Integer collType, String queryPoliceNumber);

    /**
     * 通过设备唯一标识查询设备相关人员、案件
     *
     * @param deviceUnique 设备唯一标识
     * @return
     */
    Map findByDeviceUnique(String deviceUnique);

    /**
     * 查询设备详细信息
     *
     * @param userId       用户ID
     * @param deviceUnique 设备唯一标识
     * @param place        登录IP
     * @return
     */
    List<Map<String,Object>> queryByDeviceUnique(Integer userId, String deviceUnique, String place);

    /**
     * 查询设备采集历史
     *
     * @param deviceUnique 设备唯一标识
     * @return
     */
    List<Object> findLogByDeviceUnique(String deviceUnique);


    /**
     * 校验用户是否有权限查看设备详情
     *
     * @param deviceUnique          设备唯一标识
     * @param permission            查看数据权限
     * @param softDogOrPoliceNumber 警号或加密狗号
     * @return
     */
    Boolean isNotCheck(String deviceUnique, String permission, String softDogOrPoliceNumber);

}
