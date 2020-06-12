package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.UserBean;
import com.hnf.honeycomb.config.FtpConfig;
import com.hnf.honeycomb.config.MongoBcpClientClusterConfig;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.remote.user.BusinessDepartmentMapper;
import com.hnf.honeycomb.remote.user.BusinessUserMapper;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.service.DevicePersonService;
import com.hnf.honeycomb.util.BuilderMap;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.RedisUtilNew;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 人员信息业务层接口实现
 *
 * @author zhouhong
 */
@Service("personService")
public class DevicePersonServiceImpl implements DevicePersonService {

    @Resource
    private RedisUtilNew redisUtilNew;

    @Resource
    private BusinessUserMapper businessUserMapper;

    @Resource
    private BusinessDepartmentMapper businessDepartmentMapper;

    @Resource
    private DeviceMongoDao deviceMongoDao;

    @Resource
    private FtpConfig ftpConfig;

    @Resource
    private DepartmentMapper departmentMapper;

    @Override
    public Map<String, Object> personList(
            Integer pageNumber, String personName, String userNumber, List caseTypeId, String departmentCode) {
        List<Document> findPersonBase = new ArrayList<>();
        //每次查询的所有条件组为key
        String keyNameCount = pageNumber + departmentCode + personName + userNumber + caseTypeId + "count";
        String keyNameList = pageNumber + departmentCode + personName + userNumber + caseTypeId + "List";
        Map<String, Object> redisPersonMap = (Map<String, Object>) redisUtilNew.get(DeviceServiceImpl.redisPersonName);
        //如果是第一次 避免空指针需要给它new一个
        if (redisPersonMap == null) {
            redisPersonMap = new HashMap<>(100);
        }
        Long count;
        if (redisPersonMap.size() > 0) {
            count = (Long) redisPersonMap.get(keyNameCount);
            findPersonBase = (List<Document>) redisPersonMap.get(keyNameList);
            if (count != null && findPersonBase != null) {
                return BuilderMap.of(String.class, Object.class)
                        .put("count", count)
                        .put("persons", findPersonBase)
                        .put("totalPage", Math.ceil(count / 50d))
                        .get();
            }
        }
        // TODO Auto-generated method stub
        if (pageNumber == null || pageNumber == 0) {
            throw new RuntimeException("传入的分页条件有误");
        }
        BasicDBObject queryObj = new BasicDBObject();
        BasicDBObject findQuery2 = new BasicDBObject();
//        if (caseTypeId != null) {
//            findQuery2.put("tags.tagCaseType", new BasicDBObject("$in",caseTypeId));
//        }
        if (!StringUtils.isEmpty(departmentCode)) {
            //查询范围控制
            departmentCode = StringUtils.getOldDepartmentCode(departmentCode);
            Pattern pattern = Pattern.compile("^" + departmentCode + ".*$", Pattern.CASE_INSENSITIVE);
            queryObj.put("departmentCode", pattern);
        }
        if (!StringUtils.isEmpty(personName)) {
            //模糊查询
            Pattern pattern = Pattern.compile("^.*" + DeviceServiceImpl.dispose(personName) + ".*$", Pattern.CASE_INSENSITIVE);
//            Pattern pattern = TryCompileOrQuote(DeviceServiceImpl.dispose(personName));
            queryObj.put("personName", pattern);
        }
        if (!StringUtils.isEmpty(userNumber)) {
            //模糊查询
            Pattern pattern = Pattern.compile("^.*" + DeviceServiceImpl.dispose(userNumber) + ".*$", Pattern.CASE_INSENSITIVE);
//            Pattern pattern = TryCompileOrQuote(DeviceServiceImpl.dispose(userNumber));
            queryObj.put("personNumber", pattern);
        }
        BasicDBObject group = new BasicDBObject("$group", new Document("_id", "$personNumber"));
        BasicDBObject start = new BasicDBObject("$skip", (pageNumber - 1) * 50);
        BasicDBObject size = new BasicDBObject("$limit", 50);
        List<Document> personData = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                "infoData2", "personAbstract",
                Arrays.asList(new BasicDBObject("$match", queryObj), group, start, size));
        if (personData != null && personData.size() > 0) {
            ArrayList<String> userNumbers = new ArrayList<>();
            for (Document pd : personData) {
                String id = pd.getString("_id");
                if (id != null && !"".equals(id)) {
                    userNumbers.add(id);
                }
            }
            findQuery2.put("usernumber", new BasicDBObject("$in", userNumbers));
            List<Document> infoByGatherNameAndQuery = deviceMongoDao.findInfoByGatherNameAndQueryAll(
                    "infoData2", "t_person", findQuery2);
            if (findPersonBase == null) {
                findPersonBase = new ArrayList<>();
            }
            findPersonBase.addAll(infoByGatherNameAndQuery);
            for (Document doc : findPersonBase) {
                BasicDBObject sisQuery = new BasicDBObject();
                sisQuery.append("personNumber", doc.getString("usernumber"));
                //0代表不是sis,1代表是sis
                doc.append("isSis", 0);
                List<Document> info = deviceMongoDao.findInfoByDBNameAndGatherNameAndQueryBcp("sis", "bcp", sisQuery);
                //查询用户id下所有的账单类型
                Set<Integer> fileTypeSet = new HashSet<>();
                BasicDBObject typeQuery = new BasicDBObject("personId", doc.getString("usernumber"));
                BasicDBObject groupQuery = new BasicDBObject("$group", new BasicDBObject("_id", "$fileType"));
                List<Document> fileTypeListDoc = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery("accountdata", "personId_file",
                        Arrays.asList(new BasicDBObject("$match",typeQuery), groupQuery));
                if (!CollectionUtils.isEmpty(info)) {
                    doc.append("isSis", 1);
                }

                if(!CollectionUtils.isEmpty(fileTypeListDoc)){
                    fileTypeListDoc.forEach(r->{
                        fileTypeSet.add(r.getInteger("_id"));
                    });
                    doc.append("fileType",fileTypeSet);
                }
            }
        }
        BasicDBObject group2 = new BasicDBObject("$group", new BasicDBObject("_id",
                new BasicDBObject("personNumber", "$personNumber"))
                .append("count", new BasicDBObject("$sum", 1))
        );
        count = deviceMongoDao.countByQueryAndDBAndCollName("infoData2", "personAbstract",
                Arrays.asList(new BasicDBObject("$match", queryObj), group2));
        //新查出的数据添加到redis中  fetchData ： { redisPersonMap ：{keyNameCount ：value } }
        redisPersonMap.put(keyNameCount, count);
        redisPersonMap.put(keyNameList, findPersonBase);
        redisUtilNew.set(DeviceServiceImpl.redisPersonName, redisPersonMap);
        return BuilderMap.of(String.class, Object.class)
                .put("count", count)
                .put("persons", findPersonBase)
                .put("totalPage", Math.ceil(count / 50d))
                .get();
    }

    private Pattern TryCompileOrQuote(CharSequence cs) {
        try {
            return Pattern.compile("^.*" + cs + ".*$", Pattern.CASE_INSENSITIVE);
        } catch (java.util.regex.PatternSyntaxException pse){
            return Pattern.compile("^.*" + Pattern.quote(cs.toString()) + ".*$", Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public Map queryByUnique(String caseUnique, String deviceUnique) {
        // TODO Auto-generated method stub
        String[] cas = caseUnique.split(",");
        String[] devi = deviceUnique.split(",");
        Map<String, Object> caseAndDeviceMap = new HashMap<>(2);
        ArrayList<Document> list1 = new ArrayList<>();
        ArrayList<Document> list2 = new ArrayList<>();
        //		ArrayList list3 = new ArrayList<>();

        for (String ca : cas) {
            BasicDBObject query = new BasicDBObject();
            query.append("caseuniquemark", ca);
            List<Document> associatedcase = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_case", query);
            if (!CollectionUtils.isEmpty(associatedcase)) {
                list1.add(associatedcase.get(0));
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
        caseAndDeviceMap.put("case", list1);
        caseAndDeviceMap.put("device", list2);
        return caseAndDeviceMap;
    }

    @Override
    public List<Document> personQueryByDeviceUnique(String deviceUnique) {
        if (StringUtils.isEmpty(deviceUnique.trim())) {
            throw new RuntimeException("传入对应的设备unique为空");
        }
        BasicDBObject query = new BasicDBObject();
        query.append("device_unique", deviceUnique);
        List<Document> personInfo = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("infoData2", "t_person", query);
        List<Document> promptList = new ArrayList<>();
        if (personInfo.isEmpty()) {
            Document promptDoc = new Document();
            promptDoc.put("Prompt", " ");
            promptDoc.put("elsecallpeople", " ");
            promptDoc.put("elsecallpeoplenum", " ");
            promptDoc.put("hometown", " ");
            promptDoc.put("personname", " ");
            promptDoc.put("phone", " ");
            promptDoc.put("reside", " ");
            promptDoc.put("sex", " ");
            promptDoc.put("usernumber", " ");
            promptDoc.put("usertype", " ");
            promptList.add(promptDoc);
            return promptList;
        }
        return personInfo;
    }

    @Override
    public Map findSisPerson(String policeNumber, String searchPolicePersonNumber, String personName, String personNumber
            , String isGenerateBCP, String isUploadSuccess, String personSerialNum, String personType
            , Integer page, Integer pageSize, String departmentCode, Integer departmentType) {
        // TODO Auto-generated method stub
        if (policeNumber == null || policeNumber.trim().isEmpty()) {
            throw new RuntimeException("搜索标采人员的警号为空");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("页码有误");
        }
        //封装返回信息
        Map<String, Object> map = new HashMap<>(2);
        BasicDBObject query = new BasicDBObject();
        if (!StringUtils.isEmpty(departmentCode)) {
            departmentCode = StringUtils.getOldDepartmentCode(departmentCode);
            Pattern pattern1 = Pattern.compile("^" + departmentCode + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("JYDWDM", pattern1);
        }
        if (searchPolicePersonNumber != null && !searchPolicePersonNumber.trim().isEmpty()) {
            Pattern pattern = TryCompileOrQuote(searchPolicePersonNumber.trim());
            query.append("JYSFZH", pattern);
        }
        if (personName != null && !personName.trim().isEmpty()) {
            Pattern pattern = TryCompileOrQuote(personName.trim());
            query.append("BCJRXM", pattern);
        }
        if (personNumber != null && !personNumber.trim().isEmpty()) {
            Pattern pattern = Pattern.compile("^" + personNumber.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("BCJRZJHM", pattern);
        }
        if (isGenerateBCP != null && !isGenerateBCP.trim().isEmpty()) {
            //代表已经上传过bcp
            if ("0".equals(isGenerateBCP.trim())) {
                query.append("$where", "this.STATE.length>0");
            } else {
                query.append("$where", "this.STATE.length==0");
            }
        }
        if (!StringUtils.isEmpty(isUploadSuccess)) {
            if ("0".equals(isUploadSuccess)) {
                BasicDBList obj = new BasicDBList();
                //1,2,401 为上传成功
                obj.add(new BasicDBObject().append("STATE.SCJG", "1"));
                obj.add(new BasicDBObject().append("STATE.SCJG", "2"));
                obj.add(new BasicDBObject().append("STATE.SCJG", "401"));
                query.append("$or", obj);
            } else {
                BasicDBList obj = new BasicDBList();
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                query.append("$and", obj);
            }
        }
        if (!StringUtils.isEmpty(personSerialNum)) {
            Pattern pattern = TryCompileOrQuote(personSerialNum.trim());
            query.append("RYBH", pattern);
        }
        if (!StringUtils.isEmpty(personType)) {
            query.append("CJLX", personType.trim());
        }
        List<Document> result = deviceMongoDao.findSisInfoByGatherNameAndQuery("bcp", "bcp", query, page, pageSize);
        if (!CollectionUtils.isEmpty(result)) {
            for (Document doc : result) {
                Object state = doc.get("STATE");
                if (state == null) {
                    doc.append("isGenerateBCP", "1");
                    doc.append("isUploadSuccess", "1");
                    continue;
                }
                ArrayList<?> stateList = (ArrayList) state;
                if (CollectionUtils.isEmpty(stateList)) {
                    doc.append("isGenerateBCP", "1");
                    doc.append("isUploadSuccess", "1");
                    continue;
                }
                doc.append("isGenerateBCP", "0");
                doc.append("isUploadSuccess", "1");
                for (Object oneState : stateList) {
                    Document oneStateDoc = (Document) oneState;
                    Object scjg = oneStateDoc.get("SCJG");
                    String fileName = oneStateDoc.getString("FILENAME");
                    String ftpDownLoadPath = ftpConfig.getFTPDownloadPath(fileName);
                    oneStateDoc.append("ftpDownLoadPath", ftpDownLoadPath);
                    if ("1".equals(scjg) || "2".equals(scjg) || "401".equals(scjg)) {
                        doc.append("isUploadSuccess", "0");
                    }
                }
            }
        }
        map.put("result", result);
        //加入标采人员总数
        Long count = deviceMongoDao.countSisByDBNameAndGatherNameAndQuery("bcp", "bcp", query);
        map.put("count", count);
        return map;
    }

    private String getFileBasePath(Object scjg, String fileName) {
        if ("0".equals(scjg)) {
            return File.separator + "hpkbk" + File.separator + fileName;
        } else {
            return File.separator + "hpkbk" + File.separator + scjg.toString() + File.separator + fileName;
        }
    }


    /**
     * 根据不同的登录警号查询出对应的单位代码,包含对应的单位控制
     *
     * @param pNumber 对应的登录警号
     * @return 查询单位代码的集合
     */
    private String getUnitNumByPNumber(String pNumber) {
        //通过对应的登录警号查询出对应的人员
        UserBean bean = businessUserMapper.findByPoliceNumber(pNumber.trim());
        //获取对应的单位类型
        Long unitType = bean.getUnitType();
        if (Long.valueOf(1).equals(unitType)) {
            //若是市区领导,返回空串代表全部
            return "";
        }
        //获取对应的用户权限
        Integer roleId = bean.getRoleId();
        //查询对应的单位代码
        String unitNum = (unitType.toString());
        if (Integer.valueOf(1).equals(roleId) ||
                //当时超级管理员或者单位管理员或高级用户时
                Integer.valueOf(2).equals(roleId) || Integer.valueOf(3).equals(roleId)) {
            return !unitNum.trim().isEmpty() && unitNum.trim().length() > 6
                    ? unitNum.substring(0, 6) : null;
        }
        //普通用户和部门管理员,查询自己部门的人员
        return unitNum;
    }

//	@Override
//	public Long countSisPerson(String pNumber, String searchPNumber, String personName, String personNumber,
//			String isGenerateBCP, String isUploadSuccess, String personSerialNum, String personType) {
//		// TODO Auto-generated method stub
//		if(pNumber == null || pNumber.trim().isEmpty()){
//			throw new RuntimeException("搜索标采人员的警号为空");
//		}
//		//		String unitNum = dao.findUnitNumberByPNumber(pNumber.trim());
//		String unitNum = getUnitNumByPNumber(pNumber);//通过对应的用查询对应的单位代码
//		System.out.println("unitNum:"+unitNum);
//		//		unitNum = "510724420000";
//		Map<String,Object> para = new HashMap<>();
//		if(unitNum == null){
//			throw new RuntimeException("对应的单位代码未知");
//		}
//		if(searchPNumber != null && !searchPNumber.trim().isEmpty()){
//			para.put("searchPNumber", searchPNumber.trim());
//		}
//		if(personName != null && !personName.trim().isEmpty()){
//			para.put("personName", personName.trim());
//		}
//		if(personNumber != null && !personNumber.trim().isEmpty()){
//			para.put("personNumber", personNumber.trim());
//		}
//		if(isGenerateBCP != null && !isGenerateBCP.trim().isEmpty()){
//			para.put("isGenerateBCP", isGenerateBCP);
//		}
//		if(!StringUtils.isEmpty(isUploadSuccess)){
//			para.put("isUploadSuccess", isUploadSuccess);
//		}
//		if(!StringUtils.isEmpty(personSerialNum)){
//			para.put("personSerialNum", personSerialNum.trim());
//		}
//		if(!StringUtils.isEmpty(personType)){
//			para.put("personType", personType.trim());
//		}
//		return Long.parseLong(restTemplate.postForObject("http://common/person/getCountSisPerson/{unitNumber}/{para}",unitNum,String.class,unitNum,para));
//	}

    @Override
    public Map<String, List<DBObject>> analysisSisPerson(String policeNum, String departNum, Integer departmentType,
                                                         Long startTime, Long endTime) {
        // TODO Auto-generated method stub
        if (StringUtils.isEmpty(policeNum)) {
            throw new RuntimeException("对应的登录警号为空");
        }
        //获取对应的警员信息以及对应的单位代码以及对应的部门代码
        //对应的查询条件
        Map<String, Object> para = new HashMap<>(3);
        //对应的上传并生成标采查询条件
        BasicDBObject query = new BasicDBObject();
        //对应的成功上传省标采查询条件
        BasicDBObject query2 = new BasicDBObject();
        //对应的标采人员查询条件
        BasicDBObject query3 = new BasicDBObject();
        //对应的时间查询条件
        BasicDBObject timeQuery = new BasicDBObject();
        Map<String, List<DBObject>> result = new HashMap<>(2);
        if (startTime != null) {
            para.put("startTime", startTime);
            timeQuery.append("$gte", new Date(startTime));
        }
        if (endTime != null) {
            endTime += 10000*60*60*24;
            para.put("endTime", endTime);
            timeQuery.append("$lt", new Date(endTime));
        }
        if (!timeQuery.isEmpty()) {
            query.append("UPDATETIME", timeQuery);
            query2.append("UPDATETIME", timeQuery);
            query3.append("UPDATETIME", timeQuery);
        }
        String searchDepartCode = "";
        if (!StringUtils.isEmpty(departNum)) {
            searchDepartCode = departNum.trim();
        }
        para.put("departNum", searchDepartCode.trim());
        Pattern pattern;
        searchDepartCode = StringUtils.getOldDepartmentCode(searchDepartCode);
        pattern = Pattern.compile("^" + searchDepartCode.trim() + ".*$", Pattern.CASE_INSENSITIVE);
        query.append("JYDWDM", pattern);
        query2.append("JYDWDM", pattern);
        query3.append("JYDWDM", pattern);
        query.append("$where", "this.STATE.length>0");
        //此时调用对应的接口查询
        List<DBObject> isGenerateBCP = deviceMongoDao.groupSisPersonCount("bcp", "bcp", query, searchDepartCode);
        BasicDBList obj = getUploadSuccessFiled();
        query2.append("$or", obj);
        List<DBObject> isUploadSuccessBCP = deviceMongoDao.groupSisPersonCount("bcp", "bcp", query2, searchDepartCode);
        List<DBObject> noSort = addGenerateAndUploadMsg(getNameBySisResult(para, deviceMongoDao.groupSisPersonCount("bcp", "bcp", query3, searchDepartCode)),
                isGenerateBCP, isUploadSuccessBCP);
        List<DBObject> sortCount = compareValue(noSort);
        List<DBObject> sortUpload = sortUpload(noSort);
        result.put("sortCount", sortCount);
        result.put("sortUpload", sortUpload);
        return result;
    }

    /**
     * 通过上传成功进行排序
     *
     * @param results 參數
     */
    private List<DBObject> sortUpload(List<DBObject> results) {
        results.sort((t1, t2) -> {
            Object one = t1.get("iploadSuccessCount");
            Object two = t2.get("iploadSuccessCount");
            Double one1 = one != null ? Double.valueOf(one.toString()) : 0.0;
            Double two1 = two != null ? Double.valueOf(two.toString()) : 0.0;
            return -one1.compareTo(two1);
        });
        return results;
    }

    /**
     * 通过上传人员数进行排序
     *
     * @param results 参数
     */
    private List<DBObject> compareValue(List<DBObject> results) {
        results.sort((t1, t2) -> {
            Object one = t1.get("value");
            Object two = t2.get("value");
            Double one1 = one != null ? Double.valueOf(one.toString()) : 0.0;
            Double two1 = two != null ? Double.valueOf(two.toString()) : 0.0;
            return -one1.compareTo(two1);
        });
        return results;
    }

    private List<DBObject> addGenerateAndUploadMsg(
            List<DBObject> nameBySisResult, List<DBObject> isGenerateBCP, List<DBObject> isUploadSuccessBCP) {
        if (CollectionUtils.isEmpty(nameBySisResult)) {
            return nameBySisResult;
        }
        Map<String, Object> getNumToGenerateCount = getNumToCount(isGenerateBCP);
        Map<String, Object> getNumToUploadCount = getNumToCount(isUploadSuccessBCP);
        for (DBObject obj : nameBySisResult) {
            Object id = obj.get("_id");
            if (id == null) {
                Object generateCount = getNumToGenerateCount.get(null);
                Object uploadCount = getNumToUploadCount.get(null);
                obj.put("generateCount", generateCount != null ? generateCount : 0);
                obj.put("iploadSuccessCount", uploadCount != null ? uploadCount : 0);
            } else {
                Object generateCount = getNumToGenerateCount.get(id.toString());
                Object uploadCount = getNumToUploadCount.get(id.toString());
                obj.put("generateCount", generateCount != null ? generateCount : 0);
                obj.put("iploadSuccessCount", uploadCount != null ? uploadCount : 0);
                String detail;
                DepartmentBean byDepartmentCode = departmentMapper.findByDepartmentCode(StringUtils.getNewDepartmentCode(id.toString()));
                if (byDepartmentCode == null) {
                    detail = id.toString();
                } else {
                    detail = byDepartmentCode.getDepartmentName();
                }
                obj.put("detail", detail);
            }
        }
        return nameBySisResult;
    }

    /**
     * 通过对应的类型与总数的关系
     *
     * @param isGenerateBCP 对应的类型
     */
    private Map<String, Object> getNumToCount(List<DBObject> isGenerateBCP) {
        Map<String, Object> result = new HashMap<>(isGenerateBCP.size());
        if (CollectionUtils.isEmpty(isGenerateBCP)) {
            return result;
        }
        isGenerateBCP.forEach(t -> {
            Object id = t.get("_id");
            Object value = t.get("value");
            if (id == null) {
                result.put(null, value);
            } else {
                result.put(id.toString(), value);
            }
        });
        return result;
    }

    /**
     * 获取对应单位名称或者部门名称或对应人员的名称
     *
     * @param para             对应单位部门的条件,用于获取应查询什么表
     * @param analysisSisCount 对应的sis统计结果
     * @return 返回增加对应名称的结果
     */
    private List<DBObject> getNameBySisResult(Map<String, Object> para, List<DBObject> analysisSisCount) {
        Integer i = getSearchType(para);
        switch (i) {
            //查询对应的部门名称
            case 1:
                for (DBObject obj : analysisSisCount) {
                    Object departNum = obj.get("_id");
                    String departmentName = businessDepartmentMapper.findDepartNameByDepartNum(departNum.toString());
                    obj.put("detail", departmentName);
                }
                break;
            //查询对应的人员名称
            case 2:
                for (DBObject obj : analysisSisCount) {
                    Object policeNum = obj.get("_id");
                    String userName = "";
                    if (policeNum != null) {
                        UserBean userInfo = businessUserMapper.findByPoliceNumber(policeNum.toString());
                        if (userInfo != null) {
                            userName = userInfo.getNickname();
                        }
                    }
                    obj.put("detail", userName);
                }
                break;
            default:
                break;
        }
        return analysisSisCount;
    }

    /**
     * 获取应该查询什么表的集合
     *
     * @param para 对应的条件
     * @return 0代表单位, 1代表部门, 2代表人员
     */
    private Integer getSearchType(Map<String, Object> para) {
        Object departNum = para.get("departNum");
        if (departNum.toString().length() != 12) {
            return 1;
        }
        return 2;
    }

    @Override
    public List<DBObject> analysisSisPersonPreDate(String policeNum
            , String departNum, Integer departmentType
            , Long startTime, Long endTime, String type) {
        // TODO Auto-generated method stub
        if (StringUtils.isEmpty(policeNum)) {
            throw new RuntimeException("对应的登录警号为空");
        }
        //获取对应的警员信息以及对应的单位代码以及对应的部门代码
        //对应的查询条件
        //对应的上传并生成标采查询条件
        BasicDBObject query = new BasicDBObject();
        //对应的成功上传省标采查询条件
        BasicDBObject query2 = new BasicDBObject();
        //对应的标采人员查询条件
        BasicDBObject query3 = new BasicDBObject();
        //对应的时间查询条件
        BasicDBObject timeQuery = new BasicDBObject();
        if (startTime != null) {
            timeQuery.append("$gte", new Date(startTime));
        }
        if (endTime != null) {
            timeQuery.append("$lte", new Date(endTime));
        }
        if (!timeQuery.isEmpty()) {
            query.append("UPDATETIME", timeQuery);
            query2.append("UPDATETIME", timeQuery);
            query3.append("UPDATETIME", timeQuery);
        }
        departNum = StringUtils.getOldDepartmentCode(departNum);
        Pattern pattern = Pattern.compile("^" + departNum + ".*$", Pattern.CASE_INSENSITIVE);
        query.append("JYDWDM", pattern);
        query.append("$where", "this.STATE.length>0");
        BasicDBList obj = getUploadSuccessFiled();
        query2.append("$or", obj);
        query2.append("JYDWDM", pattern);
        query3.append("JYDWDM", pattern);
        //此时调用对应的接口查询
        List<DBObject> result = new ArrayList<>();
        switch (type) {
            //按年统计
            case "1":
                List<DBObject> isGenerateBCP = deviceMongoDao.groupSisPersonPreYear("bcp", "bcp", query);
                List<DBObject> isUploadSuccessBCP = deviceMongoDao.groupSisPersonPreYear("bcp", "bcp", query2);
                result = compareTime(addGenerateAndUploadMsg(deviceMongoDao.groupSisPersonPreYear("bcp", "bcp", query3),
                        isGenerateBCP, isUploadSuccessBCP));
                break;
            //按月统计
            case "2":
                List<DBObject> isGenerateBCP1 = deviceMongoDao.groupSisPersonPreMonth("bcp", "bcp", query);
                List<DBObject> isUploadSuccessBCP1 = deviceMongoDao.groupSisPersonPreMonth("bcp", "bcp", query2);
                result = compareTime(addGenerateAndUploadMsg(deviceMongoDao.groupSisPersonPreMonth("bcp", "bcp", query3),
                        isGenerateBCP1, isUploadSuccessBCP1));
                break;
            //按日统计
            case "3":
                List<DBObject> isGenerateBCP11 = deviceMongoDao.groupSisPersonPreDay("bcp", "bcp", query);
                List<DBObject> isUploadSuccessBCP11 = deviceMongoDao.groupSisPersonPreDay("bcp", "bcp", query2);
                result = compareTime(addGenerateAndUploadMsg(deviceMongoDao.groupSisPersonPreDay("bcp", "bcp", query3),
                        isGenerateBCP11, isUploadSuccessBCP11));
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 用于对对应的结果按照时间排序
     *
     * @param addGenerateAndUploadMsg 对应需排序的数据
     * @return 返回排序后的结果
     */
    private List<DBObject> compareTime(List<DBObject> addGenerateAndUploadMsg) {
        if (CollectionUtils.isEmpty(addGenerateAndUploadMsg)) {
            return addGenerateAndUploadMsg;
        }
        addGenerateAndUploadMsg.sort((t1, t2) -> {
            Object one = t1.get("_id");
            Object two = t2.get("_id");
            Date one1 = one != null ? (Date) one : new Date(0L);
            Date two1 = two != null ? (Date) two : new Date(0L);
            return one1.compareTo(two1);
        });
        return addGenerateAndUploadMsg;
    }

    @Override
    public List<Document> findUploadSisFalseDetail(String departNum, String personIdNum,
                                                   Integer scjgType) {
        // TODO Auto-generated method stub
        if (scjgType == null) {
            scjgType = -1;
        }
        BasicDBObject query = new BasicDBObject();
        BasicDBList obj = new BasicDBList();
        //上传过bcp的条件
        query.append("$where", "this.STATE.length>0");
        switch (scjgType) {
            //所有
            case -1:
                break;
            //上传成功
            case 1:
                obj.add(new BasicDBObject().append("STATE.SCJG", "1"));
                obj.add(new BasicDBObject().append("STATE.SCJG", "2"));
                obj.add(new BasicDBObject().append("STATE.SCJG", "401"));
                query.append("$or", obj);
                break;
            //上传失败
            case 0:
//                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
//                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
//                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
//                query.append("$and", obj);
                query.append("STATE.SCJG", new BasicDBObject("$in", Arrays.asList("0","503","502","501","-2")));
                break;
            //服务的空返回
            case 2:
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                obj.add(new BasicDBObject("STATE.SCJG", "0"));
                query.append("$and", obj);
                break;
            //表示无此编号人员
            case 3:
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                obj.add(new BasicDBObject("STATE.SCJG", "501"));
                query.append("$and", obj);
                break;
            //表示解析包错误
            case 4:
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                obj.add(new BasicDBObject("STATE.SCJG", "502"));
                query.append("$and", obj);
                break;
            //表示未知错误
            case 5:
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "501")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "502")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "-2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "0")));
                query.append("$and", obj);
                break;
            //表示上传失败
            case 6:
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "1")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "2")));
                obj.add(new BasicDBObject("STATE.SCJG", new BasicDBObject(QueryOperators.NE, "401")));
                obj.add(new BasicDBObject("STATE.SCJG", "-2"));
                query.append("$and", obj);
                break;
            default:
                break;
        }
        if (!StringUtils.isEmpty(personIdNum)) {
            query.append("JYSFZH", personIdNum.trim());
        }
        if (!StringUtils.isEmpty(departNum)) {
            departNum = StringUtils.getOldDepartmentCode(departNum);
            Pattern pattern = Pattern.compile("^" + departNum + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("JYDWDM", pattern);
        }
        return sortTime(removeNoMatchState(changeStateFromManyToOne(deviceMongoDao.findSisInfoByGatherNameAndQuery(
                "bcp", "bcp", query, null, null)), scjgType));
    }


    /**
     * 将对应上传bcp详情的数据中的State多个数组修改为一个
     *
     * @param findUploadSisFalseDetail 对应上传bcp详情
     */
    private List<Document> changeStateFromManyToOne(List<Document> findUploadSisFalseDetail) {
        if (CollectionUtils.isEmpty(findUploadSisFalseDetail)) {
            return findUploadSisFalseDetail;
        }
        List<Document> result = new ArrayList<>();
        for (Document doc : findUploadSisFalseDetail) {
            Object state = doc.get("STATE");
            if (state == null) {
                result.add(doc);
                continue;
            }
            if (state instanceof ArrayList) {
                ArrayList<?> states = (ArrayList) state;
                if (!CollectionUtils.isEmpty(states)) {
                    for (Object newState : states) {
                        Document d = new Document(doc);
                        d.append("STATE", new ArrayList<>(Arrays.asList(newState)));
                        result.add(d);
                    }
                }
            }

        }
        return result;
    }


    /**
     * 去除不符合筛选类型的上传结果
     *
     * @param changeStateFromManyToOne 对应的list
     * @param sCJGType                 对应的上传结果
     */
    private List<Document> removeNoMatchState(List<Document> changeStateFromManyToOne, Integer sCJGType) {
        if (CollectionUtils.isEmpty(changeStateFromManyToOne)) {
            return changeStateFromManyToOne;
        }
        List<Document> result = new ArrayList<>();
        for (Document doc : changeStateFromManyToOne) {
            if (doc == null) {
                continue;
            }
            Object state = doc.get("STATE");
            if (state instanceof ArrayList) {
                ArrayList<?> states = (ArrayList) state;
                if (CollectionUtils.isEmpty(states)) {
                    continue;
                }
                Document oneDoc = (Document) states.get(0);
                Object scjg = oneDoc.get("SCJG");
                Boolean isMatchFiled = getIsMatch(scjg, sCJGType);
                if (isMatchFiled) {
                    //当上传结果为失败时，增加对应包的下载详情
                    if (!("1".equals(scjg) || "2".equals(scjg) || "401".equals(scjg))) {
                        String fileName = oneDoc.getString("FILENAME");
                        String ftpDownLoadPath = ftpConfig.getFTPDownloadPath(fileName);
                        oneDoc.append("ftpDownLoadPath", ftpDownLoadPath);
                    }
                    result.add(doc);
                }
            }
        }
        return result;
    }


    /**
     * 按照bcp包的详情时间排序
     *
     * @param removeNoMatchState bcp包的详情
     */
    private List<Document> sortTime(List<Document> removeNoMatchState) {
        if (CollectionUtils.isEmpty(removeNoMatchState)) {
            return removeNoMatchState;
        }
        removeNoMatchState.sort((t1, t2) -> {
            Object state1 = t1.get("STATE");
            Object state2 = t2.get("STATE");
            if ((state1 instanceof ArrayList) && state2 instanceof ArrayList) {
                ArrayList<?> one = (ArrayList) state1;
                ArrayList<?> two = (ArrayList) state2;
                if (!CollectionUtils.isEmpty(one) && !CollectionUtils.isEmpty(two)) {
                    Document doc1 = (Document) one.get(0);
                    Document doc2 = (Document) two.get(0);
                    Date date1 = doc1.get("TIME") != null ? (Date) doc1.get("TIME") : new Date();
                    Date date2 = doc2.get("TIME") != null ? (Date) doc2.get("TIME") : new Date();
                    return -date1.compareTo(date2);
                }
            }
            return 1;
        });
        return removeNoMatchState;
    }

    /**
     * 通过对应的结果查询其是否符合上传结果
     */
    private Boolean getIsMatch(Object scjg, Integer sCJGType) {
        boolean result;
        switch (sCJGType) {
            //所有
            case -1:
                result = true;
                break;
            //上传成功
            case 1:
                result = "1".equals(scjg) || "2".equals(scjg) || "401".equals(scjg);
                break;
            //上传失败
            case 0:
                result = !"1".equals(scjg) && !"2".equals(scjg) && !"401".equals(scjg);
                break;
            //服务的空返回
            case 2:
                result = "0".equals(scjg);
                break;
            //表示无此编号人员
            case 3:
                result = "501".equals(scjg);
                break;
            //表示解析包错误
            case 4:
                result = "502".equals(scjg);
                break;
            //表示未知错误
            case 5:
                result = !"1".equals(scjg) && !"2".equals(scjg) && !"401".equals(scjg)
                        && !"0".equals(scjg) && !"501".equals(scjg) && !"502".equals(scjg) && !"-2".equals(scjg);
                break;
            //表示上传失败
            case 6:
                result = "-2".equals(scjg);
                break;
            default:
                result = true;
                break;
        }
        return result;
    }

    private BasicDBList getUploadSuccessFiled() {
        BasicDBList obj = new BasicDBList();
        obj.add(new BasicDBObject().append("STATE.SCJG", "1"));
        obj.add(new BasicDBObject().append("STATE.SCJG", "2"));
        obj.add(new BasicDBObject().append("STATE.SCJG", "401"));
        return obj;
    }
}
