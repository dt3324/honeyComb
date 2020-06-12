package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.dao.GisMongoDao;
import com.hnf.honeycomb.service.GisDeviceService;
import com.hnf.honeycomb.service.GeoService;
import com.hnf.honeycomb.service.GisInsertLogs;
import com.hnf.honeycomb.util.*;
import com.mongodb.*;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("geoService")
public class GeoServiceImpl implements GeoService {

    @Resource
    private GisDeviceService gisDeviceService;

    @Resource
    private GisInsertLogs gisInsertLogs;

    @Resource
    private GisMongoDao gisMongoDao;

    @Resource
    private JsLocationUtil jsLocationUtil;

    //对应统计
    @Override
    public List<Map<String, Object>> countGeoCountPreDay(String searchNum, String userId, String startDate, String endDate,
                                                         String typeSelect, Integer timeLimit, String place) {
        if (searchNum == null || searchNum.trim().isEmpty()) {
            throw new RuntimeException("时空搜索账号为空");
        }
        //插入日志
        gisInsertLogs.insertSearchLog(userId, place, searchNum, typeSelect);
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, HashSet<String>> imeiAndMacs = gisDeviceService.findImeiAndMacByNumAndType(
                searchNum, typeSelect);
        HashSet<String> imeis = imeiAndMacs.get("imei");
        HashSet<String> macs = imeiAndMacs.get("mac");
        if (CollectionUtils.isEmpty(imeis) && CollectionUtils.isEmpty(macs)) {
            throw new RuntimeException("没有相关位置信息");
        }
        List<String> newImeis = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imeis)) {
            newImeis.addAll(imeis);
            newImeis = StringUtils.removeNullStrAndEmptyStrThenTrim(newImeis);
        }
        List<String> newMacs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(macs)) {
            newMacs.addAll(macs);
            newMacs = StringUtils.removeNullStrAndEmptyStrThenTrim(StringUtils.replaceArray(newMacs, "-", ":"));
        }
        BasicDBObject query = new BasicDBObject();
        BasicDBList obj = new BasicDBList();
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>"+newImeis);
        if (!CollectionUtils.isEmpty(newImeis)) {
            obj.add(new BasicDBObject("IMEI",
                    new BasicDBObject(QueryOperators.IN,
                            newImeis.toArray(new String[]{}))));
//            obj.add(new BasicDBObject("IMEI2", new BasicDBObject(QueryOperators.IN,
//                    searchNum.toArray(new String[]{}))));
        }
        if (!CollectionUtils.isEmpty(newMacs)) {
            obj.add(new BasicDBObject("MAC", new BasicDBObject(QueryOperators.IN,
                    newMacs.toArray(new String[]{}))));
        }
        query.append(QueryOperators.OR, obj);
//        System.out.println(">>>>>>>"+query);
        BasicDBObject timeQuery = new BasicDBObject();
        Date start = null;
        Date end = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            start = Utils.getLongMillFromStr(startDate);
            timeQuery.append("$gte", start);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            end = Utils.getLongMillFromStr(endDate);
            timeQuery.append("$lte", end);
        }
        List<Date> dates = new ArrayList<>();
        if (start != null && end != null) {
            dates = UtilMain.cutDate(start, end);
        }
        if (!timeQuery.isEmpty()) {
            query.append("Stime", timeQuery);
        }
        if (CollectionUtils.isEmpty(dates)) {
            start = UtilMain.parse("2015/01/01");
            end = UtilMain.parseDate(getNowDayStr());
            dates = UtilMain.cutDate(start, end);
        }
        //此时去查询15年之后的数据
//        if(CollectionUtils.isEmpty(dates)){
//        	dates.add(new Date());
//        	dates.add(new Date());
//        }
        List<Long> date = new ArrayList<Long>();
        dates.forEach((Date d) -> {
            date.add(d.getTime());
        });
        Map<String, String> jsLocation = jsLocationUtil.getJsLocation();
        String map = jsLocation.get("countMap.js")
                .replaceAll("dates", date.toString());
        String reduce = jsLocation.get("countReduce.js");
        List<Document> mapRedResult = gisMongoDao.mapReduceByDBNameAndGatherNameAndQuery(
                "MAC", "MACDATA2", map, reduce, query);
//		mapRedResult.forEach(t->{
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>"+t);
//		});
        if (!CollectionUtils.isEmpty(mapRedResult)) {
            mapRedResult.forEach(t -> {
                Map<String, Object> one = new HashMap<>();
                one.put("time", t.get("_id"));
                one.put("total", t.get("value"));
                results.add(one);
            });
        }

        results.sort((t1, t2) -> {
            Long time1 = Long.valueOf(t1.get("time").toString());
            Long time2 = Long.valueOf(t2.get("time").toString());
            return time1.compareTo(time2);
        });
        return results;
    }

    //获取当前年月日的字符串
    private static String getNowDayStr() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        return sdf.format(now);
    }

    public static void main(String[] args) {

        System.out.println(getNowDayStr());
    }


    //对应位置
    @Override
    public List<Document> findGeoBySomeTerm(String searchNum, String startDate, String endDate, String typeSelect,
                                            Integer timeLimit, Integer page, Integer pageSize) {
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("搜索账号为NULL");
        }
        Map<String, HashSet<String>> imeiAndMacs = gisDeviceService.findImeiAndMacByNumAndType(
                searchNum, typeSelect);
        HashSet<String> imeis = imeiAndMacs.get("imei");
        HashSet<String> macs = imeiAndMacs.get("mac");
        if (CollectionUtils.isEmpty(imeis) && CollectionUtils.isEmpty(macs)) {
            throw new RuntimeException("沒有相關位置信息");
        }
        List<String> newImeis = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imeis)) {
            newImeis.addAll(imeis);
            newImeis = StringUtils.removeNullStrAndEmptyStrThenTrim(newImeis);
        }
        List<String> newMacs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(macs)) {
            newMacs.addAll(macs);
            newMacs = StringUtils.removeNullStrAndEmptyStrThenTrim(StringUtils.replaceArray(newMacs, "-", ":"));
        }
        BasicDBObject timeQuery = new BasicDBObject();
        Date start = null;
        Date end = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            start = Utils.getLongMillFromStr(startDate);
            timeQuery.append("$gte", start);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            //计算结束时间
            end = Utils.getLongMillFromStr(endDate);
            timeQuery.append("$lte", end);
        }
        BasicDBObject query = new BasicDBObject();
        //拼接几个值在几个字段中的出现次数
        BasicDBList obj = new BasicDBList();
        if (!CollectionUtils.isEmpty(newImeis)) {
            obj.add(new BasicDBObject("IMEI",
                    new BasicDBObject(QueryOperators.IN,
                            newImeis.toArray(new String[]{}))));
//             obj.add(new BasicDBObject("IMEI2", new BasicDBObject(QueryOperators.IN,
//                     searchNum.toArray(new String[]{}))));

        }
        if (!CollectionUtils.isEmpty(newMacs)) {
            obj.add(new BasicDBObject("MAC", new BasicDBObject(QueryOperators.IN,
                    newMacs.toArray(new String[]{}))));
        }
        query.append(QueryOperators.OR, obj);
        /* if (start != null ) {
             query.append("Stime", new BasicDBObject("$gte", start).append("$lte", Utils.addDay(start)));
         }*/
        if (!timeQuery.isEmpty()) {
            query.append("Stime", timeQuery);
        }
        BasicDBObject sort = new BasicDBObject("Stime", -1);
        List<Document> result = gisMongoDao.findInfoByDBNameAndGatherNameAndQueryAndSort(
                "MAC", "MACDATA2", query, sort);
        return result;
    }

    //点画圆
    @Override
    public Object gerWithinOnePoint1(String lat, String lon, Double radius, String startDate, String endDate,
                                     String typeSelect, String search, String path) {
        if (!StringUtils.isEmpty(path)) {
            //此时为进行对应的多边形搜索
            List<Document> result = gerWithinPolygon(startDate, endDate, typeSelect, search, path);
            return result;
        }
        if (lat == null || lat.trim().isEmpty() || lon == null || lon.isEmpty()) {
            throw new RuntimeException("经纬度错误");
        }
        if (radius == null) {
            throw new RuntimeException("半径有误");
        }
        if (typeSelect == null) {
            throw new RuntimeException("选择类型错误");
        }
        Map<String, HashSet<String>> imeiAndMacs = gisDeviceService.findImeiAndMacByNumAndType(
                search, typeSelect);
        HashSet<String> imeis = imeiAndMacs.get("imei");
        HashSet<String> macs = imeiAndMacs.get("mac");
        if (CollectionUtils.isEmpty(imeis) && CollectionUtils.isEmpty(macs)) {
            throw new RuntimeException("沒有相關位置信息");
        }
        List<String> newImeis = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imeis)) {
            newImeis.addAll(imeis);
            newImeis = StringUtils.removeNullStrAndEmptyStrThenTrim(newImeis);
        }
        List<String> newMacs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(macs)) {
            newMacs.addAll(macs);
            newMacs = StringUtils.removeNullStrAndEmptyStrThenTrim(StringUtils.replaceArray(newMacs, "-", ":"));
        }
        Long startTime = null;
        Long endTime = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            startTime = TimeUtils.getLongFromDateStr(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            endTime = TimeUtils.getLongFromDateStr(endDate);
        }
        BasicDBObject query1 = new BasicDBObject();
        BasicDBList obj = new BasicDBList();
        if (imeis != null && !imeis.isEmpty()) {
            obj.add(new BasicDBObject("IMEI",
                    new BasicDBObject(QueryOperators.IN,
                            imeis.toArray(new String[]{}))));
            //            obj.add(new BasicDBObject("IMEI2", new BasicDBObject(QueryOperators.IN,
            //                    searchNum.toArray(new String[]{}))));

        }
        if (macs != null && !macs.isEmpty()) {
            obj.add(new BasicDBObject("MAC", new BasicDBObject(QueryOperators.IN,
                    macs.toArray(new String[]{}))));
        }
        if (!obj.isEmpty()) {
            query1.append(QueryOperators.OR, obj);
        }
        BasicDBList l = new BasicDBList();
        //经度
        l.add(Double.valueOf(lon));
        //纬度
        l.add(Double.valueOf(lat));
        BasicDBList ls = new BasicDBList();
        ls.add(l);
        //0.00002
        ls.add((double) ((double) radius / (double) 6378137));
        DBObject loc = new BasicDBObject("$geoWithin",
                new BasicDBObject("$centerSphere", ls));
        //			obj.add(new BasicDBObject("loc",loc).
        //					append("time", new BasicDBObject("$gte", date.get(0))
        //					.append("$lte", date.get(1))));
        query1.append("Bloc.bdcoordinates", loc);
        if (startTime != null) {
            query1.append("Stime", new BasicDBObject(QueryOperators.GTE, new Date(startTime)));
        }
        if (endTime != null) {
            query1.append("Stime", new BasicDBObject(QueryOperators.LTE, new Date(endTime)));
        }
        List<Document> result = gisMongoDao.findInfoByDBNameAndGatherNameAndQuery("MAC", "MACDATA2", query1);
        //用于mongodb中的样本数据抽取
//        BasicDBObject sampleQuery = new BasicDBObject("$sample", new BasicDBObject("size", 10));
//        List<DBObject> aggreQuerys = Arrays.asList(
//                new BasicDBObject("$match", query1), sampleQuery);
//        AggregationOutput samples = locationDao.findSampleGeo(aggreQuerys);
//        List<DBObject> sampleResult = new ArrayList<>();
//        samples.results().forEach(sample -> {
//            sample.removeField("_id");
//            sampleResult.add(sample);
//        });
        return result;
    }

    /**
     * 通过对应的多边形搜索符合条件的点
     *
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @param typeSelect 对应账号选择的类型
     * @param search     对应的搜索条件
     * @param path       对应的多边形的点
     * @return
     */
    private List<Document> gerWithinPolygon(String startDate, String endDate, String typeSelect, String search,
                                            String path) {
        BasicDBObject query = new BasicDBObject();
        Map<String, HashSet<String>> imeiAndMacs = gisDeviceService.findImeiAndMacByNumAndType(
                search, typeSelect);
        HashSet<String> imeis = imeiAndMacs.get("imei");
        HashSet<String> macs = imeiAndMacs.get("mac");
        if (CollectionUtils.isEmpty(imeis) && CollectionUtils.isEmpty(macs)) {
            throw new RuntimeException("沒有相關位置信息");
        }
        List<String> newImeis = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imeis)) {
            newImeis.addAll(imeis);
            newImeis = StringUtils.removeNullStrAndEmptyStrThenTrim(newImeis);
        }
        List<String> newMacs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(macs)) {
            newMacs.addAll(macs);
            newMacs = StringUtils.removeNullStrAndEmptyStrThenTrim(StringUtils.replaceArray(newMacs, "-", ":"));
        }
        BasicDBList obj = new BasicDBList();
        if (newImeis != null && !newImeis.isEmpty()) {
            obj.add(new BasicDBObject("IMEI",
                    new BasicDBObject(QueryOperators.IN,
                            newImeis.toArray(new String[]{}))));
        }
        if (newMacs != null && !newMacs.isEmpty()) {
            obj.add(new BasicDBObject("MAC", new BasicDBObject(QueryOperators.IN,
                    newMacs.toArray(new String[]{}))));
        }
        if (!obj.isEmpty()) {
            query.append(QueryOperators.OR, obj);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            Long startTime = TimeUtils.getLongFromDateStr(startDate);
            query.append("Stime", new BasicDBObject(QueryOperators.GTE, new Date(startTime)));

        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            Long endTime = TimeUtils.getLongFromDateStr(endDate);
            query.append("Stime", new BasicDBObject(QueryOperators.LTE, new Date(endTime)));
        }
        List<BasicDBList> list = getPolygon(path);
        query.append("Bloc.bdcoordinates",
                new BasicDBObject("$geoWithin", new BasicDBObject("$polygon", list)));
        List<Document> result = gisMongoDao.findInfoByDBNameAndGatherNameAndQuery("MAC", "MACDATA2", query);
        return result;
        //		List<Document> result = RMIUtils.gerWithinPolygon(query);
//		//		System.out.println("size:"+result.size());
//		//		for(Document doc : result){
//		//			System.out.println("doc:"+doc);
//		//		}
//		return result;
    }

    /**
     * 通过传入的json字符串获取多边形
     *
     * @param path
     * @return
     */
    private List<BasicDBList> getPolygon(String path) {
        List<BasicDBList> result = new ArrayList<>();
        JSONArray jarry = JSONArray.fromObject(path);
        List<?> list = JSONArray.toList(jarry);
        for (Object obj1 : list) {
            MorphDynaBean onePoint = MorphDynaBean.class.cast(obj1);
            Double lng = Double.class.cast(onePoint.get("lng"));
            Double lat = Double.class.cast(onePoint.get("lat"));
            BasicDBList one = new BasicDBList();
            one.add(lng);
            one.add(lat);
            result.add(one);
        }
        result.add(result.get(result.size() - 1));
        return result;
    }


    @Override
    public List<Document> findGeoByTime(String searchNum,
                                        String date, String typeSelect, Integer timeLimit, Integer page, Integer pageSize) {
        if (StringUtils.isEmpty(searchNum)) {
            throw new RuntimeException("搜索账号为NULL");
        }
        Map<String, HashSet<String>> imeiAndMacs = gisDeviceService.findImeiAndMacByNumAndType(
                searchNum, typeSelect);
        HashSet<String> imeis = imeiAndMacs.get("imei");
        HashSet<String> macs = imeiAndMacs.get("mac");
        if (CollectionUtils.isEmpty(imeis) && CollectionUtils.isEmpty(macs)) {
            throw new RuntimeException("沒有相关位置信息");
        }
        List<String> newImeis = new ArrayList<>();
        if (!CollectionUtils.isEmpty(imeis)) {
            newImeis.addAll(imeis);
            newImeis = StringUtils.removeNullStrAndEmptyStrThenTrim(newImeis);
        }
        List<String> newMacs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(macs)) {
            newMacs.addAll(macs);
            newMacs = StringUtils.removeNullStrAndEmptyStrThenTrim(StringUtils.replaceArray(newMacs, "-", ":"));
        }
        Date start = null;
        if (date != null && !date.trim().isEmpty()) {
            start = Utils.getLongMillFromStr(date);
        }
        BasicDBObject query = new BasicDBObject();
        //拼接几个值在几个字段中的出现次数
        BasicDBList obj = new BasicDBList();
        if (!CollectionUtils.isEmpty(newImeis)) {
            obj.add(new BasicDBObject("IMEI",
                    new BasicDBObject(QueryOperators.IN,
                            newImeis.toArray(new String[]{}))));
//             obj.add(new BasicDBObject("IMEI2", new BasicDBObject(QueryOperators.IN,
//                     searchNum.toArray(new String[]{}))));

        }
        if (!CollectionUtils.isEmpty(newMacs)) {
            obj.add(new BasicDBObject("MAC", new BasicDBObject(QueryOperators.IN,
                    newMacs.toArray(new String[]{}))));
        }
        query.append(QueryOperators.OR, obj);
        if (start != null) {
            query.append("Stime", new BasicDBObject("$gte", start).append("$lte", Utils.addDay(start)));
        }
        BasicDBObject sort = new BasicDBObject("Stime", -1);
        List<Document> result = gisMongoDao.findInfoByDBNameAndGatherNameAndQueryAndSort(
                "MAC", "MACDATA2", query, sort);
        return result;
    }
}
