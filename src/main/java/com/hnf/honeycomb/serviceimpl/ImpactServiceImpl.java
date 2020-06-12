package com.hnf.honeycomb.serviceimpl;

import com.hnf.crypte.MD5Util;
import com.hnf.crypte.Utils;
import com.hnf.honeycomb.daoimpl.ImpactServerDao;
import com.hnf.honeycomb.service.ImpactService;
import com.hnf.honeycomb.service.InsertLogService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.TimeUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 关系碰撞的实现类
 *
 * @author yy
 */
@Service("impactService")
public class ImpactServiceImpl implements ImpactService {


    @Resource
    private InsertLogService insertLogService;
    // 保存关系碰撞
    /**
     * 插入日志 统一请求到日志记录服务器 2018.6-26
     */
    private final ImpactServerDao impactServerDao;

    @Autowired
    public ImpactServiceImpl(ImpactServerDao impactServerDao) {
        this.impactServerDao = impactServerDao;
    }

    @Override
    public Integer insertImpactHistory(Integer userId, String type, String searchNum, String time, String project,
                                       String explain) {
        System.out.println("进入新增");
        if (userId == null) {
            throw new RuntimeException("对应的用户id为空");
        }
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("碰撞账号为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("碰撞类型为空");
        }
        Document query = new Document();
        query.append("userId", userId);
        query.append("searchNum", searchNum.trim());
        List<Document> history = impactServerDao.findOldHistory(query);
        if (!CollectionUtils.isEmpty(history)) {
            throw new RuntimeException("已存在");
        }
        Date date = new Date();
        if (!StringUtils.isEmpty(time)) {
            date = TimeUtils.parseStrToDate(time);
        }
        Document insertDoc = new Document();
        insertDoc.append("userId", userId);
        insertDoc.append("type", type.trim());
        insertDoc.append("searchNum", searchNum.trim());
        insertDoc.append("time", date);
        insertDoc.append("project", project);
        insertDoc.append("explain", explain);
        insertDoc.append("unique", MD5Util.MD5(userId + searchNum));
        impactServerDao.insertOldHistory(insertDoc);
        return 1;
        // 此处执行对应的操作
    }

    // 通过对应的信息查询碰撞列表
    @Override
    public List<Document> findImpactHistoryByUserId(Integer userId, String project, String searchNum, Integer page,
                                                    Integer pageSize) {
        if (userId == null) {
            throw new RuntimeException("查询关系碰撞的用户账号为空");
        }
        Document query = new Document("userId", userId);
        if (!StringUtils.isEmpty(project)) {
            Pattern pattern = Pattern.compile("^.*" + project.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("project", pattern);
        }
        System.out.println("searchNum:" + searchNum);
        if (!StringUtils.isEmpty(searchNum)) {
            Pattern pattern = Pattern.compile("^.*" + searchNum.trim() + ".*$", Pattern.CASE_INSENSITIVE);
            query.append("searchNum", pattern);
        }
        // System.out.println("query:"+query);
        List<Document> impactHistories = impactServerDao.findOldHistory(query);
        System.out.println("impactHistories:" + impactHistories);
        if (impactHistories != null) {
            impactHistories.forEach(t -> {
                List<Document> apps = new ArrayList<>();
                String type = t.getString("type");
                System.out.println("type:" + type);
                String searchNums = t.getString("searchNum");
                if (!StringUtils.isEmpty(searchNums)) {
                    String[] searchNumbers = StringUtils.split(searchNums, ",");
                    for (String str : searchNumbers) {
                        if (StringUtils.isEmpty(str)) {
                            continue;
                        }
                        Document one = new Document("num", str).append("name", "").append("isCollector", 2);
                        switch (type) {
                            case "qq":
                                List<Document> qqDetail = impactServerDao.findqq(str);
                                appendDetail2One(one, qqDetail);
                                break;
                            case "wx":
                                List<Document> wxDetail = impactServerDao.findwx(str);
                                appendDetail2One(one, wxDetail);
                                break;
                            case "phone":
                                BasicDBObject personQuery = new BasicDBObject("phoneNUM", str);
                                List<Document> phoneDetail = impactServerDao.findPersonExtendInfoByDoc(null,
                                        personQuery);
                                if (!CollectionUtils.isEmpty(phoneDetail)) {
                                    one.append("isCollector", 1);
                                    one.append("name", phoneDetail.get(0).getString("personName"));
                                }
                                break;
                            case "IDNumber":
                                BasicDBObject idNumberQuery = new BasicDBObject("personIdCard", str);
                                List<Document> personDetail = impactServerDao.findPersonExtendInfoByDoc(null,
                                        idNumberQuery);
                                System.out.println("personDetail:" + personDetail);
                                if (!CollectionUtils.isEmpty(personDetail)) {
                                    one.append("isCollector", 1);
                                    one.append("name", personDetail.get(0).getString("personName"));
                                }
                                break;
                            default:
                                break;
                        }
                        apps.add(one);
                    }
                }
                t.append("searchNumDetail", apps);
            });
        }
        return impactHistories;
    }

    private void appendDetail2One(Document one, List<Document> qqDetail) {
        if (!CollectionUtils.isEmpty(qqDetail)) {
            ArrayList<?> deviceUniques = ArrayList.class.cast(qqDetail.get(0).get("device_unique"));
            if (!CollectionUtils.isEmpty(deviceUniques)) {
                one.append("isCollector", 1);
                one.append("name", qqDetail.get(0).get("nickname"));
            }
        }
    }

    // 对碰撞信息进行对应的修改
    @Override
    public Integer updateImpactHistory(String unique, String explain, String project, String searchNum) {
        if (StringUtils.isEmpty(unique)) {
            throw new RuntimeException("对应的修改条件where条件为空");
        }
        Document newDoc = new Document();
        if (project != null) {
            newDoc.append("project", project.trim());
        }
        if (explain != null) {
            newDoc.append("explain", explain);
        }
        if (!StringUtils.isEmpty(searchNum)) {
            newDoc.append("searchNum", searchNum);
        }
        newDoc.append("time", new Date());
        Document query = new Document("unique", unique);
        Integer updateCount = impactServerDao.updateOldHistory(query, newDoc);
        return updateCount;
    }

    // 删除对应的碰撞历史
    @Override
    public Integer deleteImpactHistory(String unique) {
        if (StringUtils.isEmpty(unique)) {
            throw new RuntimeException("删除的uni为空");
        }
        Document query = new Document("unique", unique);
        Integer deleteCount = impactServerDao.deleteOldHistory(query);// 删除对应的碰撞信息
        return deleteCount;
    }

    // 精确搜索对应的记录
    @Override
    public List<Document> findImpactHistoryByUIdAndSearchNum(Integer userId, String searchNum) {
        if (userId == null) {
            throw new RuntimeException("精确搜索的用户ID为空");
        }
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("精确搜索的账号为空");
        }
        Document query = new Document();
        query.append("userId", userId);
        query.append("searchNum", searchNum.trim());
        List<Document> result = impactServerDao.findOldHistory(query);
        return result;
    }

    // --------------------------------------------------------------------------------
    // 对对应的账号进行对应的关系碰撞
    @Override
    public Map<String, Object> impactByNumbersAndSearchType(String searchType, Integer userId, String searchNumber,
                                                            String place) {
        Long a = System.currentTimeMillis();
        if (userId == null) {
            throw new RuntimeException("对应的用户ID为null");
        }
        if (StringUtils.isEmpty(searchType)) {
            throw new RuntimeException("对应的搜索账号类型为null");
        }
        if (StringUtils.isEmpty(searchNumber)) {
            throw new RuntimeException("对应的搜索账号为null");
        }
        /**
         * 改为统一日志服务
         */
        insertLogService.insertRelationLog(userId, place, searchNumber, searchType);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> numbers = new ArrayList<>();// 装对应的节点信息
        List<Map<String, Object>> relations = new ArrayList<>();// 装对应的关系
        String[] searchNumbers = com.hnf.honeycomb.util.Utils.bongDuplicateremove(searchNumber.split(","));
        BasicDBObject personQuery = new BasicDBObject();// 对应的人员查询条件
        HashSet<String> dupNumbers = new HashSet<>();// 用于存储对应的的节点信息,保证对应的节点不重复
        List<Document> persons = new ArrayList<>();// 每次搜索人员存储对应的信息
        HashSet<String> collPhones = new HashSet<>();// 用于存储对应的采集人电话号码
        HashSet<String> collQQs = new HashSet<>();// 用于存储对应的采集人qq账号
        HashSet<String> collWXs = new HashSet<>();// 用于存储对应的采集人QQ账号
        HashSet<String> c2cs = new HashSet<>();// 用于存储两个好友之间的共同信息
        Map<String, Map<String, String>> number2PersonBaseInfo = new HashMap<>();// 用于存储对应账号与人的基本关系
        Map<String, ArrayList<String>> personIDNum2SearchNum = new HashMap<>();
        switch (searchType) {
            case "phone":
                for (String phone : searchNumbers) {
                    // 通过电话查询对应的人员基本信息
                    personQuery.append("phoneNUM", phone);
                    persons = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                    getAllNumbersByPhoneOrQQ(numbers, dupNumbers, persons, collPhones, collQQs, collWXs, number2PersonBaseInfo, personIDNum2SearchNum, phone);
                }
                break;
            case "wx":
                for (String wx : searchNumbers) {
                    // 通过wx查询人员的基本信息
                    personQuery.append("WXNUM", wx);
                    persons = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                    if (!CollectionUtils.isEmpty(persons)) {// 包含采集账号
                        getAllNumbersByWxOrID(numbers, dupNumbers, persons, collPhones, collQQs, collWXs, number2PersonBaseInfo, personIDNum2SearchNum, wx);
                    } else {
                        Map<String, String> wxNode = new HashMap<>();
                        wxNode.put("num", wx);
                        wxNode.put("name", "");
                        wxNode.put("type", "wx");
                        numbers.add(wxNode);
                    }
                }
                break;
            case "qq":
                for (String qq : searchNumbers) {
                    // System.out.println("111111111111111111qq:"+qq);
                    // 通过QQ查询人员的基本信息
                    personQuery.append("QQNUM", qq);
                    persons = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                    getAllNumbersByPhoneOrQQ(numbers, dupNumbers, persons, collPhones, collQQs, collWXs, number2PersonBaseInfo, personIDNum2SearchNum, qq);
                }
                break;
            case "IDNumber":
                for (String personIDNumber : searchNumbers) {
                    // 通过身份证号查询对应的基本信息
                    personQuery.append("personIdCard", personIDNumber);
                    persons = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                    if (!CollectionUtils.isEmpty(persons)) {// 包含采集账号
                        getAllNumbersByWxOrID(numbers, dupNumbers, persons, collPhones, collQQs, collWXs, number2PersonBaseInfo, personIDNum2SearchNum, personIDNumber);
                    } else {
                        Map<String, String> personNode = new HashMap<>();
                        personNode.put("num", personIDNumber);
                        personNode.put("name", "");
                        personNode.put("type", "person");
                        numbers.add(personNode);
                    }
                }
                break;
            default:
                throw new RuntimeException("对应的单位类型不合规");
        }
        // 此处为对应的设备添加相关联的账号
        for (Map<String, String> number : numbers) {
            String personNum = number.get("num");
            ArrayList<String> searchs = personIDNum2SearchNum.get(personNum);
            if (!CollectionUtils.isEmpty(searchs)) {
                number.put("relation", searchs.toString());
            } else {
                number.put("relation", personNum);
            }
        }
        // System.out.println("dupNumbers1："+dupNumbers);
        // System.out.println("numbers1："+numbers);
        // System.out.println("collPhones1："+collPhones);
        // System.out.println("collQQs1："+collQQs);
        // System.out.println("collWXs1："+collWXs);
        // System.out.println("number2PersonBaseInfo1："+number2PersonBaseInfo);
        // 此时应查询对应的账号是否有直接联系
        // 对对应的电话号码进行对应的直接关系碰撞
        HashSet<String> relation = new HashSet<>();
        Integer size = relations.size();
        if (!CollectionUtils.isEmpty(collPhones)) {
            extractedStrightPhoneRelation(c2cs, relations, collPhones, number2PersonBaseInfo);
        }
        Integer size2 = relations.size();
        if (size2 - size > 0) {
            relation.add("phone");
        }
        // relations.forEach(t->{
        // System.out.println("t:"+t);
        // });
        // 查询WX的直接关系
        if (!CollectionUtils.isEmpty(collWXs)) {
            extractedStrightWXRelation(c2cs, relations, collWXs, number2PersonBaseInfo);
        }
        Integer size3 = relations.size();
        if (size3 - size2 > 0) {
            relation.add("wx");
        }
        // 查询QQ的直接联系
        // System.out.println("collQQs:"+collQQs);
        // System.out.println("number2PersonBaseInfo:"+number2PersonBaseInfo);
        if (!CollectionUtils.isEmpty(collQQs)) {
            extractedStrightQQRelation(c2cs, relations, collQQs, number2PersonBaseInfo);
        }
        Integer size4 = relations.size();
        if (size4 - size3 > 0) {
            relation.add("qq");
        }
        // relations.forEach(t12->{
        // System.out.println("t121:"+t12);
        // });
        // System.out.println("*************************");
        // 查询是否含有共同QQ好友
        if (!CollectionUtils.isEmpty(collQQs)) {
            extractedImpactByMainQQs(numbers, relations, dupNumbers, collQQs, c2cs, number2PersonBaseInfo);
        }
        Integer size5 = relations.size();
        if (size5 - size4 > 0) {
            relation.add("qq");
        }
        // 查询是否含有共同WX好友
        if (!CollectionUtils.isEmpty(collWXs)) {
            extractedImpactByMainWXs(numbers, relations, dupNumbers, collWXs, c2cs, number2PersonBaseInfo);
        }
        Integer size6 = relations.size();
        if (size6 - size5 > 0) {
            relation.add("wx");
        }
        // 查询对应是否有共同的通讯录好友
        if (!CollectionUtils.isEmpty(collPhones)) {

            // System.out.println("collPhones:"+collPhones);
            extractedImpactByMainPhones(numbers, relations, dupNumbers, collPhones, c2cs, number2PersonBaseInfo);
        }
        Integer size7 = relations.size();
        if (size7 - size6 > 0) {
            relation.add("phone");
        }
        result.put("relationType", relation);
        result.put("numbers", numbers);
        // System.out.println("numbers:"+numbers);
        result.put("relations", relations);
        // relations.forEach(t->{
        // System.out.println("1111111111:"+t);
        // });
//		System.out.println("result:" + result);
        Long b = System.currentTimeMillis();
        System.out.println("耗时:" + (b - a));
        return result;
    }

    private void getAllNumbersByWxOrID(List<Map<String, String>> numbers, HashSet<String> dupNumbers, List<Document> persons, HashSet<String> collPhones, HashSet<String> collQQs, HashSet<String> collWXs, Map<String, Map<String, String>> number2PersonBaseInfo, Map<String, ArrayList<String>> personIDNum2SearchNum, String wx) {
        persons.forEach(t -> {
            String personId = t.getString("personIdCard");
            ArrayList<String> searchPhones = personIDNum2SearchNum.get(personId);
            if (searchPhones == null) {
                searchPhones = new ArrayList<>();
                searchPhones.add(wx);
                // System.out.println("::::::::::::::::::"+searchPhones);
                personIDNum2SearchNum.put(personId, searchPhones);
            } else {
                // System.out.println("333333333333333333");
                searchPhones.add(wx);
                personIDNum2SearchNum.put(personId, searchPhones);
            }

            extractedGetNumber2PersonInfo(dupNumbers, numbers, collPhones, collQQs, collWXs,
                    number2PersonBaseInfo, t);
            // System.out.println("dupNumbers："+dupNumbers);
            // System.out.println("numbers："+numbers);
            // System.out.println("collPhones："+collPhones);
            // System.out.println("collQQs："+collQQs);
            // System.out.println("collWXs："+collWXs);
            // System.out.println("number2PersonBaseInfo："+number2PersonBaseInfo);
        });
    }

    private void getAllNumbersByPhoneOrQQ(List<Map<String, String>> numbers, HashSet<String> dupNumbers, List<Document> persons, HashSet<String> collPhones, HashSet<String> collQQs, HashSet<String> collWXs, Map<String, Map<String, String>> number2PersonBaseInfo, Map<String, ArrayList<String>> personIDNum2SearchNum, String phone) {
        if (!CollectionUtils.isEmpty(persons)) {// 包含采集账号
            getAllNumbersByWxOrID(numbers, dupNumbers, persons, collPhones, collQQs, collWXs, number2PersonBaseInfo, personIDNum2SearchNum, phone);
        } else {
            Map<String, String> phoneNode = new HashMap<>();
            phoneNode.put("num", phone);
            phoneNode.put("name", "");
            phoneNode.put("type", "phone");
            numbers.add(phoneNode);
        }
    }

    /**
     * 碰撞其共同的通讯录好友
     *
     * @param numbers               对应的节点信息
     * @param relations             对应的关系走势
     * @param dupNumbers            对应没有重复的身份ID
     * @param collWXs               对应的主控微信账号
     * @param number2PersonBaseInfo 对应的账号与人员的信息
     */
    private void extractedImpactByMainPhones(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
                                             HashSet<String> dupNumbers, HashSet<String> collWXs, HashSet<String> c2cs,
                                             Map<String, Map<String, String>> number2PersonBaseInfo) {
        // 查询出对应的碰撞信息
        System.out.println("进入共同通讯录好友碰撞");
        Long a = System.currentTimeMillis();
//		List<Document> impacts = ImpactRMIUtils.findImpactMsgByPhones(new ArrayList<>(collWXs), 2);
        List<DBObject> impactResult = impactServerDao.impactByNumsAndType(new ArrayList<>(collWXs), "PHONE");
        impactResult.forEach(t -> {
            // System.out.println("-----------------------------------------------");
            // System.out.println("t:"+t);
//			String impactWX = t.getString("_id");// 获取对应的碰撞QQ
//			List<Document> impactResult = ImpactRMIUtils.findimpactResultByImpactPHAndMPH(new ArrayList<>(collWXs),
//					impactWX);// 查询相对应的碰撞数据
            HashSet<String> wxPersonNums = new HashSet<>();
            String impactWX = t.get("_id").toString();
            BasicDBList aList = (BasicDBList) t.get("value");
            for (int i = 0; i < aList.size(); i++) {

                String mainWXUin = aList.get(i).toString();
                String personNums = number2PersonBaseInfo.get(mainWXUin).get("num");// 获取主控账号的对应的身份证号
                wxPersonNums.add(personNums);
            }
            if (wxPersonNums.size() > 1) {// 说明为两个人的共同好友
                // 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
                // 此时应查询对应的账号是否为对应的人
                BasicDBObject query = new BasicDBObject("phoneNUM", impactWX);
                List<Document> impactPersons = impactServerDao.findPersonExtendInfoByDoc(null, query);
                // System.out.println("impactPersons:"+impactPersons);
                // System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
                wxPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
                    if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
                        // System.out.println("impsctPersonsSize:"+impactPersons.size());
                        impactPersons.forEach(t2 -> {// 若一个QQ账号存在两个人使用
                            String wxToPersonIDCard = t2.getString("personIdCard");
                            if (!StringUtils.isEmpty(wxToPersonIDCard) && !wxToPersonIDCard.equals(t1)) {// 防止自己与自己连线
                                String c2cmark = Utils.StringUniqueMD5(t1 + "1", wxToPersonIDCard + "1");
                                // System.out.println("impactQQ:"+impactQQ);
                                if (!c2cs.contains(c2cmark)) {
                                    Map<String, Object> relation = new HashMap<>();
                                    relation.put("startNode", t1);
                                    relation.put("LinkName", "phone");
                                    relation.put("endNode", wxToPersonIDCard);
                                    // System.out.println("relation:" +
                                    // relation);
                                    // Integer size1 = relations.size();
                                    relations.add(relation);
                                    // Integer size2 = relations.size();
                                    // System.out.println("...........:"+(size2-size1));
                                }
                                c2cs.add(c2cmark);// 添加以免连线重复
                                Map<String, String> oneNode = new HashMap<>();
                                oneNode.put("num", wxToPersonIDCard);
                                oneNode.put("type", "person");
                                oneNode.put("name", t2.getString("personName"));
                                if (!dupNumbers.contains(wxToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
                                    dupNumbers.add(wxToPersonIDCard);
                                    numbers.add(oneNode);// 添加对应的节点信息
                                }
                            }
                        });
                    } else {// 若不是对应的人员,则为QQ账号
                        Map<String, Object> relation = new HashMap<>();
                        relation.put("startNode", t1);
                        relation.put("LinkName", "phone");
                        relation.put("endNode", impactWX);
                        String c2cmark = Utils.StringUniqueMD5(t1 + "1", impactWX + "1");
                        if (!c2cs.contains(c2cmark)) {
                            relations.add(relation);
                        }
                        c2cs.add(c2cmark);
                        Map<String, String> oneNode = new HashMap<>();
                        oneNode.put("num", impactWX);
                        oneNode.put("type", "phone");
                        oneNode.put("name", "");
                        if (!dupNumbers.contains(impactWX)) {// 若没有对应的节点信息,加入对应的节点信息
                            dupNumbers.add(impactWX);
                            numbers.add(oneNode);// 添加对应的节点信息
                        }
                    }
                });
            }
        });
        Long b = System.currentTimeMillis();
        System.out.println("耗时:" + (b - a));
        // System.out.println("通讯录relations:"+relations);
    }

//	private void extractedImpactByMainPhones(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
//			HashSet<String> dupNumbers, HashSet<String> collWXs, HashSet<String> c2cs,
//			Map<String, Map<String, String>> number2PersonBaseInfo) {
//		// 查询出对应的碰撞信息
//		 System.out.println("进入共同通讯录好友碰撞");
//		 Long a = System.currentTimeMillis();
////		List<Document> impacts = ImpactRMIUtils.findImpactMsgByPhones(new ArrayList<>(collWXs), 2);
//		List<DBObject> impactResult = ImpactRMIUtils.impactByNumsAndType(new ArrayList<>(collWXs),"PHONE");
//		HashSet<String> wxPersonNums = new HashSet<>();
//		List<BasicDBObject> query = new ArrayList<>();
//		List<String> impact = new ArrayList<>();
//		impactResult.forEach(t -> {
//			// System.out.println("-----------------------------------------------");
//			// System.out.println("t:"+t);
////			String impactWX = t.getString("_id");// 获取对应的碰撞QQ
////			List<Document> impactResult = ImpactRMIUtils.findimpactResultByImpactPHAndMPH(new ArrayList<>(collWXs),
////					impactWX);// 查询相对应的碰撞数据
//				String impactWX = t.get("_id").toString();
//				query.add(new BasicDBObject("phoneNUM", impactWX));
//				impact.add(impactWX);
//				BasicDBList aList = (BasicDBList) t.get("value");
//				for (int i = 0; i < aList.size(); i++) {
//					String mainWXUin = aList.get(i).toString();
//					String personNums = number2PersonBaseInfo.get(mainWXUin).get("num");// 获取主控账号的对应的身份证号
//					wxPersonNums.add(personNums);
//				}
//		});
//			if (wxPersonNums.size() > 1) {// 说明为两个人的共同好友
//				// 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
//				// 此时应查询对应的账号是否为对应的人
//				List<Document> impactPersons = VirtualIdentityRMIUtils.findPersonExtendInfoByDoc(null, query,"PHONE");
//				// System.out.println("impactPersons:"+impactPersons);
//				// System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
//				wxPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
//					if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
//						// System.out.println("impsctPersonsSize:"+impactPersons.size());
//						impactPersons.forEach(t2 -> {// 若一个QQ账号存在两个人使用
//							String wxToPersonIDCard = t2.getString("personIdCard");
//							if (!StringUtils.isEmpty(wxToPersonIDCard) && !wxToPersonIDCard.equals(t1)) {// 防止自己与自己连线
//								String c2cmark = Utils.StringUniqueMD5(t1 + "1", wxToPersonIDCard + "1");
//								// System.out.println("impactQQ:"+impactQQ);
//								if (!c2cs.contains(c2cmark)) {
//									Map<String, Object> relation = new HashMap<>();
//									relation.put("startNode", t1);
//									relation.put("LinkName", "phone");
//									relation.put("endNode", wxToPersonIDCard);
//									// System.out.println("relation:" +
//									// relation);
//									// Integer size1 = relations.size();
//									relations.add(relation);
//									// Integer size2 = relations.size();
//									// System.out.println("...........:"+(size2-size1));
//								}
//								c2cs.add(c2cmark);// 添加以免连线重复
//								Map<String, String> oneNode = new HashMap<>();
//								oneNode.put("num", wxToPersonIDCard);
//								oneNode.put("type", "person");
//								oneNode.put("name", t2.getString("personName"));
//								if (!dupNumbers.contains(wxToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
//									dupNumbers.add(wxToPersonIDCard);
//									numbers.add(oneNode);// 添加对应的节点信息
//								}
//							}
//						});
//					} else {
//						// 若不是对应的人员,则为QQ账号
//						for (int i = 0; i < impact.size(); i++) {
//							Map<String, Object> relation = new HashMap<>();
//							relation.put("startNode", t1);
//							relation.put("LinkName", "phone");
//							relation.put("endNode", impact.get(i));
//							String c2cmark = Utils.StringUniqueMD5(t1 + "1", impact.get(i) + "1");
//							if (!c2cs.contains(c2cmark)) {
//								relations.add(relation);
//							}
//							c2cs.add(c2cmark);
//							Map<String, String> oneNode = new HashMap<>();
//							oneNode.put("num", impact.get(i));
//							oneNode.put("type", "phone");
//							oneNode.put("name", "");
//							if (!dupNumbers.contains(impact.get(i))) {// 若没有对应的节点信息,加入对应的节点信息
//								dupNumbers.add(impact.get(i));
//								numbers.add(oneNode);// 添加对应的节点信息
//							}
//						}
//						
//					}
//				});
//			}
//		Long b = System.currentTimeMillis();
//		System.out.println("耗时:"+(b-a));
//		// System.out.println("通讯录relations:"+relations);
//	}

    /**
     * 碰撞其共同的微信好友
     *
     * @param numbers               对应的节点信息
     * @param relations             对应的关系走势
     * @param dupNumbers            对应没有重复的身份ID
     * @param collWXs               对应的主控微信账号
     * @param number2PersonBaseInfo 对应的账号与人员的信息
     */
    private void extractedImpactByMainWXs(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
                                          HashSet<String> dupNumbers, HashSet<String> collWXs, HashSet<String> c2cs,
                                          Map<String, Map<String, String>> number2PersonBaseInfo) {
        // 查询出对应的碰撞信息
        System.out.println("进入共同微信好友碰撞");
        Long a = System.currentTimeMillis();
        List<Document> impacts = impactServerDao.findImpactMsg(new ArrayList<>(collWXs), 2);
        impacts.forEach(t -> {
            // System.out.println("-----------------------------------------------");
            // System.out.println("t:"+t);

            String impactWX = t.getString("_id");// 获取对应的碰撞wx
            List<Document> wx = impactServerDao.findwx(impactWX);// 查询WX用户的基本信息
            if (!CollectionUtils.isEmpty(wx) && !com.hnf.honeycomb.util.Utils.isWXOfficalAccount(wx.get(0))) { // 查看是否为WX公众号
                List<Document> impactResult = impactServerDao.findimpactResultByImpactWXAndMWX(new ArrayList<>(collWXs),
                        impactWX);// 查询相对应的碰撞数据
                HashSet<String> wxPersonNums = new HashSet<>();
                impactResult.forEach(t1 -> {
                    String mainWXUin = t1.getString("username");
                    String personNums = number2PersonBaseInfo.get(mainWXUin).get("num");// 获取主控账号的对应的身份证号
                    // System.out.println("mainWXUin:"+mainWXUin);
                    // System.out.println("personNums:"+personNums);
                    wxPersonNums.add(personNums);
                });
                if (wxPersonNums.size() > 1) {// 说明为两个人的共同好友
                    // 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
                    // 此时应查询对应的账号是否为对应的人
                    BasicDBObject query = new BasicDBObject("WXNUM", impactWX);
                    List<Document> impactPersons = impactServerDao.findPersonExtendInfoByDoc(null, query);
                    // System.out.println("impactPersons:"+impactPersons);
                    // System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
                    wxPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
                        if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
                            // System.out.println("impsctPersonsSize:"+impactPersons.size());
                            impactPersons.forEach(t2 -> {// 若一个QQ账号存在两个人使用
                                String wxToPersonIDCard = t2.getString("personIdCard");
                                if (!StringUtils.isEmpty(wxToPersonIDCard) && !wxToPersonIDCard.equals(t1)) {// 防止自己与自己连线
                                    String c2cmark = Utils.StringUniqueMD5(t1 + "2", wxToPersonIDCard + "2");
                                    // System.out.println("impactQQ:"+impactQQ);
                                    if (!c2cs.contains(c2cmark)) {
                                        Map<String, Object> relation = new HashMap<>();
                                        relation.put("startNode", t1);
                                        relation.put("LinkName", "wx");
                                        relation.put("endNode", wxToPersonIDCard);
                                        // System.out.println("relation:" +
                                        // relation);
                                        // Integer size1 = relations.size();
                                        relations.add(relation);
                                        // Integer size2 = relations.size();
                                        // System.out.println("...........:"+(size2-size1));
                                    }
                                    c2cs.add(c2cmark);// 添加以免连线重复
                                    Map<String, String> oneNode = new HashMap<>();
                                    oneNode.put("num", wxToPersonIDCard);
                                    oneNode.put("type", "person");
                                    oneNode.put("name", t2.getString("personName"));
                                    if (!dupNumbers.contains(wxToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
                                        dupNumbers.add(wxToPersonIDCard);
                                        numbers.add(oneNode);// 添加对应的节点信息
                                    }
                                }
                            });
                        } else {// 若不是对应的人员,则为QQ账号
                            Map<String, Object> relation = new HashMap<>();
                            relation.put("startNode", t1);
                            relation.put("LinkName", "wx");
                            relation.put("endNode", impactWX);
                            String c2cmark = Utils.StringUniqueMD5(t1 + "2", impactWX + "2");
                            if (!c2cs.contains(c2cmark)) {
                                relations.add(relation);
                            }
                            c2cs.add(c2cmark);
                            Map<String, String> oneNode = new HashMap<>();
                            oneNode.put("num", impactWX);
                            oneNode.put("type", "wx");
                            oneNode.put("name", "");
                            if (!CollectionUtils.isEmpty(wx)) {
                                oneNode.put("name", wx.get(0).getString("nickname"));
                            }
                            if (!dupNumbers.contains(impactWX)) {// 若没有对应的节点信息,加入对应的节点信息
                                dupNumbers.add(impactWX);
                                numbers.add(oneNode);// 添加对应的节点信息
                            }
                        }
                    });
                }
            }

        });
        Long b = System.currentTimeMillis();
        System.out.println("耗时:" + (b - a));
        // System.out.println("微信relations:"+relations);
    }

//	private void extractedImpactByMainWXs(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
//			HashSet<String> dupNumbers, HashSet<String> collWXs, HashSet<String> c2cs,
//			Map<String, Map<String, String>> number2PersonBaseInfo) {
//		// 查询出对应的碰撞信息
//		 System.out.println("进入共同微信好友碰撞");
//		 Long a = System.currentTimeMillis();
//		List<Document> impacts = WXRMIUtils.findImpactMsg(new ArrayList<>(collWXs), 2);
//		HashSet<String> wxPersonNums = new HashSet<>();
//		List<BasicDBObject> query = new ArrayList<>();
//		List<String> impact = new ArrayList<>();
//		List<List<Document>> wxlist = new ArrayList<>();
//		impacts.forEach(t -> {
//			// System.out.println("-----------------------------------------------");
//			// System.out.println("t:"+t);
//			String impactWX = t.getString("_id");// 获取对应的碰撞wx
//			List<Document> wx = WXRMIUtils.findwx(impactWX);
//			wxlist.add(wx);
//			query.add(new BasicDBObject("WXNUM", impactWX));
//			impact.add(impactWX);
//			if (!CollectionUtils.isEmpty(wx) && !com.hnf.communicate.util.Utils.isWXOfficalAccount(wx.get(0))) { // 查看是否为WX公众号
//				List<Document> impactResult = WXRMIUtils.findimpactResultByImpactWXAndMWX(new ArrayList<>(collWXs),
//						impactWX);// 查询相对应的碰撞数据
//				
//				impactResult.forEach(t1 -> {
//					String mainWXUin = t1.getString("username");
//					String personNums = number2PersonBaseInfo.get(mainWXUin).get("num");// 获取主控账号的对应的身份证号
//					// System.out.println("mainWXUin:"+mainWXUin);
//					// System.out.println("personNums:"+personNums);
//					wxPersonNums.add(personNums);
//				});
//			}
//			});
//				if (wxPersonNums.size() > 1) {// 说明为两个人的共同好友
//					// 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
//					// 此时应查询对应的账号是否为对应的人
//					List<Document> impactPersons = VirtualIdentityRMIUtils.findPersonExtendInfoByDoc(null, query,"WX");
//					// System.out.println("impactPersons:"+impactPersons);
//					// System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
//					wxPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
//						if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
//							// System.out.println("impsctPersonsSize:"+impactPersons.size());
//							impactPersons.forEach(t2 -> {// 若一个QQ账号存在两个人使用
//								String wxToPersonIDCard = t2.getString("personIdCard");
//								if (!StringUtils.isEmpty(wxToPersonIDCard) && !wxToPersonIDCard.equals(t1)) {// 防止自己与自己连线
//									String c2cmark = Utils.StringUniqueMD5(t1 + "2", wxToPersonIDCard + "2");
//									// System.out.println("impactQQ:"+impactQQ);
//									if (!c2cs.contains(c2cmark)) {
//										Map<String, Object> relation = new HashMap<>();
//										relation.put("startNode", t1);
//										relation.put("LinkName", "wx");
//										relation.put("endNode", wxToPersonIDCard);
//										// System.out.println("relation:" +
//										// relation);
//										// Integer size1 = relations.size();
//										relations.add(relation);
//										// Integer size2 = relations.size();
//										// System.out.println("...........:"+(size2-size1));
//									}
//									c2cs.add(c2cmark);// 添加以免连线重复
//									Map<String, String> oneNode = new HashMap<>();
//									oneNode.put("num", wxToPersonIDCard);
//									oneNode.put("type", "person");
//									oneNode.put("name", t2.getString("personName"));
//									if (!dupNumbers.contains(wxToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
//										dupNumbers.add(wxToPersonIDCard);
//										numbers.add(oneNode);// 添加对应的节点信息
//									}
//								}
//							});
//						} else {// 若不是对应的人员,则为QQ账号
//							for (int i = 0; i < impact.size(); i++) {
//								Map<String, Object> relation = new HashMap<>();
//								relation.put("startNode", t1);
//								relation.put("LinkName", "wx");
//								relation.put("endNode", impact.get(i));
//								String c2cmark = Utils.StringUniqueMD5(t1 + "2", impact.get(i) + "2");
//								if (!c2cs.contains(c2cmark)) {
//									relations.add(relation);
//								}
//								c2cs.add(c2cmark);
//								Map<String, String> oneNode = new HashMap<>();
//								oneNode.put("num", impact.get(i));
//								oneNode.put("type", "wx");
//								oneNode.put("name", "");
//								for (int j = 0; j < wxlist.size(); j++) {
//									if (!CollectionUtils.isEmpty(wxlist.get(j))) {
//										oneNode.put("name", wxlist.get(j).get(0).getString("nickname"));
//									}
//								}
//								if (!dupNumbers.contains(impact.get(i))) {// 若没有对应的节点信息,加入对应的节点信息
//									dupNumbers.add(impact.get(i));
//									numbers.add(oneNode);// 添加对应的节点信息
//								}
//							}
//							
//						}
//					});
//				}
//		Long b = System.currentTimeMillis();
//		System.out.println("耗时:"+(b-a));
//		// System.out.println("微信relations:"+relations);
//	}

    /**
     * 碰撞其共同的QQ好友
     *
     * @param numbers               对应的节点信息
     * @param relations             对应的关系走势
     * @param dupNumbers            对应没有重复的身份ID
     * @param collQQs               对应的主控QQ账号
     * @param number2PersonBaseInfo 对应的账号与人员的信息
     */


    private void extractedImpactByMainQQs(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
                                          HashSet<String> dupNumbers, HashSet<String> collQQs, HashSet<String> c2cs,
                                          Map<String, Map<String, String>> number2PersonBaseInfo) {
        // 查询出对应的碰撞信息
        //
        // System.out.println("collQQs:"+collQQs);
        System.out.println("进入共同QQ好友碰撞");
        Long a = System.currentTimeMillis();
        // List<Document> impacts = ImpactRMIUtils.findImpactMsgByQQUins(new
        // ArrayList<>(collQQs), 2);
        // System.out.println("impacts:" + impacts);
        List<DBObject> impactResult = impactServerDao.impactByNumsAndType(new ArrayList<>(collQQs), "QQ");
        Long c = System.currentTimeMillis();
        System.out.println("hh:" + (c - a));
        impactResult.forEach(t -> {
            // System.out.println("-----------------------------------------------");
            // System.out.println("t:"+t);
            // String impactQQ = t.getString("_id");// 获取对应的碰撞QQ
            // List<Document> impactResult =
            // ImpactRMIUtils.findimpactResultByImpactQQAndMQQ(new
            // ArrayList<>(collQQs),
            // impactQQ);// 查询相对应的碰撞数据
            // System.out.println("impactResult:" + impactResult);
            HashSet<String> qqPersonNums = new HashSet<>();
            String impactQQ = t.get("_id").toString();
            BasicDBList aList = (BasicDBList) t.get("value");
            for (int i = 0; i < aList.size(); i++) {

                String mainQQUin = aList.get(i).toString();
                String personNums = number2PersonBaseInfo.get(mainQQUin).get("num");// 获取主控账号的对应的身份证号
                qqPersonNums.add(personNums);
            }
            if (qqPersonNums.size() > 1) {// 说明为两个人的共同好友
                // 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
                // 此时应查询对应的账号是否为对应的人
                BasicDBObject query = new BasicDBObject("QQNUM", impactQQ);
                List<Document> impactPersons = impactServerDao.findPersonExtendInfoByDoc(null, query);
                // System.out.println("impactPersons:" + impactPersons);
                // System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
                qqPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
                    if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
                        // System.out.println("impsctPersonsSize:"+impactPersons.size());
                        impactPersons.forEach(t2 -> {// 若一个QQ账号存在两个人使用
                            String qqToPersonIDCard = t2.getString("personIdCard");
                            if (!StringUtils.isEmpty(qqToPersonIDCard) && !qqToPersonIDCard.equals(t1)) {// 防止自己与自己连线
                                String c2cmark = Utils.StringUniqueMD5(t1 + "3", qqToPersonIDCard + "3");
                                // System.out.println("impactQQ:"+impactQQ);
                                if (!c2cs.contains(c2cmark)) {
                                    Map<String, Object> relation = new HashMap<>();
                                    relation.put("startNode", t1);
                                    relation.put("LinkName", "qq");
                                    relation.put("endNode", qqToPersonIDCard);
                                    // System.out.println("relation:" +
                                    // relation);
                                    // Integer size1 = relations.size();
                                    relations.add(relation);
                                    // Integer size2 = relations.size();
                                    // System.out.println("...........:"+(size2-size1));
                                }
                                c2cs.add(c2cmark);// 添加以免连线重复
                                Map<String, String> oneNode = new HashMap<>();
                                oneNode.put("num", qqToPersonIDCard);
                                oneNode.put("type", "person");
                                oneNode.put("name", t2.getString("personName"));
                                if (!dupNumbers.contains(qqToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
                                    dupNumbers.add(qqToPersonIDCard);
                                    numbers.add(oneNode);// 添加对应的节点信息
                                }
                            }
                        });
                    } else {// 若不是对应的人员,则为QQ账号
                        Map<String, Object> relation = new HashMap<>();
                        relation.put("startNode", t1);
                        relation.put("LinkName", "qq");
                        relation.put("endNode", impactQQ);
                        String c2cmark = Utils.StringUniqueMD5(t1 + "3", impactQQ + "3");
                        if (!c2cs.contains(c2cmark)) {
                            relations.add(relation);
                        }
                        c2cs.add(c2cmark);
                        Map<String, String> oneNode = new HashMap<>();
                        oneNode.put("num", impactQQ);
                        oneNode.put("type", "qq");
                        oneNode.put("name", "");
                        List<Document> qq = impactServerDao.findqq(impactQQ);// 查询QQ用户的基本信息
                        if (!CollectionUtils.isEmpty(qq)) {
                            oneNode.put("name", qq.get(0).getString("nickname"));
                        }
                        if (!dupNumbers.contains(impactQQ)) {// 若没有对应的节点信息,加入对应的节点信息
                            dupNumbers.add(impactQQ);
                            numbers.add(oneNode);// 添加对应的节点信息
                        }
                    }
                });
            }
        });
        Long d = System.currentTimeMillis();
        System.out.println("hhh:" + (d - c));
        Long b = System.currentTimeMillis();
        System.out.println("耗时:" + (b - a));
        // System.out.println("QQrelations:" + relations);
    }


//	private void extractedImpactByMainQQs(List<Map<String, String>> numbers, List<Map<String, Object>> relations,
//			HashSet<String> dupNumbers, HashSet<String> collQQs, HashSet<String> c2cs,
//			Map<String, Map<String, String>> number2PersonBaseInfo) {
//		// 查询出对应的碰撞信息
//		//
//		// System.out.println("collQQs:"+collQQs);
//		System.out.println("进入共同QQ好友碰撞");
//		Long a = System.currentTimeMillis();
//		// List<Document> impacts = ImpactRMIUtils.findImpactMsgByQQUins(new
//		// ArrayList<>(collQQs), 2);
//		// System.out.println("impacts:" + impacts);
//		List<DBObject> impactResult = ImpactRMIUtils.impactByNumsAndType(new ArrayList<>(collQQs),"QQ");
//		Long c = System.currentTimeMillis();
//		System.out.println("hh:" + (c - a));
//		HashSet<String> qqPersonNums = new HashSet<>();
//		List<BasicDBObject> query = new ArrayList<>();
//		List<String> impact = new ArrayList<>();
//		impactResult.forEach(t -> {
//			// System.out.println("-----------------------------------------------");
//			// System.out.println("t:"+t);
//			// String impactQQ = t.getString("_id");// 获取对应的碰撞QQ
//			// List<Document> impactResult =
//			// ImpactRMIUtils.findimpactResultByImpactQQAndMQQ(new
//			// ArrayList<>(collQQs),
//			// impactQQ);// 查询相对应的碰撞数据
//			// System.out.println("impactResult:" + impactResult);
//			
//			String impactQQ = t.get("_id").toString();//key 共同好友
//			query.add(new BasicDBObject("QQNUM", impactQQ));//添加查询条件
//			impact.add(impactQQ);//取出碰撞的qq存到list
//			BasicDBList aList = (BasicDBList) t.get("value"); // 共同好友对应的qq号
//			System.out.println("Alist"+aList);
//			System.out.println("impactQQ:"+impactQQ);
//			for (int i = 0; i < aList.size(); i++) {
//				String mainQQUin = aList.get(i).toString();
//				String personNums = number2PersonBaseInfo.get(mainQQUin).get("num");// 获取主控账号的对应的身份证号
//				qqPersonNums.add(personNums);//利用hashset 保证身份证唯一性，避免重复的人
//			}
//		});
//			if (qqPersonNums.size() > 1) {// 说明为两个人的共同好友
//				// 此时查询对应的数据的QQ账号节点,并将对应的碰撞信息加入到对应的关系表以及对应的节点信息中,并且查询对应的QQ账号是否为人
//				// 此时应查询对应的账号是否为对应的人
//				List<Document> impactPersons = VirtualIdentityRMIUtils.findPersonExtendInfoByDoc(null,query,"QQ");
//				// System.out.println("impactPersons:" + impactPersons);
//				// System.out.println(impactQQ+",qqPersonNumbers:"+qqPersonNums);
//				qqPersonNums.forEach(t1 -> {// 此为不会重复的对应信息
//					if (!CollectionUtils.isEmpty(impactPersons)) {// 此时其为对应的人员,则与人员连线
//						// System.out.println("impsctPersonsSize:"+impactPersons.size());
//						impactPersons.forEach(t2 -> {// .
//							String qqToPersonIDCard = t2.getString("personIdCard");
//							if (!StringUtils.isEmpty(qqToPersonIDCard) && !qqToPersonIDCard.equals(t1)) {// 防止自己与自己连线
//								String c2cmark = Utils.StringUniqueMD5(t1 + "3", qqToPersonIDCard + "3");
//								// System.out.println("impactQQ:"+impactQQ);
//								if (!c2cs.contains(c2cmark)) {
//									Map<String, Object> relation = new HashMap<>();
//									relation.put("startNode", t1);
//									relation.put("LinkName", "qq");
//									relation.put("endNode", qqToPersonIDCard);
//									// System.out.println("relation:" +
//									// relation);
//									// Integer size1 = relations.size();
//									relations.add(relation);
//									// Integer size2 = relations.size();
//									// System.out.println("...........:"+(size2-size1));
//								}
//								c2cs.add(c2cmark);// 添加以免连线重复
//								Map<String, String> oneNode = new HashMap<>();
//								oneNode.put("num", qqToPersonIDCard);
//								oneNode.put("type", "person");
//								oneNode.put("name", t2.getString("personName"));
//								if (!dupNumbers.contains(qqToPersonIDCard)) {// 若没有对应的节点信息,加入对应的节点信息
//									dupNumbers.add(qqToPersonIDCard);
//									numbers.add(oneNode);// 添加对应的节点信息
//								}
//							}
//						});
//					} else {// 若不是对应的人员,则为QQ账号
//						for(int i =0;i<impact.size();i++){
//							Map<String, Object> relation = new HashMap<>();
//							relation.put("startNode", t1);
//							relation.put("LinkName", "qq");
//							relation.put("endNode", impact.get(i));
//							String c2cmark = Utils.StringUniqueMD5(t1 + "3", impact.get(i) + "3");
//							if (!c2cs.contains(c2cmark)) {
//								relations.add(relation);
//							}
//							c2cs.add(c2cmark);
//							Map<String, String> oneNode = new HashMap<>();
//							oneNode.put("num", impact.get(i));
//							oneNode.put("type", "qq");
//							oneNode.put("name", "");
//							List<Document> qq = QQRMIUtils.findqq(impact.get(i));// 查询QQ用户的基本信息
//							if (!CollectionUtils.isEmpty(qq)) {
//								oneNode.put("name", qq.get(0).getString("nickname"));
//							}
//							if (!dupNumbers.contains(impact.get(i))) {// 若没有对应的节点信息,加入对应的节点信息
//								dupNumbers.add(impact.get(i));
//								numbers.add(oneNode);// 添加对应的节点信息
//							}
//						}
//					}
//				});
//			}
//		Long d = System.currentTimeMillis();
//		System.out.println("hhh:" + (d - c));
//		Long b = System.currentTimeMillis();
//		System.out.println("耗时:" + (b - a));
//		// System.out.println("QQrelations:" + relations);
//	}


    /**
     * 通过对应的的账号查询电话号码之间的直接联系
     *
     * @param relations             对应的关系集合
     * @param collQQs               对应的电话号码集合
     * @param number2PersonBaseInfo 对应的账号与人员基本信息的集合
     */
    private void extractedStrightQQRelation(HashSet<String> c2cs, List<Map<String, Object>> relations,
                                            HashSet<String> collQQs, Map<String, Map<String, String>> number2PersonBaseInfo) {
        BasicDBObject strightReaWXQuery = new BasicDBObject()
                .append("QQNUM", new BasicDBObject(QueryOperators.IN, collQQs.toArray(new String[]{})))
                .append("QQFNUM", new BasicDBObject(QueryOperators.IN, collQQs.toArray(new String[]{})));
        // 碰撞电话直接关系
        // System.out.println("strightReaQQQuery:"+strightReaWXQuery);
        List<Document> strightWXRelation = impactServerDao.impactStgtReaBySearchNumsAndType(strightReaWXQuery, "QQFREM");
        if (!CollectionUtils.isEmpty(strightWXRelation)) {
            strightWXRelation.forEach(t -> {
                String selfPhone = t.getString("QQNUM");
                String otherPeoplePhone = t.getString("QQFNUM");
                Map<String, Object> phoneRelation = new HashMap<>();
                // System.out.println("selfQQ:"+selfPhone);
                String startNode = number2PersonBaseInfo.get(selfPhone).get("num");
                String endNode = number2PersonBaseInfo.get(otherPeoplePhone).get("num");
                if (!StringUtils.isEmpty(startNode) && !startNode.equals(endNode)) {
                    phoneRelation.put("startNode", startNode);
                    phoneRelation.put("endNode", endNode);
                    phoneRelation.put("LinkName", "qq");
                    phoneRelation.put("type", 0);
                    // System.out.println(startNode+","+endNode);
                    // 添加对应的关联关系
                    String c2cmark = Utils.StringUniqueMD5(startNode + "3", endNode + "3");
                    if (!c2cs.contains(c2cmark)) {
                        relations.add(phoneRelation);
                    }
                    c2cs.add(c2cmark);
                }
            });
        }
    }

    /**
     * 通过对应的的账号查询电话号码之间的直接联系
     *
     * @param relations             对应的关系集合
     * @param collWXs               对应的电话号码集合
     * @param number2PersonBaseInfo 对应的账号与人员基本信息的集合
     */
    private void extractedStrightWXRelation(HashSet<String> c2cs, List<Map<String, Object>> relations,
                                            HashSet<String> collWXs, Map<String, Map<String, String>> number2PersonBaseInfo) {
        BasicDBObject strightReaWXQuery = new BasicDBObject()
                .append("username", new BasicDBObject(QueryOperators.IN, collWXs.toArray(new String[]{})))
                .append("fusername", new BasicDBObject(QueryOperators.IN, collWXs.toArray(new String[]{})));
        // 碰撞电话直接关系
        List<Document> strightWXRelation = impactServerDao.impactStgtReaBySearchNumsAndType(strightReaWXQuery,
                "t_wxuser_friend");
        if (!CollectionUtils.isEmpty(strightWXRelation)) {
            strightWXRelation.forEach(t -> {
                String selfPhone = t.getString("username");
                String otherPeoplePhone = t.getString("fusername");
                Map<String, Object> phoneRelation = new HashMap<>();
                String startNode = number2PersonBaseInfo.get(selfPhone).get("num");
                String endNode = number2PersonBaseInfo.get(otherPeoplePhone).get("num");
                if (!StringUtils.isEmpty(startNode) && !startNode.equals(endNode)) {
                    phoneRelation.put("startNode", startNode);
                    phoneRelation.put("endNode", endNode);
                    phoneRelation.put("LinkName", "wx");
                    phoneRelation.put("type", 0);
                    // 添加对应的关联关系
                    // String c2cmark = Utils.NumberStringUniqueMD5(qqUserUin,
                    // qqFriendUin);//数字生成md5
                    // String c2cmark = Utils.StringUniqueMD5(wxUserName,
                    // wxFriendName);字符串生成md5
                    String c2cmark = Utils.StringUniqueMD5(startNode + "2", endNode + "2");
                    if (!c2cs.contains(c2cmark)) {
                        relations.add(phoneRelation);
                    }
                    c2cs.add(c2cmark);
                }
            });
        }
    }

    /**
     * 通过对应的的账号查询电话号码之间的直接联系
     *
     * @param relations             对应的关系集合
     * @param collPhones            对应的电话号码集合
     * @param number2PersonBaseInfo 对应的账号与人员基本信息的集合
     */
    private void extractedStrightPhoneRelation(HashSet<String> c2cs, List<Map<String, Object>> relations,
                                               HashSet<String> collPhones, Map<String, Map<String, String>> number2PersonBaseInfo) {
        BasicDBObject strightReaPhoneQuery = new BasicDBObject()
                .append("PHNUM", new BasicDBObject(QueryOperators.IN, collPhones.toArray(new String[]{})))
                .append("PHFNUM", new BasicDBObject(QueryOperators.IN, collPhones.toArray(new String[]{})));
        // 碰撞电话直接关系
        List<Document> strightPhoneRelation = impactServerDao.impactStgtReaBySearchNumsAndType(strightReaPhoneQuery,
                "PHREM");
        // System.out.println("phoneRelation:"+strightPhoneRelation);
        if (!CollectionUtils.isEmpty(strightPhoneRelation)) {
            strightPhoneRelation.forEach(t -> {
                String selfPhone = t.getString("PHNUM");
                String otherPeoplePhone = t.getString("PHFNUM");
                Map<String, Object> phoneRelation = new HashMap<>();
                String startNode = number2PersonBaseInfo.get(selfPhone).get("num");
                String endNode = number2PersonBaseInfo.get(otherPeoplePhone).get("num");
                if (!StringUtils.isEmpty(startNode) && !startNode.equals(endNode)) {
                    phoneRelation.put("startNode", startNode);
                    phoneRelation.put("endNode", endNode);
                    phoneRelation.put("LinkName", "phone");
                    phoneRelation.put("type", 0);
                    // 添加对应的关联关系
                    String c2cmark = Utils.StringUniqueMD5(startNode + "1", endNode + "1");// 代表两人已存在电话关系
                    if (!c2cs.contains(c2cmark)) {
                        relations.add(phoneRelation);
                    }
                    c2cs.add(c2cmark);
                }
            });
        }
    }

    private void extractedGetNumber2PersonInfo(HashSet<String> dupNumbers, List<Map<String, String>> numbers,
                                               HashSet<String> collPhones, HashSet<String> collQQs, HashSet<String> collWXs,
                                               Map<String, Map<String, String>> number2PersonBaseInfo, Document t) {
        Map<String, String> personBaseInfo = new HashMap<>();
        String personIDCard = t.getString("personIdCard");
        if (dupNumbers.contains(personIDCard)) {
            return;
        }
        dupNumbers.add(personIDCard);
        String personName = t.getString("personName");
        personBaseInfo.put("num", personIDCard);
        personBaseInfo.put("name", personName);
        personBaseInfo.put("type", "person");
        numbers.add(personBaseInfo);
        ArrayList<?> phoneNumbers = t.get("phoneNUM", ArrayList.class);
        putNumbe2PersonBaseInfo(collPhones, number2PersonBaseInfo, personBaseInfo, phoneNumbers);
        ArrayList<?> qqNumbers = t.get("QQNUM", ArrayList.class);
        putNumbe2PersonBaseInfo(collQQs, number2PersonBaseInfo, personBaseInfo, qqNumbers);
        ArrayList<?> wxNumbers = t.get("WXNUM", ArrayList.class);
        putNumbe2PersonBaseInfo(collWXs, number2PersonBaseInfo, personBaseInfo, wxNumbers);
    }

    private void putNumbe2PersonBaseInfo(HashSet<String> collPhones, Map<String, Map<String, String>> number2PersonBaseInfo, Map<String, String> personBaseInfo, ArrayList<?> phoneNumbers) {
        if (!CollectionUtils.isEmpty(phoneNumbers)) {
            phoneNumbers.forEach(t1 -> {
                if (t1 != null && !StringUtils.isEmpty(t1.toString())) {
                    collPhones.add(t1.toString());
                    number2PersonBaseInfo.put(t1.toString(), personBaseInfo);
                }
            });
        }
    }

    @Override
    public List<Map<String, Object>> findNumInfo(String searchType, String searchNumber) {
        if (StringUtils.isEmpty(searchType)) {
            throw new RuntimeException("对应的搜索账号类型为null");
        }
        if (StringUtils.isEmpty(searchNumber)) {
            throw new RuntimeException("对应的搜索账号为null");
        }
        String[] searchNumbers = com.hnf.honeycomb.util.Utils.bongDuplicateremove(searchNumber.split(","));
        BasicDBObject personQuery = new BasicDBObject();// 对应的人员查询条件
        List<Map<String, Object>> persons = new ArrayList<>();// 每次搜索人员存储对应的信息
        List<String> likePhone = new ArrayList<>();
        List<String> likeWX = new ArrayList<>();
        List<String> likeQQ = new ArrayList<>();
        List<String> likeID = new ArrayList<>();
        switch (searchType) {
            case "phone":
                for (String phone : searchNumbers) {
                    List<Document> personPhone = impactServerDao.likePersonPhone(phone);
                    likePhone.addAll(impactServerDao.getPhoneFromPerson(personPhone));
                    likePhone.addAll(impactServerDao.likePhonenum(phone));
                }
                if (!CollectionUtils.isEmpty(likePhone)) {
                    for (String str : likePhone) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("number", str);
                        map.put("name", "");
                        personQuery.append("phoneNUM", str);
                        List<Document> findP = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                        if (!CollectionUtils.isEmpty(findP)) {
                            map.put("name", findP.get(0).get("personName"));
                        }
                        persons.add(map);
                    }
                }
                break;
            case "wx":
                for (String wx : searchNumbers) {
                    likeWX.addAll(impactServerDao.likeWxUsername(wx));
                }
                if (!CollectionUtils.isEmpty(likeWX)) {
                    for (String str : likeWX) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("number", str);
                        map.put("name", "");
                        personQuery.append("WXNUM", str);
                        List<Document> findP = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                        if (!CollectionUtils.isEmpty(findP)) {
                            map.put("name", findP.get(0).get("personName"));
                        }
                        persons.add(map);
                    }
                }
                break;
            case "qq":
                for (String qq : searchNumbers) {
                    likeQQ.addAll(impactServerDao.likeQqUin(qq));
                }
                if (!CollectionUtils.isEmpty(likeQQ)) {
                    for (String str : likeQQ) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("number", str);
                        map.put("name", "");
                        personQuery.append("QQNUM", str);
                        List<Document> findP = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                        if (!CollectionUtils.isEmpty(findP)) {
                            map.put("name", findP.get(0).getString("personName"));
                        }
                        persons.add(map);
                    }
                }
                break;
            case "IDNumber":

                Pattern pattern = Pattern.compile("^" + searchNumber.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                personQuery.append("personIdCard", pattern);
                List<Document> findP = impactServerDao.findPersonExtendInfoByDoc(null, personQuery);
                if (!CollectionUtils.isEmpty(findP)) {
                    for (Document doc : findP) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("number", doc.getString("personIdCard"));
                        map.put("name", doc.getString("personName"));
                        persons.add(map);
                    }
                }
                break;
            default:
                throw new RuntimeException("对应的搜索类型不合规");
        }
        return persons;
    }

}
