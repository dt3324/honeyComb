package com.hnf.honeycomb.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * 用于地理位置信息相关的查询
 *
 * @author yy
 */
public interface GeoService {

    /**
     * 统计输入对应账号中相关的位置信息以天区分
     *
     * @param searchNum  搜索的账号
     * @param userId     搜索对应人员的id
     * @param startDate  搜索对应账号位置的开始时间
     * @param endDate    搜索对应位置的结束时间
     * @param typeSelect 选择搜索账号的类型
     * @param timeLimit  选择搜索对应的时间间隔
     * @return
     */
    List<Map<String, Object>> countGeoCountPreDay(String searchNum, String userId,
                                                  String startDate, String endDate, String typeSelect, Integer timeLimit, String place);

    /**
     * 通过对应的账号查询对应点出现的信息
     *
     * @param searchNum  搜索的账号
     * @param startDate  搜索对应开始的时间
     * @param endDate    搜索对应结束的时间
     * @param typeSelect 账号对应的类型
     * @param timeLimit  时间间隔
     * @param page       对应的页数
     * @param pageSize   对应的页码
     * @return
     */
    List<Document> findGeoBySomeTerm(String searchNum,
                                     String startDate, String endDate, String typeSelect, Integer timeLimit, Integer page, Integer pageSize);

    /**
     * 查询一个对应的经纬度在一定半径圆中的出现的全部位置
     *
     * @param lat
     * @param lon
     * @param raduis
     * @param startDate
     * @param endDate
     * @param typeSelect
     * @param search
     * @return
     */
    Object gerWithinOnePoint1(String lat, String lon, Double raduis, String startDate, String endDate
            , String typeSelect, String search, String path);


    /**
     * 通过对应的账号查询对应点出现的信息(具体到某一天)
     *
     * @param searchNum  搜索的账号
     * @param date       具体时间
     * @param typeSelect 账号对应的类型
     * @param timeLimit  时间间隔
     * @param page       对应的页数
     * @param pageSize   对应的页码
     * @return
     */
    List<Document> findGeoByTime(String searchNum,
                                 String date, String typeSelect, Integer timeLimit, Integer page, Integer pageSize);
}
