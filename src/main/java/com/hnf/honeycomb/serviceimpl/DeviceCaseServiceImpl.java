package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.service.DeviceCaseService;
import com.hnf.honeycomb.util.BuilderMap;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.RedisUtilNew;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 案件业务层接口实现
 *
 * @author zhouhong
 */
@Service
public class DeviceCaseServiceImpl implements DeviceCaseService {

    private final DeviceMongoDao deviceMongoDao;

    private final RedisUtilNew redisUtilNew;

    @Autowired
    public DeviceCaseServiceImpl(DeviceMongoDao deviceMongoDao, RedisUtilNew redisUtilNew) {
        this.deviceMongoDao = deviceMongoDao;
        this.redisUtilNew = redisUtilNew;
    }

    @Override
    public Map<String, Object> findCaseByUnitTypeOrDog(Integer page, String caseName, String departmentCode) {
        List<Document> findCaseBase = new ArrayList<>();
        //每次查询的所有条件组为key
        String keyNameCount = page + departmentCode + caseName + "count";
        String keyNameList = page + departmentCode + caseName + "List";
        Map<String, Object> redisCaseMap = null;
//        Map<String, Object> redisCaseMap = (Map<String, Object>) redisUtilNew.get(DeviceServiceImpl.redisCaseName);
        //如果是第一次 避免空指针需要给它new一个
        if(redisCaseMap == null){
            redisCaseMap = new HashMap<>(2);
        }
        Long count;
        if (redisCaseMap.size() > 0) {
            count = (Long) redisCaseMap.get(keyNameCount);
            findCaseBase = (List<Document>)redisCaseMap.get(keyNameList);
            if(count != null && findCaseBase != null){
                return BuilderMap.of(String.class, Object.class)
                        .put("count", count)
                        .put("cases", redisCaseMap.get(keyNameList))
                        .put("totalPage", Math.ceil(count / 50d))
                        .get();
            }
        }
        if (page == null || page == 0) {
            throw new RuntimeException("传入的分页条件有误");
        }
        BasicDBObject queryObj = new BasicDBObject();
        BasicDBObject findQuery2 = new BasicDBObject();
        if (!StringUtils.isEmpty(departmentCode)) {
            departmentCode = StringUtils.getOldDepartmentCode(departmentCode);
            Pattern pattern = Pattern.compile("^.*" + departmentCode + ".*$", Pattern.CASE_INSENSITIVE);
            queryObj.put("departmentCode", pattern);
        }
        if (!StringUtils.isEmpty(caseName)) {
            //模糊查询
            Pattern pattern = Pattern.compile("^.*" + DeviceServiceImpl.dispose(caseName) + ".*$", Pattern.CASE_INSENSITIVE);
            queryObj.put("caseName", pattern);
        }
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$caseUniqueMark"));
        BasicDBObject start = new BasicDBObject("$skip", (page - 1) * 50);
        BasicDBObject size = new BasicDBObject("$limit", 50);
        List<Document> caseData = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                "infoData2", "caseAbstract",
                Arrays.asList(new BasicDBObject("$match", queryObj), group, start, size)
        );
        if(caseData !=null){
            ArrayList<String> caseUniqueMarks = new ArrayList<>();
            for (Document cd : caseData) {
                String id = cd.getString("_id");
                if (id != null && !"".equals(id)) {
                    caseUniqueMarks.add(id);
                }
            }
            findQuery2.put("caseuniquemark", new BasicDBObject("$in",caseUniqueMarks));
            List<Document> infoByGatherNameAndQuery = deviceMongoDao.findInfoByGatherNameAndQueryAll("infoData2",
                    "t_case", findQuery2);
            if(findCaseBase == null){
                findCaseBase = new ArrayList<>();
            }
            findCaseBase.addAll(infoByGatherNameAndQuery);
        }
        BasicDBObject group1 = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("caseUniqueMark", "$caseUniqueMark"))
                .append("count", new BasicDBObject("$sum", 1))
        );
        count = deviceMongoDao.countByQueryAndDBAndCollName("infoData2", "caseAbstract", Arrays.asList(new BasicDBObject("$match", queryObj), group1));
        //新查出的数据添加到redis中  fetchData ： { redisPersonMap ：{keyNameCount ：value } }
        redisCaseMap.put(keyNameCount, count);
        redisCaseMap.put(keyNameList, findCaseBase);
        redisUtilNew.set(DeviceServiceImpl.redisCaseName, redisCaseMap);
        return BuilderMap.of(String.class, Object.class)
                .put("count", count)
                .put("cases", findCaseBase)
                .put("totalPage", Math.ceil(count / 50d))
                .get();
    }

    @Override
    public Map queryByUnique(String policeNumber, String deviceUnique) {
        // TODO Auto-generated method stub
        if (policeNumber == null) {
            throw new RuntimeException("对应的警号为空");
        }
        //split 以逗号分隔其中的值
        String[] user = policeNumber.split(",");
        String[] devi = deviceUnique.split(",");
        Map<String,List<Document>> personAndDeviceMap = new HashMap<>(2);
        ArrayList<Document> list1 = new ArrayList<>();
        ArrayList<Document> list2 = new ArrayList<>();
        for (String anUser : user) {
            BasicDBObject query = new BasicDBObject();
            query.append("usernumber", anUser);
            List<Document> associatedPerson = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_person", query);
            if (!CollectionUtils.isEmpty(associatedPerson)) {
                list1.add(associatedPerson.get(0));
            }
        }
        for (String aDevi : devi) {
            BasicDBObject query = new BasicDBObject();
            query.append("device_unique", aDevi);
            List<Document> associatedDevice = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_device", query);
            if (!CollectionUtils.isEmpty(associatedDevice)) {
                list2.add(associatedDevice.get(0));
            }
        }
        personAndDeviceMap.put("person", list1);
        personAndDeviceMap.put("device", list2);
        return personAndDeviceMap;
    }

}
