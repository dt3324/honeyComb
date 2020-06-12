package com.hnf.honeycomb.service.user;

import java.util.List;
import java.util.Map;

/**
 * 小蜜蜂业务层接口
 *
 * @author zhouhong
 */
public interface FetchService {

    /**
     * 对于小蜜蜂采集统计对应其的统计方法
     *
     * @param departmentCode 单位编码
     * @param pNumber        警号
     * @param page           页码
     * @param pageSize       每页显示的数目
     * @return
     */
    Map<String, Object> countSBeeCount(String departmentCode, String pNumber, Integer page, Integer pageSize);


    /**
     * 查询大小蜜蜂采集量
     *
     * @param departCode 部门代码
     * @param pNumber    警号
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @return
     */
    List<Map<String, Object>> countFetchLogSmallAndBig(String departCode,
                                                       String pNumber, String startDate, String endDate);
}
