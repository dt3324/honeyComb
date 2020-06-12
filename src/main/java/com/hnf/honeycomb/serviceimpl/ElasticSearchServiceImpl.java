package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.bean.AllQueryBean;
import com.hnf.honeycomb.dao.ElasticSearchDao;
import com.hnf.honeycomb.dao.EsBaseMongoDao;
import com.hnf.honeycomb.service.ElasticSearchService;
import com.hnf.honeycomb.service.EsDeviceService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.ESSearchUtil;
import com.hnf.honeycomb.util.StringUtils;
import com.hnf.honeycomb.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import org.bson.Document;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author admin
 */
@Service("elasticSearchService")
@EnableCaching
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Resource
    private ESSearchUtil eSSearchUtil;
    /**
     * 判断搜索条件是否由数字字母组成
     */
    private static final String REG = "^[a-zA-Z\\d]+$";
    /**
     * 私有静态常量，用于生成唯一的redis key
     */
    @Resource
    private ElasticSearchDao elasticSearchDao;

    @Resource
    private EsDeviceService esDeviceService;

    @Resource
    private EsBaseMongoDao esBaseMongoDao;

    @Override
    public void prepareUpdate(String type, String search, List<Map<String, String>> list) {
        QueryBuilder queryBuilder;
        SearchHits searchHits;
        //如果类型是case
        if (ESSearchUtil.CASE_TYPE.equals(type)) {
            //根据案件唯一标识查出对应的一条数据
            queryBuilder = eSSearchUtil.getQueryBuilder(search, Collections.singletonList("caseuniquemark"));
            searchHits = elasticSearchDao.searchCase(ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.CASE_TYPE, queryBuilder, null, null, 0, 1);
            for (SearchHit searchHit : searchHits) {
                //给该条数据加上 caseKeyWords 字段
                Map<String, Object> source = searchHit.getSource();
                source.putIfAbsent("caseKeyWords", list);
                //更新该条数据
                elasticSearchDao.esUpdate(ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.CASE_TYPE, search, source);
            }
        }
        //如果类型是person
        if (ESSearchUtil.PERSON_TYPE.equals(type)) {
            //根据传入的身份证号查询该人员的数据
            queryBuilder = eSSearchUtil.getQueryBuilder(search, Collections.singletonList("usernumber"));
            searchHits = elasticSearchDao.searchCase(ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.PERSON_TYPE, queryBuilder, null, null, 0, 1);
            for (SearchHit searchHit : searchHits) {
                //给该条数据加上 personKeyWords 字段
                Map<String, Object> source = searchHit.getSource();
                source.putIfAbsent("personKeyWords", list);
                //更新该条数据
                elasticSearchDao.esUpdate(ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.PERSON_TYPE, search, source);
            }
        }

    }

    @Override
@Cacheable(value = "wxChatRoomMsg", keyGenerator = "keyGenerator")
    public Object searchWxChatRoomMsg(String search, Integer page, Integer pageSize, String timeFrom,
                                      String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("msgname", "chatroomname", "username");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, false);

        boolean matches = search.matches(REG);
        if (matches) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "1");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);

        SearchHits searchHits = elasticSearchDao.searchWxChatRoomMsg(ESSearchUtil.INDEX_NAME,
                ESSearchUtil.WXCHATROOMMSG_TYPE, queryBuilder, rqb, hb, page, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField msgDataField = highlightFields.get("msgname");
            if (msgDataField != null) {
                String msgname = ESSearchUtil.getHightLightStr(msgDataField);
                result.put("msgname", msgname);
            }
            HighlightField chatRoomNameField = highlightFields.get("chatroomname");
            if (chatRoomNameField != null) {
                String chatrooname = ESSearchUtil.getHightLightStr(chatRoomNameField);
                result.put("chatroomname", chatrooname);
            }
            HighlightField unameField = highlightFields.get("username");
            if (unameField != null) {
                String uname = ESSearchUtil.getHightLightStr(unameField);
                result.put("username", uname);
            }
            results.add(result);
        }
        if(results.isEmpty()){
            searchWxChatRoomMsgByMongo(search, page, pageSize, timeFrom,timeEnd, results);
        }
        List<String> wxNumbers = new ArrayList<>();
        List<String> wxChatUins = new ArrayList<>();
        List<AllQueryBean> list = new ArrayList<>();
        results.forEach((Map<String, Object> object) -> {
            AllQueryBean bean = new AllQueryBean();
            wxChatUins.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("chatroomname").toString()));
            bean.setNickname((String) object.get("chatroomname"));

            bean.setUniseq((String) object.get("uniseq"));
            bean.setMsgTime(TimeUtil.parseStringToDate(object.get("msgtime").toString()));
            bean.setConTent((String) object.get("msgname"));
            wxNumbers.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("username").toString()));
            bean.setSenderName((String) object.get("username"));
            list.add(bean);
        });
        Map<String, String> wxUin2WXNick =
                esDeviceService.findWXNumber2WXNickByWXNums(wxChatUins);
        Map<String, String> wxTroopUin2Nick =
                esDeviceService.findWXChatNumber2WXChatNickByWXChats(wxNumbers);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(t -> {
                String reNick = wxTroopUin2Nick.get(ESSearchUtil.hightLightStrToNormal(t.getNickname()));
                String seNick = wxUin2WXNick.get(ESSearchUtil.hightLightStrToNormal(t.getSenderName()));
                reNick = reNick != null ? reNick : "";
                seNick = seNick != null ? seNick : "";
                t.setName(reNick);
                t.setRecvName(seNick);
            });
        }
        return list;
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countWxChatRoomMsg(String search, String timeFrom, String timeEnd) {
        System.out.println("第一次缓存");
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("msgname", "chatroomname", "username");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "1");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);

        Long aLong = elasticSearchDao.countWxTroopMsg(ESSearchUtil.INDEX_NAME, ESSearchUtil.WXCHATROOMMSG_TYPE, queryBuilder, rqb);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("msgname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("chatroomname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("username", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong =  esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "wxChatroomMsg", query);
        }
        return aLong;
    }

    private void searchWxChatRoomMsgByMongo(String search, Integer page, Integer pageSize, String timeFrom, String timeEnd, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("msgname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("chatroomname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("username", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "wxChatroomMsg", query, null, page, pageSize);
        //封装返回数据"msgname", "chatroomname", "username"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(8);
            result.put("mediapath", document.getString("mediapath"));
            result.put("uniseq", document.getString("uniseq"));
            Date date = (Date) document.get("msgtime");
            result.put("msgtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            result.put("usertype", document.get("usertype"));
            result.put("timeNode", document.get("timeNode"));

            //匹配的字段进行高亮显示
            String chatroomName = document.getString("chatroomname");
            if(chatroomName != null && chatroomName.contains(search)){
                chatroomName = ESSearchUtil.getHightLightStr(chatroomName, chatroomName);
            }
            result.put("chatroomname", chatroomName);
            result.put("msgname", ESSearchUtil.getHightLightStr(search, document.getString("msgname")));
            //匹配的字段进行高亮显示
            String userName = document.getString("username");
            if(userName != null && userName.contains(search)){
                userName = ESSearchUtil.getHightLightStr(userName, userName);
            }
            result.put("username", userName);
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "searchRecord", keyGenerator = "keyGenerator")
    public List<AllQueryBean> searchRecordService(String search, String timeFrom, String timeEnd, Integer page,
                                                  Integer pageSize) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("time", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Collections.singletonList("phonenum");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        SearchHits searchHits = elasticSearchDao.searchRecord(
                ESSearchUtil.INDEX_NAME, ESSearchUtil.RECORD_CALL_TYPE, stringQueryBuilder, rqb, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> hightFields = searchHit.getHighlightFields();
            HighlightField phoneNumField = hightFields.get("phonenum");
            if (phoneNumField != null) {
                String phoneNum = ESSearchUtil.getHightLightStr(phoneNumField);
                result.put("phonenum", phoneNum);
            }
            list.add(result);
        }
        if(list.isEmpty()){
            searchRecordServiceByMongo(search, timeFrom, timeEnd, page, pageSize,list);
        }

        List<AllQueryBean> allList = new ArrayList<>();
        List<String> deviceUniques = new ArrayList<>();
        for (Map<String, Object> aList : list) {
            AllQueryBean bean = new AllQueryBean();
            bean.setMsgTime(TimeUtil.parseStringToDate(aList.get("time").toString()));
            bean.setId(Long.valueOf(aList.get("callstate").toString()));
            bean.setIdxType(Long.valueOf(aList.get("callduration").toString()));
            deviceUniques.add(aList.get("deviceUnique").toString());
            bean.setDeviceUnique(aList.get("deviceUnique").toString());
            bean.setPhone(aList.get("phonenum").toString());
            String contactName = "未知";
            String findContactName = esDeviceService.findContactNameByDeviceUniqueAndPhone(aList.get("deviceUnique").toString()
                    , ESSearchUtil.hightLightStrToNormal(aList.get("phonenum").toString()));
            if (!StringUtils.isEmpty(findContactName)) {
                contactName = findContactName;
            }
            bean.setName(contactName);
            allList.add(bean);
        }
        //获取人员以及人员电话号码
        Map<String, String[]> unique2nameAndPhone = esDeviceService.findDeviceUnique2PersonNameAndPhone(
                deviceUniques);
        if (!CollectionUtils.isEmpty(allList)) {
            allList.forEach(t -> {
                String unique = t.getDeviceUnique();
                String[] nameAndPhone = unique2nameAndPhone.get(unique);
                if (nameAndPhone != null && nameAndPhone.length > 0) {
                    t.setNickname(nameAndPhone[0]);
                    t.setTelePhone(nameAndPhone[1]);
                }
            });
        }
        System.out.println("没有使用缓存" + new Random().nextInt(10));
        return allList;
    }

    private void searchRecordServiceByMongo(String search, String timeFrom, String timeEnd, Integer page, Integer pageSize, List<Map<String, Object>> list) {
        //组装搜索条件
        BasicDBObject query = new BasicDBObject("phonenum", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "record", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            Map<String, Object> map = new HashMap<>(8);
            map.put("callstate", document.get("callstate"));
            map.put("deviceUnique", document.get("deviceUnique"));
            map.put("uniseq", document.get("uniseq"));
            String phoneNum = document.getString("phonenum");
            //只按一个添加查询 这个字段肯定存在不能为null
            map.put("phonenum", ESSearchUtil.getHightLightStr(phoneNum,phoneNum));
            map.put("callduration", document.get("callduration"));
            Date date = (Date) document.get("time");
            map.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            map.put("timeNode", document.get("timeNode"));
            list.add(map);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countRecord(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("time", timeFrom, timeEnd);
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(),
                Arrays.asList("phonenum"));
        Long aLong = elasticSearchDao.countRecordCall(ESSearchUtil.INDEX_NAME, ESSearchUtil.RECORD_CALL_TYPE, stringQueryBuilder, rqb);
        if(aLong == null || aLong == 0){
            BasicDBObject query = new BasicDBObject("phonenum", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "record", query);
        }
        return aLong;
    }

    @Override
@Cacheable(value = "searchQqTroopMsgService", keyGenerator = "keyGenerator")
    public Object searchQqTroopMsgService(String search, String timeFrom, String timeEnd, Integer page,
                                          Integer pageSize) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("senderuin", "msgdata", "troopuin");
        QueryBuilder stringBuilder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "-1000");
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, false);

        boolean matches = search.matches(REG);
        if (matches) {
            stringBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringBuilder).must(typeQuery);
        SearchHits searchHits = elasticSearchDao.searchQqTroopMsg(
                ESSearchUtil.INDEX_NAME, ESSearchUtil.QQTROOPMSG_TYPE, queryBuilder, rqb, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> hightFields = searchHit.getHighlightFields();
            HighlightField reviverUinField = hightFields.get("troopuin");
            if (reviverUinField != null) {
                String receiverUin = ESSearchUtil.getHightLightStr(reviverUinField);
                result.put("troopuin", receiverUin);
            }
            HighlightField senderUinField = hightFields.get("senderuin");
            if (senderUinField != null) {
                String senderUin = ESSearchUtil.getHightLightStr(senderUinField);
                result.put("senderuin", senderUin);
            }
            HighlightField msgDataField = hightFields.get("msgdata");
            if (msgDataField != null) {
                String msgData = ESSearchUtil.getHightLightStr(msgDataField);
                result.put("msgdata", msgData);
            }
            results.add(result);
        }

        if(results.isEmpty()){
            searchQqTroopMsgServiceByMongo(search, timeFrom, timeEnd, page,pageSize,results);
        }
        List<String> qqTroopUins = new ArrayList<>();
        List<String> qqNumbers = new ArrayList<>();
        List<AllQueryBean> list = new ArrayList<>();
        results.forEach((Map<String, Object> object) -> {
            AllQueryBean bean = new AllQueryBean();
            qqTroopUins.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("troopuin").toString()));
            bean.setNickname((String) object.get("troopuin"));
///			QqTroopBean qqTroopBean = session.selectOne("qqtroop.findByQqTroopName"
            bean.setUniseq((String) object.get("uniseq"));
            bean.setMsgTime(TimeUtil.parseStringToDate(object.get("msgtime").toString()));
            bean.setConTent((String) object.get("msgdata"));
            //			QqUserBean senderBean = session.selectOne("qquser.findByUin"
            qqNumbers.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("senderuin").toString()));
            bean.setSenderName((String) object.get("senderuin"));
            list.add(bean);
        });
        Map<String, String> qqUin2QQNick =
                esDeviceService.findQQNumber2QQNickByQQNums(qqTroopUins);
        Map<String, String> qqTroopUin2Nick =
                esDeviceService.findQQTroopNumber2QQTroopNumberByQQTroops(qqNumbers);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(t -> {
                String reUin = ESSearchUtil.hightLightStrToNormal(t.getNickname());
                String seUin = ESSearchUtil.hightLightStrToNormal(t.getSenderName());
                String reNick = qqTroopUin2Nick.get(reUin);
                String seNick = qqUin2QQNick.get(seUin);
                reNick = reNick != null ? reNick : "";
                seNick = seNick != null ? seNick : "";
                t.setName(reNick);
                t.setRecvName(seNick);
            });
        }
        return list;
    }

    private void searchQqTroopMsgServiceByMongo(String search, String timeFrom, String timeEnd, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("senderuin", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("troopuin", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "qqTroopMsg", query, null, page, pageSize);
        //封装返回数据"senderuin", "msgdata", "troopuin"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(8);
            result.put("uniseq", document.getString("uniseq"));
            result.put("msgtype", document.getString("msgtype"));
            result.put("mexjsonobject", document.get("mexjsonobject"));
            result.put("msgseq", document.get("msgseq"));
            Date date = (Date) document.get("msgtime");
            result.put("msgtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            result.put("timeNode", document.get("timeNode"));
            result.put("isdelete", document.get("isdelete"));

            //匹配的字段进行高亮显示
            String senderUin = document.getString("senderuin");
            if(senderUin != null && senderUin.contains(search)){
                senderUin = ESSearchUtil.getHightLightStr(senderUin, senderUin);
            }
            result.put("senderuin", senderUin);

            result.put("msgdata", ESSearchUtil.getHightLightStr(search, document.getString("msgdata")));

            //匹配的字段进行高亮显示
            String troopUin = document.getString("troopuin");
            if(troopUin != null && troopUin.contains(search)){
                troopUin = ESSearchUtil.getHightLightStr(troopUin, troopUin);
            }
            result.put("troopuin", troopUin);
            results.add(result);
        }
    }

    /**
     * 通过查询的开始时间以及结束时间,以及查询时间范围字段名获取一键搜的查询时间
     *
     * @param timeQueryFieldName 查询的时间类型
     * @param timeFrom           查询的开始时间
     * @param timeEnd            查询的结束时间
     * @return RangeQueryBuilder 查询结果
     */
    private RangeQueryBuilder getESTimeQueryFiled(String timeQueryFieldName, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = null;
        //若开始时间不为NULL
        if (!StringUtils.isEmpty(timeFrom)) {
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            rqb = QueryBuilders.rangeQuery(timeQueryFieldName).gte(startTime);
        }
        if (!StringUtils.isEmpty(timeEnd)) {
            Long endTime = TimeUtil.parseDateAndAddOneDayFromStr(timeEnd);
            if (rqb == null) {
                rqb = QueryBuilders.rangeQuery(timeQueryFieldName).lt(endTime);
            } else {
                rqb.lt(endTime);
            }
        }
        return rqb;
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countQqTroopMsg(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("senderuin", "msgdata", "troopuin");
        QueryBuilder stringBuilder = eSSearchUtil.getQueryBuilder(search.trim(),
                filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            stringBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(),
                    filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "-1000");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringBuilder).must(typeQuery);
        Long aLong = elasticSearchDao.countQqTroopMsg(ESSearchUtil.INDEX_NAME, ESSearchUtil.QQTROOPMSG_TYPE, queryBuilder, rqb);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("senderuin", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("troopuin", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "qqTroopMsg", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "searchQqMsgService", keyGenerator = "keyGenerator")
    public Object searchQqMsgService(String search, String timeFrom, String timeEnd, Integer page,
                                     Integer pageSize) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应查询条件,符合对应的消息类型
        final List<String> filedNames = Arrays.asList("senderuin", "msgdata", "receiveruin");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search,
                filedNames);
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "-1000");

        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, false);
        boolean matches = search.matches(REG);
        if (matches) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(),
                    filedNames);
        }
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);
        SearchHits searchHits = elasticSearchDao.searchQqMsg(
                ESSearchUtil.INDEX_NAME, ESSearchUtil.QQMSG_TYPE, queryBuilder, rqb, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        //遍历结果，对其进行处理
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField receiverUinHighlightField = highlightFields.get("receiveruin");
            if (receiverUinHighlightField != null) {
                String reciverUin = ESSearchUtil.getHightLightStr(receiverUinHighlightField);
                result.put("receiveruin", reciverUin);
            }
            HighlightField senderUinField = highlightFields.get("senderuin");
            if (senderUinField != null) {
                String senderUin = ESSearchUtil.getHightLightStr(senderUinField);
                result.put("senderuin", senderUin);
            }
            HighlightField msgDataField = highlightFields.get("msgdata");
            if (msgDataField != null) {
                String msgData = ESSearchUtil.getHightLightStr(msgDataField);
                result.put("msgdata", msgData);
            }
            results.add(result);
        }
        if(results.isEmpty()){
            searchQqMsgServiceByMongo(search, timeFrom, timeEnd, page, pageSize, results);
        }
        List<AllQueryBean> list = new ArrayList<>();
        List<String> qqNumbers = new ArrayList<>();
        results.forEach((Map<String, Object> object) -> {
            AllQueryBean bean = new AllQueryBean();
            qqNumbers.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("receiveruin").toString()));
            bean.setRecvName((String) object.get("receiveruin"));
            qqNumbers.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("senderuin").toString()));
            bean.setSenderName((String) object.get("senderuin"));
            bean.setConTent((String) object.get("msgdata"));
            bean.setUniseq((String) object.get("uniseq"));
            bean.setMsgTime(TimeUtil.parseStringToDate(object.get("msgtime").toString()));
            bean.setDeviceUnique((String) object.get("c2cmsg_mark"));
            list.add(bean);
        });
        Map<String, String> qqNumber2Nick =
                esDeviceService.findQQNumber2QQNickByQQNums(qqNumbers);
        addLists(list, qqNumber2Nick);
        return list;
    }

    private void searchQqMsgServiceByMongo(String search, String timeFrom, String timeEnd, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("senderuin", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("receiveruin", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "qqmsg", query, null, page, pageSize);
        //封装返回数据"senderuin", "msgdata", "receiveruin"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(16);
            result.put("issend", document.get("issend"));
            result.put("uniseq", document.get("uniseq"));
            result.put("mexjsonobject", document.get("mexjsonobject"));
            result.put("msgtype", document.get("msgtype"));
            result.put("c2cmsg_mark", document.get("c2cmsg_mark"));
            result.put("shmsgseq", document.get("shmsgseq"));
            result.put("msguid", document.get("msguid"));
            result.put("msgseq", document.get("msgseq"));
            result.put("time", document.get("time"));
            Date date = (Date) document.get("msgtime");
            result.put("msgtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            result.put("isdelete", document.get("isdelete"));
            result.put("timeNode", document.get("timeNode"));

            //匹配的字段进行高亮显示
            String senderUin = document.getString("senderuin");
            if(senderUin != null && senderUin.contains(search)){
                senderUin = ESSearchUtil.getHightLightStr(senderUin, senderUin);
            }
            result.put("senderuin", senderUin);

            result.put("msgdata", ESSearchUtil.getHightLightStr(search, document.getString("msgdata")));

            //匹配的字段进行高亮显示
            String receiverUin = document.getString("receiveruin");
            if(receiverUin != null && receiverUin.contains(search)){
                receiverUin = ESSearchUtil.getHightLightStr(receiverUin, receiverUin);
            }
            result.put("receiveruin", receiverUin);
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countQqMsg(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("senderuin", "msgdata", "receiveruin");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search,
                filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(),
                    filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "-1000");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);
        Long aLong = elasticSearchDao.countQqMsg(ESSearchUtil.INDEX_NAME, ESSearchUtil.QQMSG_TYPE, queryBuilder, rqb);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("senderuin", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("receiveruin", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "qqmsg", query);
        }
        return aLong;
    }

    @Override
@Cacheable(value = "searchWxMsgService", keyGenerator = "keyGenerator")
    public Object searchWxMsgService(String search, Integer page, Integer pageSize, String timeFrom,
                                     String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("receivername", "msgdata", "sendername");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, false);

        boolean matches = search.matches(REG);
        if (matches) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "1");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);
        SearchHits searchHits = elasticSearchDao.searchWxMsg(ESSearchUtil.INDEX_NAME,
                ESSearchUtil.WXMSG_TYPE, queryBuilder, rqb, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField receiverNameField = highlightFields.get("receivername");
            if (receiverNameField != null) {
                String reciverName = ESSearchUtil.getHightLightStr(receiverNameField);
                result.put("receivername", reciverName);
            }
            HighlightField msgDataField = highlightFields.get("msgdata");
            if (msgDataField != null) {
                String msgData = ESSearchUtil.getHightLightStr(msgDataField);
                result.put("msgdata", msgData);
            }
            HighlightField senderNameField = highlightFields.get("sendername");
            if (senderNameField != null) {
                String senderName = ESSearchUtil.getHightLightStr(senderNameField);
                result.put("sendername", senderName);
            }
            results.add(result);
        }

        if(results.isEmpty()){
            searchWxMsgServiceByMongo(search, page, pageSize, timeFrom,timeEnd, results);
        }
        List<String> wxUins = new ArrayList<>();
        List<AllQueryBean> list = new ArrayList<>();
        results.forEach((Map<String, Object> object) -> {
            AllQueryBean bean = new AllQueryBean();
            wxUins.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("receivername").toString()));
            bean.setRecvName((String) object.get("receivername"));
            wxUins.add(
                    ESSearchUtil.hightLightStrToNormal(object.get("sendername").toString()));
            bean.setSenderName((String) object.get("sendername"));
            bean.setConTent((String) object.get("msgdata"));
            bean.setUniseq((String) object.get("uniseq"));
            bean.setMsgTime(TimeUtil.parseStringToDate(object.get("msgtime").toString()));
            bean.setDeviceUnique((String) object.get("c2cmark"));
            list.add(bean);
        });
        Map<String, String> wxNumber2Nick =
                esDeviceService.findWXNumber2WXNickByWXNums(wxUins);
        addLists(list, wxNumber2Nick);
        return list;
    }

    private void searchWxMsgServiceByMongo(String search, Integer page, Integer pageSize, String timeFrom, String timeEnd, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("receivername", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("sendername", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "wxmsg", query, null, page, pageSize);
        //封装返回数据"receivername", "msgdata", "sendername"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(16);
            result.put("c2cmark", document.get("c2cmark"));
            result.put("mediapath", document.get("mediapath"));
            result.put("uniseq", document.get("uniseq"));
            result.put("msgtype", document.get("msgtype"));
            Date date = (Date) document.get("msgtime");
            result.put("msgtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            result.put("timeNode", document.get("timeNode"));

            //匹配的字段进行高亮显示
            String receiverName = document.getString("receivername");
            if(receiverName != null && receiverName.contains(search)){
                receiverName = ESSearchUtil.getHightLightStr(receiverName, receiverName);
            }
            result.put("receivername", receiverName);

            result.put("msgdata",  ESSearchUtil.getHightLightStr(search, document.getString("msgdata")));

            //匹配的字段进行高亮显示
            String senderName = document.getString("sendername");
            if(senderName != null && senderName.contains(search)){
                senderName = ESSearchUtil.getHightLightStr(senderName, senderName);
            }
            result.put("sendername", senderName);
            results.add(result);
        }
    }

    private void addLists(List<AllQueryBean> list, Map<String, String> wxNumber2Nick) {
        list.forEach(t -> {
            String reUin = ESSearchUtil.hightLightStrToNormal(t.getRecvName());
            String seUin = ESSearchUtil.hightLightStrToNormal(t.getSenderName());
            String reNick = wxNumber2Nick.get(reUin);
            String seNick = wxNumber2Nick.get(seUin);
            reNick = reNick != null ? reNick : "";
            seNick = seNick != null ? seNick : "";
            t.setNickname(reNick);
            t.setName(seNick);
        });
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countWxMsg(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("msgtime", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Arrays.asList("receivername", "msgdata", "sendername");
        QueryBuilder stringQueryBuilder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            stringQueryBuilder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        TermQueryBuilder typeQuery = QueryBuilders.termQuery("msgtype", "1");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(stringQueryBuilder).must(typeQuery);
        Long aLong = elasticSearchDao.countWxMsg(ESSearchUtil.INDEX_NAME, ESSearchUtil.WXMSG_TYPE, queryBuilder, rqb);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("receivername", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("msgdata", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("sendername", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("msgtime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "wxmsg", query);
        }
        return aLong;
    }

    @Override
@Cacheable(value = "searchMsgService", keyGenerator = "keyGenerator")
    public List<AllQueryBean> searchMsgService(String search, Integer page, Integer pageSize, String timeFrom,
                                               String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("time", timeFrom, timeEnd);
        //拼接对应的消息类型查询条件
        final List<String> filedNames = Collections.singletonList("content");
        QueryBuilder stringBuilder = eSSearchUtil.getQueryBuilder(search, filedNames);
        if (search.matches(REG)) {
            stringBuilder = eSSearchUtil.getQueryBuilderMath(search, filedNames);
        }
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        SearchHits searchHits = elasticSearchDao.searchMsg(ESSearchUtil.INDEX_NAME,
                ESSearchUtil.MESSAGE_TYPE, stringBuilder, rqb, hb, (page - 1) * pageSize, pageSize);
        /* handle hightlight */
        //组装为前台需要格式
        List<AllQueryBean> allList = new ArrayList<>();
        List<String> deviceUniques = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            AllQueryBean bean = new AllQueryBean();
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField contentField = highlightFields.get("content");
            if (contentField != null) {
                bean.setConTent(ESSearchUtil.getHightLightStr(contentField));
            }
            bean.setMsgTime(TimeUtil.parseStringToDate(result.get("time").toString()));
            bean.setPhone(result.get("phonenum").toString());
            bean.setUniseq(result.get("uniseq").toString());
            bean.setId(Long.valueOf(result.get("getType").toString()));
            bean.setDeviceUnique(result.get("deviceUnique").toString());
            deviceUniques.add(result.get("deviceUnique").toString());
            String contactName = "未知";
            String findContactName = esDeviceService.findContactNameByDeviceUniqueAndPhone(result.get("deviceUnique").toString()
                    , result.get("phonenum").toString());
            if (!StringUtils.isEmpty(findContactName)) {
                contactName = findContactName;
            }
            bean.setName(contactName);
            allList.add(bean);

        }
        if(allList.isEmpty()){
            searchMsgServiceByMongo(search, page, pageSize, timeFrom, timeEnd,allList ,deviceUniques);
        }
        //获取人员以及人员电话号码
        Map<String, String[]> unique2nameAndPhone = esDeviceService.findDeviceUnique2PersonNameAndPhone(
                deviceUniques);
        allList.forEach(t -> {
            String unique = t.getDeviceUnique();
            String[] nameAndPhone = unique2nameAndPhone.get(unique);
            if (nameAndPhone != null && nameAndPhone.length > 1) {
                t.setNickname(nameAndPhone[0]);
                t.setTelePhone(nameAndPhone[1]);
            }
        });
        return allList;
    }

    private void searchMsgServiceByMongo(String search, Integer page, Integer pageSize, String timeFrom, String timeEnd,
                                         List<AllQueryBean> allList, List<String> deviceUniques) {
        //组装搜索条件
        BasicDBObject query = new BasicDBObject("content", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData", "message", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            AllQueryBean bean = new AllQueryBean();
            String contentField = document.getString("content");
            bean.setConTent(ESSearchUtil.getHightLightStr(search, contentField));
            bean.setMsgTime(((Date)document.get("time")).getTime());
            bean.setPhone(document.get("phonenum").toString());
            bean.setUniseq(document.get("uniseq").toString());
            bean.setId(Long.valueOf(document.get("getType").toString()));
            bean.setDeviceUnique(document.get("deviceUnique").toString());
            deviceUniques.add(document.get("deviceUnique").toString());
            String contactName = "未知";
            String findContactName = esDeviceService.findContactNameByDeviceUniqueAndPhone(document.get("deviceUnique").toString()
                    , document.get("phonenum").toString());
            if (!StringUtils.isEmpty(findContactName)) {
                contactName = findContactName;
            }
            bean.setName(contactName);
            allList.add(bean);
        }
    }

    /**
     * 统计短消息的条数
     *
     * @param search   搜索对应的条件
     * @param timeFrom 搜索开始的时间
     * @param timeEnd  搜索结束的时间
     * @return 返回短消息条数
     */
    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countMsg(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("time", timeFrom, timeEnd);
        final List<String> content = Collections.singletonList("content");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), content);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), content);
        }
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(builder);
        Long aLong = elasticSearchDao.countMsg(ESSearchUtil.INDEX_NAME, ESSearchUtil.MESSAGE_TYPE, queryBuilder, rqb);
        if(aLong == null || aLong == 0){
            BasicDBObject query = new BasicDBObject("content", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("time", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData", "message", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findPerson", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findPerson(String search, Integer page, Integer pageSize) {
        final List<String> filedNames = Arrays.asList("usernumber", "hometown", "personname", "phone");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchPerson(
                ESSearchUtil.PERSON_INDEX_NAME, ESSearchUtil.PERSON_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> list = new ArrayList<>();
        //封装er的查询结果
        findPersonAnalysisResult(searchHits, list);
        //如果es中没查到就去mongo中查询一下
        if(list.isEmpty()){
            findPersonByMongo(search, page, pageSize,list);
        }
        return list;
    }

    private void findPersonByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> list) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("hometown", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("personname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("phone", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("usernumber", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_person", query, null, page, pageSize);
        //封装返回数据
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(12);
            result.put("elsecallpeople", document.getString("elsecallpeople"));
            result.put("sex", document.getString("sex"));
            result.put("reside", document.get("reside"));
            result.put("usertype", document.get("usertype"));
            result.put("elsecallpeoplenum", document.get("elsecallpeoplenum"));
            result.put("personKeyWords", document.get("personKeyWords"));
            result.put("caseuniquemark", document.get("caseuniquemark"));
            result.put("device_unique", document.get("device_unique"));

            //匹配的字段进行高亮显示
            String userNumber = document.getString("usernumber");
            if(userNumber != null && userNumber.contains(search)){
                userNumber = ESSearchUtil.getHightLightStr(userNumber, userNumber);
            }
            result.put("usernumber", userNumber);

            result.put("personname", ESSearchUtil.getHightLightStr(search, document.getString("personname")));

            result.put("hometown", ESSearchUtil.getHightLightStr(search, document.getString("hometown")));
            List<String> phoneFields = ((List<String>) document.get("phone"));
            result.put("phone", phoneFields);
            for (String s : phoneFields) {
                if(s.contains(search)){
                    result.put("phone", Collections.singletonList(ESSearchUtil.getHightLightStr(s, s)));
                    break;
                }
            }
            list.add(result);
        }
    }

    private void findPersonAnalysisResult(SearchHits searchHits, List<Map<String, Object>> list) {
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField contentField = highlightFields.get("usernumber");
            if (contentField != null) {
                String content = ESSearchUtil.getHightLightStr(contentField);
                result.put("usernumber", content);
            }
            HighlightField hometownField = highlightFields.get("hometown");
            if (hometownField != null) {
                String content = ESSearchUtil.getHightLightStr(hometownField);
                result.put("hometown", content);
            }
            HighlightField personnameField = highlightFields.get("personname");
            if (personnameField != null) {
                String content = ESSearchUtil.getHightLightStr(personnameField);
                result.put("personname", content);
            }
            HighlightField phoneField = highlightFields.get("phone");
            if (phoneField != null) {
                String content = ESSearchUtil.getHightLightStr(phoneField);
                result.put("phone", content);
            }
            list.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countPerson(String search) {
        final List<String> filedNames = Arrays.asList("usernumber", "hometown", "personname", "phone");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countPerson(ESSearchUtil.PERSON_INDEX_NAME, ESSearchUtil.PERSON_TYPE, builder, null);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("hometown", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("personname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("phone", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("usernumber", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_person", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "queryCase", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findCase(String search, String timeFrom, String timeEnd, Integer page,
                                              Integer pageSize) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("casetime", timeFrom, timeEnd);
        //查询条件
        final List<String> filedNames = Arrays.asList("casefrom", "casetype", "casediscripte", "usernumber", "casename");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        //高亮显示条件
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchCase(
                ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.CASE_TYPE, builder, rqb, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        //解析es中查询出来的数据
        findCaseAnalysisResult(searchHits, results);
        //如果没有就去mongodb中查询然后放入es中
        if(results.isEmpty()){
            findCaseByMongodb(search, timeFrom, timeEnd, page,pageSize,results);
        }
        return results;
    }

    private void findCaseByMongodb(String search, String timeFrom, String timeEnd, Integer page, Integer pageSize,
                                   List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("casefrom", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("casetype", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("casediscripte", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("casename", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("usernumber", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
            Long startTime = TimeUtil.parseDateFromStr(timeFrom);
            Long endTime = TimeUtil.parseDateFromStr(timeEnd);
            query.append("casetime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
        }
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_case", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            result.put("caseuniquemark", document.getString("caseuniquemark"));
            result.put("casenumb", document.getString("casenumb"));
            result.put("caseKeyWords", document.get("caseKeyWords"));
            result.put("device_unique", document.get("device_unique"));

            result.put("casefrom",  ESSearchUtil.getHightLightStr(search, document.getString("casefrom")));

            result.put("casetype",ESSearchUtil.getHightLightStr(search, document.getString("casetype")));

            result.put("casediscripte", ESSearchUtil.getHightLightStr(search, document.getString("casediscripte")));

            List<String> usernumberFields = ((List<String>) document.get("usernumber"));
            result.put("usernumber", usernumberFields);
            for (String s : usernumberFields) {
                if(s.contains(search)){
                    result.put("usernumber", Collections.singletonList(ESSearchUtil.getHightLightStr(s, s)));
                    break;
                }
            }

            result.put("casename", ESSearchUtil.getHightLightStr(search, document.getString("casename")));
            result.put("casetime", document.get("casetime"));
            results.add(result);
        }
    }

    private void findCaseAnalysisResult(SearchHits searchHits, List<Map<String, Object>> results) {
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField casefromField = highlightFields.get("casefrom");
            if (casefromField != null) {
                String casefrom = ESSearchUtil.getHightLightStr(casefromField);
                result.put("casefrom", casefrom);
            }
            HighlightField casetypeField = highlightFields.get("casetype");
            if (casetypeField != null) {
                String casetype = ESSearchUtil.getHightLightStr(casetypeField);
                result.put("casetype", casetype);
            }
            HighlightField casediscripteField = highlightFields.get("casediscripte");
            if (casediscripteField != null) {
                String casediscripte = ESSearchUtil.getHightLightStr(casediscripteField);
                result.put("casediscripte", Collections.singletonList(casediscripte));
            }
            HighlightField usernumberField = highlightFields.get("usernumber");
            if (usernumberField != null) {
                String usernumber = ESSearchUtil.getHightLightStr(usernumberField);
                result.put("usernumber", Collections.singletonList(usernumber));
            }
            HighlightField casenameField = highlightFields.get("casename");
            if (casenameField != null) {
                String casename = ESSearchUtil.getHightLightStr(casenameField);
                result.put("casename", casename);
            }
            results.add(result);
        }
        results.forEach(t -> {
            Object caseObj = t.get("casetime");
            if (caseObj != null) {
                Long time = TimeUtil.parseStringToDate(caseObj.toString());
                t.put("casetime", time);
            }
        });
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countCase(String search, String timeFrom, String timeEnd) {
        RangeQueryBuilder rqb = this.getESTimeQueryFiled("casetime", timeFrom, timeEnd);
        final List<String> filedNames = Arrays.asList("casefrom", "casetype", "casediscripte", "usernumber", "casename");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countCase(ESSearchUtil.CASE_INDEX_NAME, ESSearchUtil.CASE_TYPE, builder, rqb);
        if(aLong == null || aLong == 0){
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("casefrom", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("casetype", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("casediscripte", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("casename", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("usernumber", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            if(!StringUtils.isEmpty(timeFrom) && !StringUtils.isEmpty(timeEnd)){
                Long startTime = TimeUtil.parseDateFromStr(timeFrom);
                Long endTime = TimeUtil.parseDateFromStr(timeEnd);
                query.append("casetime", new BasicDBObject("$lt", endTime).append("$gte", startTime));
            }
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_case", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findDevice", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findDevice(String search, Integer page, Integer pageSize) {
        final List<String> filedNames = Collections.singletonList("devicename");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        SearchHits searchHits = elasticSearchDao.searchDevice(
                ESSearchUtil.DEVICE_INDEX_NAME, ESSearchUtil.DEVICE_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField nameFiled = highlightFields.get("devicename");
            if (nameFiled != null) {
                String deviceName = ESSearchUtil.getHightLightStr(nameFiled);
                result.put("devicename", deviceName);
            }
            results.add(result);
        }
        if(results.isEmpty()){
            findDeviceByMongo(search, page, pageSize,results);
        }
        return results;
    }

    private void findDeviceByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBObject query = new BasicDBObject("devicename", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_device", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(12);
            result.put("device_unique", document.get("device_unique"));
            result.put("bt_mac", document.get("bt_mac"));
            result.put("imei", document.get("imei"));
            result.put("wifi_mac", document.get("wifi_mac"));
            result.put("type", document.get("type"));
            result.put("androidver", document.get("androidver"));
            result.put("imie_2", document.get("imie_2"));
            result.put("model", document.get("model"));
            result.put("brand", document.get("brand"));
            result.put("usernumber", document.get("usernumber"));
            result.put("caseuniquemark", document.get("caseuniquemark"));

            result.put("devicename", ESSearchUtil.getHightLightStr(search, document.getString("devicename")));
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countDevice(String search) {

        final List<String> filedNames = Collections.singletonList("devicename");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countDevice(ESSearchUtil.DEVICE_INDEX_NAME, ESSearchUtil.DEVICE_TYPE, builder, null);
        if(aLong == null || aLong == 0 ){
            //组装搜索条件
            BasicDBObject query = new BasicDBObject("devicename", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE));
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_device", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findQQUser", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findQQUser(String search, Integer page, Integer pageSize) {
        //搜索条件
        final List<String> filedNames = Arrays.asList("nickname", "uin");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchQQUser(
                ESSearchUtil.QQUSER_INDEX_NAME, ESSearchUtil.QQUSER_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField nicknameFiled = highlightFields.get("nickname");
            if (nicknameFiled != null) {
                String nickname = ESSearchUtil.getHightLightStr(nicknameFiled);
                result.put("nickname", nickname);
            }
            HighlightField uinFiled = highlightFields.get("uin");
            if (uinFiled != null) {
                String uin = ESSearchUtil.getHightLightStr(uinFiled);
                result.put("uin", uin);
            }
            results.add(result);
        }

        if(results.isEmpty()){
            findQQUserByMongo(search, page, pageSize, results);
        }
        return results;
    }

    private void findQQUserByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("nickname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("uin", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_qquser", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        //"nickname", "uin"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            result.put("sex", document.getString("sex"));
            result.put("homeplace", document.getString("homeplace"));
            result.put("richbuffer", document.get("richbuffer"));
            result.put("alias", document.get("alias"));
            result.put("e-mail", document.get("e-mail"));
            result.put("age", document.get("age"));
            result.put("ispublicaccount", document.get("ispublicaccount"));
            result.put("qqfriend", document.get("qqfriend"));
            result.put("device_unique", document.get("device_unique"));

            result.put("nickname", ESSearchUtil.getHightLightStr(search, document.getString("nickname")));
            String uin = document.getString("uin");
            if(uin != null && uin.contains(search)){
                uin = ESSearchUtil.getHightLightStr(uin, uin);
            }
            result.put("uin", uin);
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countQQUser(String search) {
        //搜索条件
        final List<String> filedNames = Arrays.asList("nickname", "uin");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countQQUser(ESSearchUtil.QQUSER_INDEX_NAME, ESSearchUtil.QQUSER_TYPE, builder, null);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("nickname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("uin", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_qquser", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findQQTroop", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findQQTroop(String search, Integer page, Integer pageSize) {
        final List<String> filedNames = Arrays.asList("troopuin", "troopname", "fingertroopmemo", "troopmemo", "trooplocation");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchQQTroop(
                ESSearchUtil.QQTROOP_INDEX_NAME, ESSearchUtil.QQTROOP_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        findQQTroopAnalysisResult(searchHits, results);
        if(results.isEmpty()){
            findQQTroopByMongo(search, page, pageSize,results);
        }
        return results;
    }

    private void findQQTroopByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("troopuin", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("troopname", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_qq_troop",
                query, null, page, pageSize);
        //匹配的字段进行高亮显示
        //"troopuin", "troopname", "fingertroopmemo", "troopmemo", "trooplocation"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            result.put("troopowner", document.get("troopowner"));
            result.put("trooplocation", document.get("trooplocation"));
            result.put("richfingermemo", document.get("richfingermemo"));
            result.put("fingertroopmemo", document.get("fingertroopmemo"));
            result.put("troopmemo", document.get("troopmemo"));
            result.put("_lastUpdate", document.get("_lastUpdate"));
            result.put("qqmember", document.get("qqmember"));

            result.put("troopname", ESSearchUtil.getHightLightStr(search, document.getString("troopname")));

            String troopUin = document.getString("troopuin");
            if(troopUin != null && troopUin.contains(search)){
                troopUin = ESSearchUtil.getHightLightStr(troopUin, troopUin);
            }
            result.put("troopuin", troopUin);

            results.add(result);
        }
    }

    private void findQQTroopAnalysisResult(SearchHits searchHits, List<Map<String, Object>> results) {
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField troopuinFiled = highlightFields.get("troopuin");
            if (troopuinFiled != null) {
                String troopuin = ESSearchUtil.getHightLightStr(troopuinFiled);
                result.put("troopuin", troopuin);
            }
            HighlightField troopnameFiled = highlightFields.get("troopname");
            if (troopnameFiled != null) {
                String troopname = ESSearchUtil.getHightLightStr(troopnameFiled);
                result.put("troopname", troopname);
            }
            HighlightField fingertroopmemoFiled = highlightFields.get("fingertroopmemo");
            if (fingertroopmemoFiled != null) {
                String fingertroopmemo = ESSearchUtil.getHightLightStr(fingertroopmemoFiled);
                result.put("fingertroopmemo", fingertroopmemo);
            }
            HighlightField troopmemoFiled = highlightFields.get("troopmemo");
            if (troopmemoFiled != null) {
                String troopmemo = ESSearchUtil.getHightLightStr(troopmemoFiled);
                result.put("troopmemo", troopmemo);
            }

            HighlightField trooplocationFiled = highlightFields.get("trooplocation");
            if (trooplocationFiled != null) {
                String trooplocation = ESSearchUtil.getHightLightStr(trooplocationFiled);
                result.put("trooplocation", trooplocation);
            }
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countQQTroop(String search) {
        final List<String> filedNames = Arrays.asList("troopuin", "troopname", "fingertroopmemo", "troopmemo", "trooplocation");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countQQTroop(ESSearchUtil.QQTROOP_INDEX_NAME, ESSearchUtil.QQTROOP_TYPE, builder, null);
        //如果er中没有数据就先从mongo中进行查询
        if(aLong == null || aLong == 0){
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("troopuin", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("troopname", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_qq_troop",
                    query);
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findWXUser", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findWXUser(String search, Integer page, Integer pageSize) {
        //搜索条件
        final List<String> filedNames = Arrays.asList("username", "nickname", "sign", "loc1", "loc2");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchWXUser(
                ESSearchUtil.WXUSER_INDEX_NAME, ESSearchUtil.WXUSER_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        findWXUserAnalysisResult(searchHits, results);
        if(results.isEmpty()){
            findWXUserByMongo(search, page, pageSize,results);
        }
        return results;
    }

    private void findWXUserByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("username", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("nickname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("sign", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("loc1", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("loc2", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_wxuser", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        //"username", "nickname", "sign", "loc1", "loc2"
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            result.put("sex", document.getString("sex"));
            result.put("bindphone", document.getString("bindphone"));
            result.put("bindqq", document.get("bindqq"));
            result.put("extends", document.get("extends"));
            result.put("tweibo", document.get("tweibo"));
            result.put("email", document.get("email"));
            result.put("type", document.get("type"));
            result.put("verifyFlag", document.get("verifyFlag"));
            result.put("weiboFlag", document.get("weiboFlag"));
            result.put("alias", document.get("alias"));
            result.put("wxfriend", document.get("wxfriend"));
            result.put("device_unique", document.get("device_unique"));

            result.put("nickname", ESSearchUtil.getHightLightStr(search, document.getString("nickname")));
            result.put("username", ESSearchUtil.getHightLightStr(search, document.getString("username")));
            result.put("sign", ESSearchUtil.getHightLightStr(search, document.getString("sign")));
            result.put("loc1", ESSearchUtil.getHightLightStr(search, document.getString("loc1")));
            result.put("loc2", ESSearchUtil.getHightLightStr(search, document.getString("loc2")));
            results.add(result);
        }
    }

    private void findWXUserAnalysisResult(SearchHits searchHits, List<Map<String, Object>> results) {
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField usernameFiled = highlightFields.get("username");
            if (usernameFiled != null) {
                String username = ESSearchUtil.getHightLightStr(usernameFiled);
                result.put("username", username);
            }
            HighlightField nicknameFiled = highlightFields.get("nickname");
            if (nicknameFiled != null) {
                String nickname = ESSearchUtil.getHightLightStr(nicknameFiled);
                result.put("nickname", nickname);
            }

            HighlightField signFiled = highlightFields.get("sign");
            if (signFiled != null) {
                String sign = ESSearchUtil.getHightLightStr(signFiled);
                result.put("sign", sign);
            }
            HighlightField loc1Filed = highlightFields.get("loc1");
            if (loc1Filed != null) {
                String loc1 = ESSearchUtil.getHightLightStr(loc1Filed);
                result.put("loc1", loc1);
            }

            HighlightField loc2Filed = highlightFields.get("loc2");
            if (loc2Filed != null) {
                String loc2 = ESSearchUtil.getHightLightStr(loc2Filed);
                result.put("loc2", loc2);
            }
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countWXUser(String search) {
        final List<String> filedNames = Arrays.asList("username", "nickname", "sign", "loc1", "loc2");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countWXUser(ESSearchUtil.WXUSER_INDEX_NAME, ESSearchUtil.WXUSER_TYPE, builder, null);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("username", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("nickname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("sign", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("loc1", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("loc2", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_wxuser", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findWxChatRoom", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findWXChatroom(String search, Integer page, Integer pageSize) {
        final List<String> filedNames = Arrays.asList("chatroomname", "chatroomnickname");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        SearchHits searchHits = elasticSearchDao.searchWxChatroom(
                ESSearchUtil.WXTROOP_INDEX_NAME, ESSearchUtil.WXTROOP_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField chatroomnameFiled = highlightFields.get("chatroomname");
            if (chatroomnameFiled != null) {
                String chatroomname = ESSearchUtil.getHightLightStr(chatroomnameFiled);
                result.put("chatroomname", chatroomname);
            }
            HighlightField chatroomnicknameFiled = highlightFields.get("chatroomnickname");
            if (chatroomnicknameFiled != null) {
                String chatroomnickname = ESSearchUtil.getHightLightStr(chatroomnicknameFiled);
                result.put("chatroomnickname", chatroomnickname);
            }
            results.add(result);
        }
        if(results.isEmpty()){
            findWXChatroomByMongo(search, page, pageSize,results);
        }
        return results;
    }

    private void findWXChatroomByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results) {
        //"chatroomname", "chatroomnickname"
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("chatroomname", Pattern.compile("^.*" + search + ".*$")));
        objects.add(new BasicDBObject("chatroomnickname", Pattern.compile("^.*" + search + ".*$")));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_wxchatroom", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            result.put("chatroomowner", document.get("chatroomowner"));
            result.put("wxmember", document.get("wxmember"));
            result.put("_lastUpdate", document.get("_lastUpdate"));

            result.put("chatroomnickname", ESSearchUtil.getHightLightStr(search, document.getString("chatroomnickname")));
            Object chatroomName = document.get("chatroomname");
            if(chatroomName != null && chatroomName.toString().contains(search)){
                    chatroomName = ESSearchUtil.getHightLightStr(chatroomName.toString(), chatroomName.toString());
            }
            result.put("chatroomname", chatroomName);
            results.add(result);
        }
    }

    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countWXChatroom(String search) {
        final List<String> filedNames = Arrays.asList("chatroomname", "chatroomnickname");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        if (search.matches(REG)) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countWXChatroom(ESSearchUtil.WXTROOP_INDEX_NAME, ESSearchUtil.WXTROOP_TYPE, builder, null);
        if (aLong == null || aLong == 0) {
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("chatroomname", Pattern.compile("^.*" + search + ".*$")));
            objects.add(new BasicDBObject("chatroomnickname", Pattern.compile("^.*" + search + ".*$")));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_wxchatroom", query );
        }
        return aLong;
    }

    @Override
@Cacheable(value = "findContactPhoneNum", keyGenerator = "keyGenerator")
    public List<Map<String, Object>> findContactPhoneNum(String search, Integer page, Integer pageSize) {
        //搜索条件
        final List<String> filedNames = Arrays.asList("phonenum", "personname");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        HighlightBuilder hb = ESSearchUtil.getHighLightBuilder(filedNames, true);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        SearchHits searchHits = elasticSearchDao.searchContactPhoneNum(
                ESSearchUtil.CONTACT_INDEX_NAME, ESSearchUtil.CONTACT_TYPE, builder, null, hb, (page - 1) * pageSize, pageSize);
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> deviceUniques = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> result = searchHit.getSource();
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField phonenumFiled = highlightFields.get("phonenum");
            if (phonenumFiled != null) {
                String phonenum = ESSearchUtil.getHightLightStr(phonenumFiled);
                result.put("phonenum", phonenum);
            }
            HighlightField personnameFiled = highlightFields.get("personname");
            if (personnameFiled != null) {
                String personname = ESSearchUtil.getHightLightStr(personnameFiled);
                result.put("personname", personname);
            }
            Object o = result.get("device_unique");
            if(o != null){
                deviceUniques.add(o.toString());
            }
            results.add(result);
        }

        if(results.isEmpty()){
            findContactPhoneNumByMongo(search, page, pageSize,results,deviceUniques);
        }

        if (!CollectionUtils.isEmpty(results)) {
            Map<String, String> unique2name = esDeviceService.findDeviceUnique2DeviceName(deviceUniques);
            results.forEach(t -> t.put("devicename", unique2name.get(t.get("device_unique"))));
        }
        //此处需对对应的通讯录设备名进行查询
        return results;
    }

    private void findContactPhoneNumByMongo(String search, Integer page, Integer pageSize, List<Map<String, Object>> results,List<String> deviceUniques) {
        //组装搜索条件
        BasicDBList objects = new BasicDBList();
        objects.add(new BasicDBObject("phonenum", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
        objects.add(new BasicDBObject("personname", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
        BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
        //去mongodb中查询
        List<Document> res = esBaseMongoDao.findInfoByGatherNameAndQuery("infoData2", "t_contact_phonenum", query, null, page, pageSize);
        //匹配的字段进行高亮显示
        for (Document document : res) {
            Map<String, Object> result = new HashMap<>(10);
            String deviceUnique = document.getString("device_unique");
            result.put("device_unique", deviceUnique);
            deviceUniques.add(deviceUnique);
            result.put("phonenumSelf", document.getString("phonenumSelf"));
            String phoneNum = document.getString("phonenum");
            if(phoneNum != null && phoneNum.contains(search)){
                phoneNum = ESSearchUtil.getHightLightStr(phoneNum, phoneNum);
            }
            result.put("phonenum", phoneNum);
            result.put("personname", ESSearchUtil.getHightLightStr(search, document.getString("personname")));
            results.add(result);
        }
    }

    /**
     * 统计电话号码
     *
     * @param search 搜索的条件
     * @return 返回电话号码条数
     */
    @Override
@Cacheable(value = "count", keyGenerator = "keyGenerator")
    public Long countContactPhoneNum(String search) {
        final List<String> filedNames = Arrays.asList("phonenum", "personname");
        QueryBuilder builder = eSSearchUtil.getQueryBuilder(search.trim(), filedNames);
        boolean matches = search.matches(REG);
        if (matches) {
            builder = eSSearchUtil.getQueryBuilderMath(search.trim(), filedNames);
        }
        Long aLong = elasticSearchDao.countWXChatroom(ESSearchUtil.CONTACT_INDEX_NAME, ESSearchUtil.CONTACT_TYPE, builder, null);
        if(aLong == null || aLong == 0){
            //组装搜索条件
            BasicDBList objects = new BasicDBList();
            objects.add(new BasicDBObject("phonenum", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
            objects.add(new BasicDBObject("personname", Pattern.compile("^.*" + search + ".*$", Pattern.CASE_INSENSITIVE)));
            BasicDBObject query = new BasicDBObject(QueryOperators.OR, objects);
            //去mongodb中查询
            aLong = esBaseMongoDao.findInfoByGatherNameAndQueryCount("infoData2", "t_contact_phonenum", query );
        }
        return aLong;
    }

    /**
     * 通过对应的结果对对应的一键搜进行搜索
     *
     * @param search   对应的搜索条件
     * @param page     对应的页数
     * @param pageSize 对应的每页条数
     * @param timeFrom 对应的搜索开始时间
     * @param timeEnd  对应的搜索结束时间
     * @param type     对应的消息类型
     * @return 返回搜索结果
     */
    @Override
@Cacheable(value = "findSearchByInfo", keyGenerator = "keyGenerator")
    public Object findSearchResultBySearchInfo(String search, Integer page, Integer pageSize,
                                               String timeFrom, String timeEnd, String type) {
        if (StringUtils.isEmpty(search)) {
            throw new RuntimeException("搜索条件为NULL");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("对应的搜索类型为NULL");
        }
        if (page == null || pageSize == null) {
            throw new RuntimeException("搜索分页条件有误");
        }
        Object result;
        switch (type) {
            //案件信息
            case "case":
                result = this.findCase(search, timeFrom, timeEnd, page, pageSize);
                break;
            //涉案人员信息
            case "caseInfo":
                result = this.findPerson(search, page, pageSize);
                break;
            //设备信息
            case "device":
                result = this.findDevice(search, page, pageSize);
                break;
            //通讯录
            case "phoneMsg":
                result = this.findContactPhoneNum(search, page, pageSize);
                break;
            //短信息
            case "shortmsg":
                result = this.searchMsgService(search, page, pageSize, timeFrom, timeEnd);
                break;
            //通话记录
            case "phone":
                result = this.searchRecordService(search, timeFrom, timeEnd, page, pageSize);
                break;
            //QQ用户信息
            case "qq":
                result = this.findQQUser(search, page, pageSize);
                break;
            //微信用户信息
            case "wx":
                result = this.findWXUser(search, page, pageSize);
                break;
            //QQ群信息
            case "qqmsg":
                result = this.findQQTroop(search, page, pageSize);
                break;
            //微信群信息
            case "wxmsg":
                result = this.findWXChatroom(search, page, pageSize);
                break;
            //QQ好友聊天信息
            case "qqfriendmsg":
                result = this.searchQqMsgService(search, timeFrom, timeEnd, page, pageSize);
                break;
            //微信好友聊天信息
            case "wxfriendmsg":
                result = this.searchWxMsgService(search, page, pageSize, timeFrom, timeEnd);
                break;
            //QQ群聊天信息
            case "qqgroupmsg":
                result = this.searchQqTroopMsgService(search, timeFrom, timeEnd, page, pageSize);
                break;
            //微信群聊天信息
            case "wxGroupmsg":
                result = this.searchWxChatRoomMsg(search, page, pageSize, timeFrom, timeEnd);
                break;
            default:
                throw new RuntimeException("搜索类型不合规");
        }
        return result;
    }

    /**
     * 通过前缀对es中的号码进行查询
     */
    @Override
    @Cacheable(keyGenerator = "keyGenerator", value = "searchPreNumber")
    public HashSet<String> searchPreNumber(String type, String search) {

        HashSet<String> numbers = new HashSet<>();
        PrefixQueryBuilder query;
        switch (type) {
            case "phone":
                query = QueryBuilders.prefixQuery("phonenum", search.trim());
                SearchHits result1 = elasticSearchDao.searchPre(
                        ESSearchUtil.CONTACT_INDEX_NAME, ESSearchUtil.CONTACT_TYPE, query);
                for (SearchHit hit : result1) {
                    Map<String, Object> onePhoneInfo = hit.getSource();
                    Object phone = onePhoneInfo.get("phonenum");
                    if (phone == null) {
                        continue;
                    }
                    numbers.add(phone.toString());
                }
                break;
            case "wx":
                query = QueryBuilders.prefixQuery("username", search.trim());
                SearchHits result = elasticSearchDao.searchPre(ESSearchUtil.WXUSER_INDEX_NAME, ESSearchUtil.WXUSER_TYPE, query);
                for (SearchHit hit : result) {
                    Map<String, Object> oneWXInfo = hit.getSource();
                    Object wxUserName = oneWXInfo.get("username");
                    if (wxUserName == null) {
                        continue;
                    }
                    numbers.add(wxUserName.toString());
                }
                break;
            case "qq":
                query = QueryBuilders.prefixQuery("uin", search.trim());
                SearchHits result2 = elasticSearchDao.searchPre(
                        ESSearchUtil.QQUSER_INDEX_NAME, ESSearchUtil.QQUSER_TYPE, query);
                for (SearchHit hit : result2) {
                    Map<String, Object> oneQQInfo = hit.getSource();
                    Object qqUin = oneQQInfo.get("uin");
                    if (qqUin == null) {
                        continue;
                    }
                    numbers.add(qqUin.toString());
                }
                break;
            case "IDNumber":
                break;
            default:
                throw new RuntimeException("对应的账号类型不合规");
        }
        return numbers;
    }
}
