package com.hnf.honeycomb.service.user;


import java.util.List;
import java.util.Map;

/**
 * 加密狗的增加修改以及统计的相关统计类
 *
 * @author yy
 */
public interface BbeeFetchService {

    /**
     * 通过对应条件查询对应加密狗的具体细节
     *
     * @param pageNumber   页码数
     * @param policeNumber 警号
     * @param type 采集来源（大蜜蜂 1，小蜜蜂 2，海鑫 3）
     * @param departCode   单位类编码
     * @param selectType   选择查询的加密狗类型,采集过为have,未采集过为no
     * @param bindType     对应加密狗是否绑定在人员身上的限制条件
     * @param pageSize     每页显示的数目
     * @return
     */
    Map<String, Object> findAll(Integer pageNumber,List<Integer> type, String policeNumber, String selectType,
                                String departCode, Integer bindType, Integer pageSize);

}
