package com.hnf.honeycomb.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 小蜜蜂数据交互层接口
 *
 * @author zhouhong
 */
//@Mapper
//@Repository
public interface FetchMapper {


    /**
     * 统计对应的小蜜蜂采集量,通过单位类型以及人员警号
     *
     * @param para
     * @return
     */
    public List<Map<String, Object>> findSBeeFetchLogByUnitTypeAndPNumber(Map<String, Object> para);

    /**
     * 新结构下统计对应单位的采集量统计
     *
     * @param para 对应的查询条件
     * @return 返回对应的记过结果
     */
    public List<Map<String, Object>> countSBeeFetch(Map<String, Object> para);

    /**
     * 新结构下统计对应单位的大蜜蜂采集量 去除重复的设备 （查询单位下采集的设备数）
     *
     * @param para 对应的条件
     * @return 对应的结果记录
     */
    public List<Map<String, Object>> countBBeeFetchDist(Map<String, Object> para);

 /**
     * 新结构下统计对应单位的大蜜蜂采集量
     *
     * @param para 对应的条件
     * @return 对应的结果记录
     */
    public List<Map<String, Object>> countBBeeFetch(Map<String, Object> para);

    /**
     * 新结构下统计最下级单位大蜜蜂的统计量
     *
     * @param para
     * @return
     */
    public List<Map<String, Object>> countBBeeFetchByCompleteDepartCodeOrPnumber(Map<String, Object> para);

/**
     * 新结构下统计最下级单位各个警员采集的手机数
     *
     * @param para
     * @return
     */
    public List<Map<String, Object>> countBBeeFetchByCompleteDepartCodeOrPnumberDist(Map<String, Object> para);

    /**
     * 新结构下统计最下级单位小蜜蜂统计量
     *
     * @param para
     * @return
     */
    public List<Map<String, Object>> countSBeeFetchByCompleteDepartCodeOrPnumber(Map<String, Object> para);
}
