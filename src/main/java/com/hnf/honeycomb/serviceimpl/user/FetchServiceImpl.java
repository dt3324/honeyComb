package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.remote.user.FetchLogMapper;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import com.hnf.honeycomb.service.user.FetchService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 小蜜蜂业务层接口实现
 *
 * @author zhouhong
 */
@Service("fetchService")
public class FetchServiceImpl implements FetchService {

    @Resource
    private FetchLogMapper fetchLogMapper;

    private final UserDepartmentService userDepartmentService;

    @Autowired
    public FetchServiceImpl(UserDepartmentService userDepartmentService) {
        this.userDepartmentService = userDepartmentService;
    }

    @Override
    public Map<String, Object> countSBeeCount(String departmentCode, String pNumber, Integer page, Integer pageSize) {
        // TODO Auto-generated method stub
        if (page == null || pageSize == null) {
            throw new RuntimeException("对应的分页条件有误");
        }
        Map<String, Object> result = new HashMap<>(4);
        Map<String, Object> para = new HashMap<>(4);
        if (!StringUtils.isEmpty(departmentCode)) {
            para.put("departCode", departmentCode.trim());
        }
        if (!StringUtils.isEmpty(departmentCode) && "510107999999".equals(departmentCode)) {
            para.put("departCode", null);
        }
        if (pNumber != null && !pNumber.trim().isEmpty()) {
            para.put("policeNumber", pNumber);
        }
        List<Map<String, Object>> allFetchs = fetchLogMapper.findSBeeFetchLogByUnitTypeAndPNumber(para);
        Integer fetchUserCount = 0;
        Integer fetchDeviceCount = 0;
        if (!CollectionUtils.isEmpty(allFetchs)) {
            fetchUserCount = allFetchs.size();
            for (Map<String, Object> t : allFetchs) {
                Object total = t.get("total");
                if (total != null) {
                    fetchDeviceCount += Integer.valueOf(total.toString());
                }
            }
        }
        para.put("start", (page - 1) * pageSize);
        para.put("end", pageSize);
        List<Map<String, Object>> sBeeFetch = countSBeeCount1(departmentCode, pNumber, page, pageSize);
        result.put("fetct", sBeeFetch);
        result.put("fetchUserCount", allFetchs.size());
        result.put("fetchDeviceCount", fetchDeviceCount);
        result.put("countPages", (int) (Math.ceil(fetchUserCount.doubleValue() / pageSize.doubleValue())));
        return result;
    }

    @Override
    public List<Map<String, Object>> countFetchLogSmallAndBig(String departCode, String pNumber,
                                                              String startDate, String endDate) {
        // TODO Auto-generated method stub
        Map<String, Object> para = new HashMap<>(6);
        if (startDate != null && !startDate.isEmpty()
                && endDate != null && !endDate.isEmpty()) {
            //这里的日期格式必须跟前台传过来的一样
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            //转换成时间戳
            Long startTime = null;
            Long endTime = null;
            try {
                startTime = format.parse(startDate).getTime();
                endTime = format.parse(endDate).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            para.put("startDate", startTime);
            para.put("endDate", endTime);
        }
        //获取对应的单位代码长度
        int codeLength = 0;
        //若单位代码长度为空,则为查询全部
        if (!StringUtils.isEmpty(departCode)) {
            //获取对应的长度
            departCode = StringUtils.getOldDepartmentCode(departCode.trim());
            codeLength = departCode.trim().length();
        }
        String deptCode = "510107999999";
        if (!StringUtils.isEmpty(departCode) && deptCode.equals(departCode)) {
            codeLength = 2;
            departCode = "51";
        }
        List<Map<String, Object>> sBeeFetch;
        List<Map<String, Object>> bBeeFetch;
        List<Map<String, Object>> bBeeDevice;
        HashMap<Object, Object> bBeeDeviceMap = new HashMap<>();
        switch (codeLength) {
            //代表查看最先权限为省级
            case 0:
                para.put("codeLength", codeLength + 2);
//                sBeeFetch = fetchLogMapper.countSBeeFetch(para);
                bBeeFetch = fetchLogMapper.countBBeeFetch(para);
                //单位下采集手机部数
                bBeeDevice = fetchLogMapper.countBBeeFetchDist(para);
                bBeeDevice.forEach(b->bBeeDeviceMap.put(b.get("code"),b.get("total")));
                break;
            //代表查看最小单位为市级
            case 2:
                para.put("codeLength", codeLength + 2);
                para.put("departCode", departCode.trim());
//                sBeeFetch = fetchLogMapper.countSBeeFetch(para);
                //大蜜蜂单位下采集次数
                bBeeFetch = fetchLogMapper.countBBeeFetch(para);
                //单位下采集手机部数
                bBeeDevice = fetchLogMapper.countBBeeFetchDist(para);
                bBeeDevice.forEach(b->bBeeDeviceMap.put(b.get("code"),b.get("total")));
                break;
            //代表查看最小单位为区级
            case 4:
                //统计县级6需除去以510700开头的
                para.put("codeLength", codeLength + 2);
                para.put("departCode", departCode.trim());
//                sBeeFetch = fetchLogMapper.countSBeeFetch(para);
                bBeeFetch = fetchLogMapper.countBBeeFetch(para);
                //单位下采集手机部数
                bBeeDevice = fetchLogMapper.countBBeeFetchDist(para);
                bBeeDevice.forEach(b->bBeeDeviceMap.put(b.get("code"),b.get("total")));
                break;
            //代表最小单位为部门（县级）
            case 6:
                para.put("departCode", departCode.trim());
                para.put("codeLength", departCode.length() + 5);
//                sBeeFetch = fetchLogMapper.countSBeeFetch(para);
                bBeeFetch = fetchLogMapper.countBBeeFetch(para);
                //单位下采集手机部数
                bBeeDevice = fetchLogMapper.countBBeeFetchDist(para);
                bBeeDevice.forEach(b->bBeeDeviceMap.put(b.get("code"),b.get("total")));
                break;
            //代表为查询最小单位下的人员
            default:
                para.put("codeLength", codeLength);
                para.put("departCode", departCode.trim());
                if (!StringUtils.isEmpty(pNumber)) {
                    para.put("pNumber", pNumber.trim());
                }
//                sBeeFetch = fetchLogMapper.countSBeeFetchByCompleteDepartCodeOrPnumber(para);
                bBeeFetch = fetchLogMapper.countBBeeFetchByCompleteDepartCodeOrPnumber(para);
                bBeeDevice = fetchLogMapper.countBBeeFetchByCompleteDepartCodeOrPnumberDist(para);
                bBeeDevice.forEach(b->bBeeDeviceMap.put(b.get("code"),b.get("total")));
                break;
        }
//        Map<String, Map<String, Object>> unitToSbeeFetch = getCode2Count(sBeeFetch);
        for (Map<String, Object> one : bBeeFetch) {
//            if (one.get("code") == null) {
//                //若小蜜蜂全部均有单位,大蜜蜂存在没有单位时
//                Map<String, Object> sBeeTotalMap = unitToSbeeFetch.get("unknown");
//                Object sBeeTotal = 0;
//                if (!CollectionUtils.mapIsEmpty(sBeeTotalMap)) {
//                    sBeeTotal = sBeeTotalMap.get("total");
//                }
//                one.put("sBeeTotal", sBeeTotal != null ? sBeeTotal : 0);
//                unitToSbeeFetch.remove("unknown");
//                continue;
//            }
            String unitName = one.get("code").toString();
            one.put("deviceTotal",bBeeDeviceMap.get(unitName));
//            Map<String, Object> sbf = unitToSbeeFetch.get(unitName) != null ? unitToSbeeFetch.get(unitName) :
//                    new HashMap<>(0);
//            Integer sBeeTotal = sbf.get("total") != null ? Integer.valueOf(
//                    sbf.get("total").toString()) : 0;
//            unitToSbeeFetch.remove(unitName);
//            one.put("sBeeTotal", sBeeTotal);
        }
        //暂时没有小蜜蜂采集，暂不需要以下步骤
        //大蜜蜂没有统计的信息
//        if (!unitToSbeeFetch.isEmpty()) {
//            Set<Entry<String, Map<String, Object>>> set = unitToSbeeFetch.entrySet();
//            for (Entry<String, Map<String, Object>> one : set) {
//                String unitName = one.getKey();
//                Object total = one.getValue().get("total");
//                Map<String, Object> oneFetch = new HashMap<>(4);
//                oneFetch.put("code", unitName);
//                oneFetch.put("sBeeTotal", total);
//                oneFetch.put("name", one.getValue().get("name"));
//                oneFetch.put("total", 0);
//                bBeeFetch.add(oneFetch);
//            }
//        }
        return sortFetch(bBeeFetch);

    }

    /**
     * 对采集统计后的数据进行排序
     */
    private List<Map<String, Object>> sortFetch(List<Map<String, Object>> fetches) {
        if (fetches == null || fetches.isEmpty()) {
            return fetches;
        }
        fetches.sort((f1, f2) -> -Integer.valueOf(f1.get("total").toString()).compareTo(
                Integer.valueOf(f2.get("total").toString())));
        return fetches;
    }


    /**
     * 获取单位名称与小蜜蜂统计量的关系
     */
    private Map<String, Map<String, Object>> getCode2Count(List<Map<String, Object>> sBeeFetch) {
        Map<String, Map<String, Object>> result = new HashMap<>(sBeeFetch.size());
        if (sBeeFetch.isEmpty()) {
            return result;
        }
        for (Map<String, Object> one : sBeeFetch) {
            Object nameObj = one.get("code");
            String name = nameObj != null ? nameObj.toString() : "unknown";
            result.put(name, one);
        }
        return result;
    }

    private List<Map<String, Object>> countSBeeCount1(String departCode, String pNumber, Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            throw new RuntimeException("查询的分页条件有误");
        }
        Map<String, Object> para = new HashMap<>(4);
        para.put("start", (page - 1) * pageSize);
        para.put("end", pageSize);
        if (!StringUtils.isEmpty(departCode)) {
            para.put("departCode", departCode.trim());
        }
        if (!StringUtils.isEmpty(departCode) && "510107999999".equals(departCode)) {
            para.put("departCode", null);
        }
        if (pNumber != null && !pNumber.trim().isEmpty()) {
            para.put("policeNumber", pNumber);
        }
        List<Map<String, Object>> result = fetchLogMapper.findSBeeFetchLogByUnitTypeAndPNumber(para);
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(t -> {
                Object departmentCode = t.get("department_code");
                if (departmentCode != null && !StringUtils.isEmpty(departmentCode.toString())) {
                    String newDepartmentName = userDepartmentService
                            .findWholeDepartmentNameByDepartCode(departmentCode.toString());
                    t.put("departmetnName", newDepartmentName);
                }
            });
        }
        return result;
    }
}
