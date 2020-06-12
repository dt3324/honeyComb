package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.mapper.SoftDogMapper;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.service.user.BbeeFetchService;
import com.hnf.honeycomb.util.BuilderMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * @author admin
 */
@Service("BbeeFetchService")
public class BbeeFetchServiceImpl implements BbeeFetchService {

    private static final Logger logger = LoggerFactory.getLogger(BbeeFetchServiceImpl.class);

    @Resource
    private SoftDogMapper softDogMapper;

    @Autowired
    private DepartmentMapper departmentMapper;


    @Override
    public Map<String, Object> findAll(Integer pageNumber,List<Integer> type, String policeNumber, String selectType,
                                       String departCode, Integer bindTypeField, Integer pageSize) {
        Map<String, Object> para = new HashMap<>(8);
        Map<String, Object> departPara = new HashMap<>(8);
        if (pageNumber == null || pageSize == null) {
            throw new RuntimeException("对应的分页条件为空");
        }

        if(type != null){
            para.put("type", type);
            departPara.put("type", type);
        }
        if (!com.hnf.honeycomb.util.StringUtils.isEmpty(policeNumber)) {
            para.put("policeNumber", policeNumber.trim());
        }
        //对其采集以及未采集过进行对应的统计
        String have = "have";
        if (have.equals(selectType)) {
            para.put("fetchCount", 0);
        }
        //前端是未采集时的条件
        String no = "no";
        if (no.equals(selectType)) {
            para.put("fetchCount1", 0);
        }
        if (!StringUtils.isEmpty(departCode)) {
            para.put("departCode", com.hnf.honeycomb.util.StringUtils.getOldDepartmentCode(departCode));
        }
        logger.info("大蜜蜂采集管理查询条件 ->{}" + para);
        //统计每个人的采集的次数
        List<Map<String, Object>> results = softDogMapper.findSoftDogDetailBySomeCondition(para);
        //统计每个人采集手机的部数
        List<Map<String, Object>> resultsDist = softDogMapper.findSoftDogDetailBySomeConditionDist(para);
        HashMap<Object, Object> hashMap = new HashMap<>();
        //每一个警号对应的采集部数
        resultsDist.forEach(r -> hashMap.put(r.get("policeNumber"), r.get("fetchCount")));

        //如果查到数据需要把部门名字获取到
        //把没有警号的删除
        int nullPoliceNumber = -1;
        if (!CollectionUtils.isEmpty(results)) {
            //这list放每天警号的采集
            List<Map<String, Object>> resultsAll = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> t = results.get(i);
                String policeNumber1 = getString(t.get("policeNumber"));
                //如果没填警号
                if(StringUtils.isEmpty(policeNumber1)){
                    nullPoliceNumber = i;
                    para.remove("start");
                    para.remove("end");
                    para.put("policeNumberIsNull", 1);
                    //查出所有没填警号的部门  以及每个部门的采集次数
                    List<Map<String, Object>> results1 = softDogMapper.findDepartByPoilceNumIsNull(para);
                    for (Map<String, Object> r : results1) {
                        DepartmentBean department = departmentMapper.findByDepartmentCode(r.get("department_code").toString());
                        HashMap<String, Object> map = new HashMap<>(4);
                        map.put("departCode", r.get("department_code"));
                        map.put("policeNumberIsNull", "1");
                        map.put("type", type);
                        //查出所有没填警号的部门  以及每个部门的采集部数
                        List<Map<String, Object>> policeNumberIsNull = softDogMapper.findSoftDogDetailBySomeConditionDistAndPNisNull(map);
                        r.put("deviceCount", policeNumberIsNull.get(0).get("fetchCount"));
                        //如果单位代码填写错误
                        if(department == null){
                            r.put("department_name", r.get("department_code").toString() + "：对应的部门不存在请从单位管理中添加");
                            r.put("departNames", new ArrayList<>());
                            r.put("departCount", 0);
                            continue;
                        }
                        String departmentName = department.getDepartmentName();
                        r.put("departNames", Arrays.asList(departmentName));
                        r.put("departCount", 1);
                        String deptCode = "department_code";
                        if (r.get(deptCode) != null) {
                            String departmentCode = r.get(deptCode).toString();
                            DepartmentBean departmentBean = departmentMapper.findByDepartmentCode(departmentCode);
                            String deptName = "department_name";
                            if(departmentBean == null){
                                r.put(deptName, departmentCode + "：对应的部门不存在请从单位管理中添加");
                            }else {
                                r.put(deptName, departmentBean.getDepartmentName());
                            }
                        }
                    }
                    resultsAll.addAll(results1);
                }else {
                    //查询警员在哪些单位有采集
                    departPara.put("policeNumber", getString(t.get("policeNumber")));
                    List<String> departCount = softDogMapper.findDeparts(departPara);
                    ArrayList<String> list = new ArrayList<>();
                    for (String s : departCount) {
                        DepartmentBean byDepartmentCode = departmentMapper.findByDepartmentCode(s);
                        if(byDepartmentCode!= null){
                            list.add(byDepartmentCode.getDepartmentName());
                        }
                    }
                    //有多少个采集单位
                    t.put("departCount", departCount.size());
                    //采集单位列表
                    t.put("departNames", list);
                    //采集的设备数
                    t.put("deviceCount", hashMap.get(t.get("policeNumber")));
                    String deptCode = "department_code";
                    //查询警员当前所在的单位
                    if (t.get(deptCode) != null) {
                        String departmentCode = t.get(deptCode).toString();
                        DepartmentBean departmentBean = departmentMapper.findByDepartmentCode(departmentCode);
                        String deptName = "department_name";
                        if(departmentBean == null){
                            logger.info(departmentCode + "，没有找到对应的单位信息。");
                            t.put(deptName, departmentCode + "：对应的部门不存在请从单位管理中添加");
                        }else {
                            t.put(deptName, departmentBean.getDepartmentName());
                        }
                    }
                }

            }
            //第一次统计如果存在没填写警号的  删除该项
            if(nullPoliceNumber != -1) {
                results.remove(nullPoliceNumber);
            }
            //把没有填警号的数据添加进集合
            results.addAll(resultsAll);
        }
        Map<String, Object> returnMap = new HashMap<>(5);
        returnMap.put("dogs", results);
        if (results != null) {
            //排序
            results.sort((o1, o2) -> getInteger(o2.get("fetchCount")) - getInteger(o1.get("fetchCount")));
            int start = (pageNumber - 1) * pageSize;
            int end = (pageNumber - 1) * pageSize + pageSize < results.size() ? (pageNumber - 1) * pageSize + pageSize : results.size();
            //分页
            returnMap.put("dogs", results.subList(start,end));
            // 统计大蜜蜂有采集过
            Long isCollectDogCount = results.stream().
                    filter(t -> Integer.valueOf(t.get("fetchCount").toString()) > 0).count();
            int size = results.size();
            returnMap.put("count", size);
            returnMap.put("countPage", Math.ceil(size / pageSize.doubleValue()));
            returnMap.put("havedata", isCollectDogCount);
            returnMap.put("nodata", (size - isCollectDogCount));
        }
        return returnMap;
    }
}
