package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.config.MongoBaseClientClusterConfig;
import com.hnf.honeycomb.config.TomcatUrlConfig;
import com.hnf.honeycomb.dao.UserMongoDao;
import com.hnf.honeycomb.mapper.SoftDogMapper;
import com.hnf.honeycomb.remote.user.BusinessDepartmentMapper;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.remote.user.FetchLogMapper;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import com.hnf.honeycomb.service.user.UserDeviceService;
import com.hnf.honeycomb.util.ExportExcel;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.util.ObjectUtil.getLong;
import static com.hnf.honeycomb.util.ObjectUtil.getString;


/**
 * 设备业务层接口实现
 *
 * @author zhouhong
 */
@Service
public class UserDeviceServiceImpl implements UserDeviceService {

    private Logger logger = LoggerFactory.getLogger(UserDeviceServiceImpl.class);
    @Resource
    private BusinessDepartmentMapper businessDepartmentMapper;

    @Resource
    private SoftDogMapper softDogMapper;

    @Resource
    private FetchLogMapper fetchLogMapper;

    @Resource
    private UserMongoDao userMongoDao;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private UserDepartmentService userDepartmentService;

    private final TomcatUrlConfig tomcatUrlConfig;

    private final MongoClient mongoClient;

    @Autowired
    public UserDeviceServiceImpl(@Qualifier(MongoBaseClientClusterConfig.MONGO_BASE) MongoClient mongoClient, TomcatUrlConfig tomcatUrlConfig) {
        this.mongoClient = mongoClient;
        this.tomcatUrlConfig = tomcatUrlConfig;
    }

    @Override
    public Map<String, Object> aggregateDeviceInfo(String departmentCode, Long startTime, Long endTime) {
        //对应的查询条件
        BasicDBObject query = new BasicDBObject();
        if (startTime != null || endTime != null) {
            Document fetchTime = new Document();
            if (startTime != null) {
                fetchTime.append("$gte", startTime);
            }
            if (endTime != null) {
                fetchTime.append("$lte", endTime);
            }
            query.append("fetchtime", fetchTime);
        }
        //单位处理
        departmentCode = StringUtils.getOldDepartmentCode(departmentCode);
        Pattern unitPattern = Pattern.compile("^" + departmentCode + ".*$", Pattern.CASE_INSENSITIVE);
        query.append("department_code", unitPattern);

        //表明采集的是安卓
        query.append("type", "1");
        String androidMapFunction = "function() {\n" +
                "    var androidver = this.androidver;\n" +
                "    if (androidver == null || typeof(androidver) == \"undefined\" || androidver == \"null\" || androidver == \"\") {\n" +
                "        emit( \"-1\", 1);\n" +
                "        return;\n" +
                "    };\n" +
                "    var index = androidver.indexOf(\".\");\n" +
                "    var androidverStr ='android版本'+ androidver ;\n" +
                "    if (index == -1) {\n" +
                "        if (parseInt(androidver) > 9) {\n" +
                "            emit( \"-1\", 1);\n" +
                "            return;\n" +
                "        }\n" +
                "        emit(androidverStr, 1);\n" +
                "        return;\n" +
                "    };\n" +
                "    var ver = 'android版本'+ androidver.substring(0, index);\n" +
                "    if (parseInt(ver) > 9) {\n" +
                "        emit( \"-1\", 1);\n" +
                "    } else {\n" +
                "        emit(ver, 1);\n" +
                "    }\n" +
                "};";
        String reduceFunction = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        List<DBObject> android = userMongoDao.mapReduce("infoData2", "fetchlog", androidMapFunction, reduceFunction, query);
        //表明采集的是IOS
        query.append("type", "2");
        String iPhoneMapFunction = "function() {\n" +
                "    var IOS = this.androidver;\n" +
                "    if (IOS == null || typeof(IOS) == \"undefined\" || IOS == \"null\" || IOS == \"\") {\n" +
                "        emit( \"-1\", 1);\n" +
                "        return;\n" +
                "    };\n" +
                "    var index = IOS.indexOf(\".\");\n" +
                "    if (index == -1) {\n" +
                "        emit(IOS, 1);\n" +
                "        return;\n" +
                "    };\n" +
                "    var ver = 'IOS版本'+ IOS.substring(0, index);\n" +
                "    emit(ver, 1);\n" +
                "};";
        String iPhoneReduceFunction = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        List<DBObject> iPhone = userMongoDao.mapReduce("infoData2", "fetchlog", iPhoneMapFunction, iPhoneReduceFunction, query);


        query.remove("type");
        String deviceMapFunction = "function() {\n" +
                "    var model = this.brand;\n" +
                "    if (model == null || model == \"null\" || model == \"\") {\n" +
//                "        emit(\"-1\", 1);\n" +
                "        return;\n" +
                "    };\n" +
                "    if (model.indexOf(\"苹果\") > -1) {\n" +
                "        emit(\"iPhone\", 1);\n" +
                "        return;\n" +
                "    }\n" +
                "    model = model.toUpperCase();\n" +
                "    if (model.startsWith(\"IPHONE\")) {\n" +
                "        emit(\"iPhone\", 1);\n" +
                "        return;\n" +
                "    }\n" +
                "    if (model.startsWith(\"IPAD\")) {\n" +
                "        emit(\"iPad\", 1);\n" +
                "        return;\n" +
                "    }\n" +
                "    var index = model.indexOf(\" \");\n" +
                "    if (index == -1) {\n" +
                "        emit(model, 1);\n" +
                "    } else {\n" +
                "        emit(model.substring(0, index), 1);\n" +
                "    }\n" +
                "};";
        List<DBObject> deviceName = userMongoDao.mapReduce("infoData2", "fetchlog", deviceMapFunction, reduceFunction, query);
        List<DBObject> returnDeviceName = new ArrayList<>();
        deviceName.stream().filter(dbObject -> Double.valueOf(dbObject.get("value").toString()) > 20).forEach(returnDeviceName::add);
        String mapStr = "function(){\n"
                + "var qq = this.qq;\n"
                + "var phone = this.phone;\n"
                + "var wx = this.wx;\n"
                + "var gps = this.gps;\n"
                + "var qqPre = 0;\n"
                + "var wxPre = 0;\n"
                + "var phonePre = 0;\n"
                + "var gpsPre = 0;\n"
                + "	if(qq == 1){\n"
                + "          qqPre = 0.4; "
                + "	};"
                + "	if(wx == 1){\n"
                + "          wxPre = 0.4; "
                + "	};"
                + "	if(phone == 1){\n"
                + "          phonePre = 0.1; "
                + "	};"
                + "	if(gps == 1){\n"
                + "          gpsPre = 0.1; "
                + "	};"
                + "	emit(qqPre + wxPre + phonePre + gpsPre,1);\n"
                + "};";
        String reduceStr = "function(key,values){	"
                + "     return Array.sum(values);   "
                + "};";
        query.append("collType", 1);
        //统计一个设备采集的采集率，采集一个qq 为0.4，采集一个wx 0.4 。。。
        List<DBObject> bbCollPre = userMongoDao.mapReduce("infoData2", "fetchlog", mapStr, reduceStr, query);
        query.append("collType", 2);
        List<DBObject> sbCollPre = userMongoDao.mapReduce("infoData2", "fetchlog", mapStr, reduceStr, query);
        Map<String, Object> result = new HashMap<>(6);
        result.put("android", android);
        result.put("IOS", iPhone);
        result.put("deviceName", returnDeviceName);
        result.put("bbCollPre", bbCollPre);
        result.put("sbCollPre", sbCollPre);
        return result;
    }

    @Override
    public Map<String, Object> aggregateDataInfo(String departmentCode, Long startTime, Long endTime) {
        return null;
    }

    @Override
    public void downloadExcel(String fileName, HttpServletResponse response) {
        // 下载本地文件
        // 读到流中// 文件的存放路径
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(new File("").getAbsolutePath()
                    + File.separator + "ExportExcel" + File.separator + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 设置输出的格式
        response.reset();
        response.setContentType("bin");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        // 循环取出流中的数据
        byte[] b = new byte[1024];
        int len;
        try {
            if (inStream != null) {
                while ((len = inStream.read(b)) > 0) {
                    response.getOutputStream().write(b, 0, len);
                }
                inStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> fetchPercentage(Map<String, Object> map) {
        String departCode = getString(map.get("departCode"));
        if (StringUtils.isEmpty(departCode)) {
            throw new RuntimeException("请选择部门单位！");
        }

        //参数封装
        Long startTime = getLong(map.get("startTime"));
        Long endTime = getLong(map.get("endTime"));
        //mysql需要参数
        HashMap<String, Object> param = new HashMap<>(6);
        //mongo需要参数
        BasicDBObject mongoParam = new BasicDBObject();
        Pattern compile = Pattern.compile("^" + StringUtils.getOldDepartmentCode(departCode) + ".*$");
        mongoParam.put("department_code", compile);
        param.put("departCode", StringUtils.getOldDepartmentCode(departCode));
        if (startTime != null && endTime != null) {
            BasicDBObject timeQuery = new BasicDBObject();
            timeQuery.append("$gte", startTime);
            timeQuery.append("$lt", endTime);
            mongoParam.put("fetchtime", timeQuery);
            param.put("startDate", startTime);
            param.put("endDate", endTime);
        }

        Map<String, Object> returnMap = new HashMap<>(7);
        //获取单位type
        Integer departmentType = StringUtils.getDepartmentType(departCode);
        //数据库查询封装
        List<Map<String, Object>> results = findDepartmentTypeFetch(departmentType, param, mongoParam);
        returnMap.put("dataDetail", results);
        return returnMap;
    }

    @Override
    public Map<String, Object> fetchEntiretyPercentage(Map<String, Object> map) throws ParseException {
        String departCode = getString(map.get("departCode"));
        String startTimeStr = getString(map.get("startDate"));
        String endTimeStr = getString(map.get("endDate"));
        Pattern pattern = Pattern.compile("^" + StringUtils.getOldDepartmentCode(departCode) + ".*$", Pattern.CASE_INSENSITIVE);
        BasicDBObject dbObject = new BasicDBObject("department_code", pattern);
        Long startTime;
        if (startTimeStr != null && !"".equals(startTimeStr)) {
            startTime = new SimpleDateFormat("yyyy/MM/dd").parse(startTimeStr).getTime();
            dbObject.put("fetchtime", new BasicDBObject("$gt", startTime));
        }
        Long endTime;
        if (endTimeStr != null && !"".equals(endTimeStr)) {
            endTime = new SimpleDateFormat("yyyy/MM/dd").parse(endTimeStr).getTime() + 24 * 60 * 60 * 1000 - 1;
            dbObject.put("fetchtime", new BasicDBObject("$lt", endTime));
        }
        MongoCollection<Document> collection = mongoClient.getDatabase("infoData2").getCollection("fetchlog");
        FindIterable<Document> documents = collection.find(dbObject);
        //通过采集日志中的设备唯一标识查询QQ WX 通讯录的采集情况
        Map<String, Object> result = new HashMap<>(7);
        result.put("departCode", map.get("departCode").toString());
        result.put("departName", userDepartmentService.findWholeDepartmentNameByDepartCode(map.get("departCode").toString()));
        getPercentage(result, documents);
        return result;
    }

    /**
     * 次数用MySQL 质量用mongo
     *
     * @return 采集情况
     */
    private List<Map<String, Object>> findDepartmentTypeFetch(int departmentType, Map<String, Object> param, BasicDBObject mongoParam) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //字符串截取 及分组
        BasicDBList objects = new BasicDBList();
        BasicDBList subStr = new BasicDBList();
        subStr.add("$department_code");
        subStr.add(0);
        switch (departmentType) {
            case 1:
                param.put("codeLength", 4);
                subStr.add(4);
                objects.add(new BasicDBObject("$substr", subStr));
                objects.add("0000000");
                break;
            case 2:
                param.put("codeLength", 6);
                subStr.add(6);
                objects.add(new BasicDBObject("$substr", subStr));
                objects.add("00000");
                break;
            default:
                param.put("codeLength", 11);
                subStr.add(11);
                objects.add(new BasicDBObject("$substr", subStr));
                objects.add("");
                break;
        }
        List<Map<String, Object>> bBeeFetch = fetchLogMapper.countBBeeFetch(param);
        //单位下采集手机部数
        List<Map<String, Object>> bBeeDevice = fetchLogMapper.countBBeeFetchDist(param);
        HashMap<Object, Object> bBeeDeviceMap = new HashMap<>();
        bBeeDevice.forEach(b -> bBeeDeviceMap.put(b.get("code"), b.get("total")));
        MongoCollection<Document> collection = mongoClient.getDatabase("infoData2").getCollection("fetchlog");
        //查询 的条件
        BasicDBObject match = new BasicDBObject("$match", mongoParam);
        //查询的字段 以及字段的形式
        BasicDBObject project = new BasicDBObject(
                "$project",
                new BasicDBObject("department_code", 1)
                        .append("app", new BasicDBObject("$max", Arrays.asList("$wx", "$qq")))
                        .append("msg", "$shortMessage")
                        .append("callLog", 1)
                        .append("phone", 1)
        );
        //用数据去统计数量 减少查询次数 降低服务器压力
        BasicDBObject group = new BasicDBObject(
                "$group",
                new BasicDBObject("_id", new BasicDBObject("$concat", objects))
                        .append("count", new BasicDBObject("$sum", 1))
                        .append("app", new BasicDBObject("$sum", "$app"))
                        .append("msg", new BasicDBObject("$sum", "$msg"))
                        .append("callLog", new BasicDBObject("$sum", "$callLog"))
                        .append("phone", new BasicDBObject("$sum", "$phone"))
        );
        AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(match, project, group));

        //数值格式化对象 用来计算百分比
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        HashMap<String, Map<String, Object>> percentage = new HashMap<>();
        for (Document document : aggregate) {
            HashMap<String, Object> map = new HashMap<>(6);
            Integer app = document.getInteger("app");
            Integer msg = document.getInteger("msg");
            Integer callLog = document.getInteger("callLog");
            Integer phone = document.getInteger("phone");
            Integer count = document.getInteger("count");
            //算出每个的百分比
            String appPercent = app == 0 ? "0%" : numberFormat.format((float) app / (float) count * 100) + "%";
            map.put("appPercent", appPercent);
            String phonePercent = phone == 0 ? "0%" : numberFormat.format((float) phone / (float) count * 100) + "%";
            map.put("phonePercent", phonePercent);
            String callLogPercent = callLog == 0 ? "0%" : numberFormat.format((float) callLog / (float) count * 100) + "%";
            map.put("callLogPercent", callLogPercent);
            String shortMsgPercent = msg == 0 ? "0%" : numberFormat.format((float) msg / (float) count * 100) + "%";
            map.put("shortMsgPercent", shortMsgPercent);
            percentage.put(document.getString("_id"), map);

        }
        bBeeFetch.forEach(b -> {
            String code = (String) b.get("code");
            Map<String, Object> result = new HashMap<>(7);
            result.put("fetchCount", b.get("total"));
            result.put("deviceCount", bBeeDeviceMap.get(code));
            //把每个单位对应的采集情况添加进来
            result.putAll(percentage.get(code));
            //添加部门
            DepartmentBean departmentBean = userDepartmentService.findByCode(code);
            String d = "";
            if (departmentBean != null) {
                d = departmentBean.getDepartmentName();
            }
            result.put("departCode", code);
            result.put("departName", StringUtils.isEmpty(d) ? "单位不存在" : d);
            resultList.add(result);
        });
        return resultList;
    }

    /**
     * 统计QQ WX 通讯录的采集情况
     */
    private void getPercentage(Map<String, Object> result, FindIterable<Document> documents) {
        //数值格式化对象 用来计算百分比
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        int appCount = 0;
        int phoneCount = 0;
        int callLogCount = 0;
        int shortMsgCount = 0;
        int size = 0;
        for (Document document : documents) {
            size++;
            if (document.get("qq").equals(1) || document.get("wx").equals(1)) {
                appCount++;
            }
            if (document.get("phone").equals(1)) {
                phoneCount++;
            }
            if (document.get("callLog") != null) {
                if (document.get("callLog").equals(1)) {
                    callLogCount++;
                }
            }
            if (document.get("shortMessage") != null) {
                if (document.get("shortMessage").equals(1)) {
                    shortMsgCount++;
                }
            }
        }

        //算出每个的百分比
        String appPercent = appCount == 0 ? "0%" : numberFormat.format((float) appCount / (float) size * 100) + "%";
        result.put("appPercent", appPercent);
        String phonePercent = phoneCount == 0 ? "0%" : numberFormat.format((float) phoneCount / (float) size * 100) + "%";
        result.put("phonePercent", phonePercent);
        String callLogPercent = callLogCount == 0 ? "0%" : numberFormat.format((float) callLogCount / (float) size * 100) + "%";
        result.put("callLogPercent", callLogPercent);
        String shortMsgPercent = shortMsgCount == 0 ? "0%" : numberFormat.format((float) shortMsgCount / (float) size * 100) + "%";
        result.put("shortMsgPercent", shortMsgPercent);
    }

    @Override
    public Map<String, String> fetchQualityExportExcel(String personKeyWord, String path, String departmentCode, Integer pageNum, Integer pageSize,
                                                       String wx, String qq, String fetchPhone, String gps, Long startTime,
                                                       Long endTime, HttpServletResponse response) {
        Map<String, String> result = new HashMap<>(2);
        //采集内容
        List<Document> list = (List) aggregateCaseDevicePersonInfo(
                personKeyWord, departmentCode, 0, 0, wx, qq, fetchPhone, gps, startTime, endTime).get("detail");
        List<List<Object>> detailList = new ArrayList<>();
        for (Document document : list) {
            detailList.add(document2List(document));
        }

        //添加采集时间
        List<String[]> strings = new ArrayList<>();
        String[] time;
        if (startTime != null) {
            String startTimeStr = new SimpleDateFormat("yyyy年MM月dd日").format(startTime);
            String endTimeStr = new SimpleDateFormat("yyyy年MM月dd日").format(getDate(endTime));
            time = new String[]{"采集时间范围", startTimeStr, "至", endTimeStr};
        } else {
            time = new String[]{"采集时间范围", "全部"};
        }
        strings.add(time);

        //添加采集单位
        Integer departmentType = StringUtils.getDepartmentType(departmentCode);
        String[] department = {};
        switch (departmentType) {
            case 1:
                String departmentName = departmentMapper.findByDepartmentCode(departmentCode).getDepartmentName();
                department = new String[]{"单位", departmentName, "所有市"};
                break;
            case 2:
                List<DepartmentBean> parentDepart = userDepartmentService.findParentDepart(departmentCode);
                department = new String[4];
                department[0] = "单位";
                for (int i = 1; i <= parentDepart.size(); i++) {
                    department[i] = parentDepart.get(i - 1).getDepartmentName();
                }
                department[3] = "所有区";
                break;
            case 3:
                List<DepartmentBean> parentDepart1 = userDepartmentService.findParentDepart(departmentCode);
                department = new String[5];
                department[0] = "单位";
                for (int i = 1; i <= parentDepart1.size(); i++) {
                    department[i] = parentDepart1.get(i - 1).getDepartmentName();
                }
                department[4] = "所有部门";
                break;
            case 4:
                String departmentName1 = departmentMapper.findByDepartmentCode(departmentCode).getDepartmentName();
                department = new String[]{"单位", departmentName1};
                break;
            default:
                break;

        }
        strings.add(department);
        //采集项
        String[] personKeyWords = new String[2];
        personKeyWords[0] = "人员涉案标签";
        if (StringUtils.isEmpty(personKeyWord)) {
            personKeyWords[1] = "所有标签";
        } else {
            personKeyWords[1] = getPersonKetName(personKeyWord);
        }
        strings.add(personKeyWords);

        //采集项
        String[] fetchNape = new String[5];
        fetchNape[0] = "采集质量";
        int a = 1;
        if (!StringUtils.isEmpty(wx)) {
            fetchNape[a] = "微信已采";
            a++;
        }
        if (!StringUtils.isEmpty(qq)) {
            fetchNape[a] = "QQ已采";
            a++;
        }
        if (!StringUtils.isEmpty(gps)) {
            fetchNape[a] = "位置信息已采";
            a++;
        }
        if (!StringUtils.isEmpty(fetchPhone)) {
            fetchNape[a] = "通讯信息已采";
        }
        if (a == 1) {
            fetchNape[a] = "无";
        }
        strings.add(fetchNape);
        //excel中需要的字段
        String[] rowName = {"被采人员", "身份证号", "电话号码", "设备名称", "人员涉案标签", "案件名称", "案件编号", "通讯信息", "微信",
                "QQ", "GPS", "采集单位", "采集警号", "采集时间", "操作"};
        ExportExcel exportExcel = new ExportExcel("蜂巢-被采人员详情", strings, rowName, detailList);
        //每次导出前先将以前导出的文件删除
        String fileParent = new File("").getAbsoluteFile() + File.separator + "ExportExcel";
        File fileD = new File(fileParent);
        if (fileD.isDirectory()) {
            File[] files = fileD.listFiles();
            if (!(files == null || files.length <= 0)) {
                for (File file1 : files) {
                    if (file1.isFile()) {
                        file1.delete();
                    }
                }
            }
        }
        FileOutputStream fileOutputStream = null;
        //文件名
        String fileName = File.separator + "cjzl_" + System.currentTimeMillis() + ".xls";
        try {
            //文件输出流
            File file = new File(new File("").getAbsolutePath() +
                    File.separator + "ExportExcel");
            if (!file.exists()) {
                file.mkdirs();
            }
            fileOutputStream = new FileOutputStream(new File("").getAbsoluteFile() +
                    File.separator + "ExportExcel" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //导出excel
        exportExcel.export(fileOutputStream);
        //ftp下载路径
//        String excelDownloadPath = ftpConfig.getExcelDownloadPath(File.separator + "ExportExcel" + fileName);
//        result.put("ftpPath", excelDownloadPath);
        //tomcat下载需要的文件名
        result.put("ftpPath", "http://" + tomcatUrlConfig.getTomcatUrl() + "/dataRoot" + fileName);

        return result;
    }

    private String getPersonKetName(String personKeyWord) {
        String s;
        switch (personKeyWord) {
            case "1":
                s = "治安";
                break;
            case "2":
                s = "刑侦";
                break;
            case "3":
                s = "禁毒";
                break;
            case "4":
                s = "网安";
                break;
            case "5":
                s = "经侦";
                break;
            case "6":
                s = "涉恐涉爆";
                break;
            case "7":
                s = "交通";
                break;
            case "8":
                s = "国保";
                break;
            case "9":
                s = "防范";
                break;
            default:
                s = "其他";
        }
        return s;
    }

    /**
     * 将document转化为list
     */
    private List<Object> document2List(Document document) {
        List<Object> result = new ArrayList<>();
        result.add(document.getString("personname"));
        result.add(document.getString("usernumber"));
        List<String> list = (List<String>) document.get("phone");
        StringBuilder stringBuilder = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    stringBuilder.append(list.get(i));
                } else {
                    stringBuilder.append("，").append(list.get(i));
                }
            }
        }
        result.add(stringBuilder.toString());
        result.add(document.getString("devicename"));
        List<Document> personKeyWords = document.get("personKeyWords", List.class);
        if (personKeyWords != null && personKeyWords.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Document personKeyWord : personKeyWords) {
                builder.append(personKeyWord.getString("name"));
                builder.append("，");
            }
            result.add(builder.toString());
        } else {
            result.add("其他");
        }
        result.add(document.getString("casename"));
        result.add(document.getString("casenumb"));
        Integer fetchPhone = document.getInteger("fetchPhone");
        result.add(fetchPhone == 1 ? "有" : "无");
        Integer wx = document.getInteger("wx");
        result.add(wx == 1 ? "有" : "无");
        Integer qq = document.getInteger("qq");
        result.add(qq == 1 ? "有" : "无");
        Integer gps = document.getInteger("gps");
        result.add(gps == 1 ? "有" : "无");
        result.add(document.getString("department_name"));
        String policeNubmer = document.getString("key_id");
        if (policeNubmer == null || "".equals(policeNubmer)) {
            result.add("上传未填警号");
        } else {
            result.add(policeNubmer);
        }
        result.add(new SimpleDateFormat("yyy-MM-dd hh:mm:ss").format(new Date(document.getLong("fetchtime"))));
        return result;
    }

    @Override
    public Map<String, Object> aggregateCaseDevicePersonInfo(String personKeyWord, String departmentCode, int pageNum, int pageSize, String wx,
                                                             String qq, String phone, String gps,
                                                             Long startTime, Long endTime)
    {
        if (StringUtils.isEmpty(departmentCode)) {
            throw new RuntimeException("部门编号不能为空！");
        }
        List<BasicDBObject> basicDBObjects = new ArrayList<>();
        List<BasicDBObject> basicDBObjectsCount = new ArrayList<>();
        getQueryCondition(personKeyWord, departmentCode, pageNum, pageSize, wx, qq, phone, gps, startTime, endTime, basicDBObjects, basicDBObjectsCount);

        //查询人员以及关联的采集统计
        AggregateIterable<Document> documents = userMongoDao.findInfoData2("infoData2", "t_person",
                basicDBObjects);

        Map<String, Object> result = new HashMap<>(2);

        //封装前台需要的数据结构
        encapsulateData(documents, result);

        //分组  用来统计总条数
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "").append(
                "count", new BasicDBObject("$sum", 1)));
        //添加分组 获取总条数
        basicDBObjectsCount.add(group);
        AggregateIterable<Document> count = userMongoDao.findInfoData2("infoData2", "t_person",
                basicDBObjectsCount);
        for (Document document : count) {
            result.put("count", document.get("count"));
            break;
        }
        return result;
    }

    private void encapsulateData(AggregateIterable<Document> documents, Map<String, Object> result) {
        //把查询的结果中加入案件信息 并装入集合中
        List<Document> res = new ArrayList<>();
        for (Document document : documents) {
            Document fetch = document.get("temp", Document.class);
            document.put("androidver", fetch.get("androidver"));
            document.put("brand", fetch.get("brand"));
            document.put("callLog", fetch.get("callLog"));
            document.put("collType", fetch.get("collType"));
            document.put("department_code", fetch.get("department_code"));
            document.put("department_name", getDepartName(fetch));
            document.put("devicename", fetch.get("devicename"));
            document.put("fetchtime", fetch.get("fetchtime"));
            document.put("gps", fetch.get("gps"));
            document.put("key_id", fetch.get("key_id"));
            document.put("model", fetch.get("model"));
            document.put("fetchPhone", fetch.get("phone"));
            document.put("shortMessage", fetch.get("shortMessage"));
            document.put("qq", fetch.get("qq"));
            document.put("type", fetch.get("type"));
            document.put("wx", fetch.get("wx"));

            String caseUnique = fetch.getString("case_unique");

            //如果还是没有关联的案件就从人员中拿取一个
            if (StringUtils.isEmpty(caseUnique)) {
                List list = document.get("caseuniquemark", List.class);
                caseUnique = list.get(0).toString();
            }
            //通过案件唯一标识查询案件数据
            BasicDBObject caseFilds = new BasicDBObject("caseuniquemark", 1).append("casename", 1).append("casenumb", 1);
            List<Document> deviceCases = userMongoDao.findInfoData2("infoData2", "t_case",
                    new BasicDBObject("caseuniquemark", caseUnique), caseFilds);
            if (deviceCases.size() > 0) {
                Document caseDoc = deviceCases.get(0);
                document.put("casename", caseDoc.get("casename"));
                document.put("casenumb", caseDoc.get("casenumb"));
                document.put("caseuniquemark", caseDoc.get("caseuniquemark"));
            }
            res.add(document);
        }
        result.put("detail", res);
    }

    private void getQueryCondition(String personKeyWord, String departmentCode, int pageNum, int pageSize, String wx,
                                   String qq, String phone, String gps, Long startTime, Long endTime,
                                   List<BasicDBObject> basicDBObjects, List<BasicDBObject> basicDBObjectsCount) {
        //如果有标签 先通过标签赛选t_person表
        BasicDBObject match = null;
        if (!StringUtils.isEmpty(personKeyWord)) {
            match = new BasicDBObject().append("$match", new BasicDBObject("personKeyWords.type_id", personKeyWord));
        }
        if ("9".equals(personKeyWord)) {
            match = new BasicDBObject().append("$match", new BasicDBObject("personKeyWords", new ArrayList<>()));
        }
        //按照设备唯一标识展开
        BasicDBObject unwind = new BasicDBObject("$unwind", "$device_unique");
        //通过设备唯一标识关联fetch表
        BasicDBObject lookup = new BasicDBObject("$lookup",
                new BasicDBObject("from", "fetchlog")
                        .append("localField", "device_unique")
                        .append("foreignField", "device_unique")
                        .append("as", "temp"));
        //并把多个结果拆分成多条数据
        BasicDBObject unwind1 = new BasicDBObject("$unwind", "$temp");
        BasicDBObject basicDBObject = new BasicDBObject();
        //然后再把查询条件进行筛选
        BasicDBObject query = new BasicDBObject();
        query.append("temp.department_code", Pattern.compile("^" + StringUtils.getOldDepartmentCode(departmentCode) + ".*$"));
        BasicDBObject timeQuery = new BasicDBObject();
        if (startTime != null) {
            timeQuery.append("$gte", startTime);
        }
        if (endTime != null) {
            timeQuery.append("$lt", endTime);
        }
        if (!timeQuery.isEmpty()) {
            query.append("temp.fetchtime", timeQuery);
        }
        BasicDBObject dbObject = new BasicDBObject();
        if (!StringUtils.isEmpty(wx)) {
            query.append("temp.wx", Integer.valueOf(wx));
            dbObject.append("temp.wx", Integer.valueOf(wx));
        }
        if (!StringUtils.isEmpty(qq)) {
            query.append("temp.qq", Integer.valueOf(qq));
            dbObject.append("temp.qq", Integer.valueOf(qq));
        }
        if (!StringUtils.isEmpty(phone)) {
            query.append("temp.phone", Integer.valueOf(phone));
            dbObject.append("temp.phone", Integer.valueOf(phone));
        }
        if (!StringUtils.isEmpty(gps)) {
            query.append("temp.gps", Integer.valueOf(gps));
            dbObject.append("temp.gps", Integer.valueOf(gps));
        }

        basicDBObject.append("$match", query);
        BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("temp.fetchtime", -1));
        BasicDBObject skip = new BasicDBObject("$skip", (pageNum - 1) * pageSize);
        BasicDBObject limit = new BasicDBObject("$limit", pageSize);
        //如果没有标签就不进行筛选
        if (match != null) {
            basicDBObjects.add(match);
            basicDBObjectsCount.add(match);
        }
        //添加筛选条件
        basicDBObjects.add(unwind);
        basicDBObjectsCount.add(unwind);
        basicDBObjects.add(lookup);
        basicDBObjectsCount.add(lookup);
        basicDBObjects.add(unwind1);
        basicDBObjectsCount.add(unwind1);
        basicDBObjects.add(basicDBObject);
        basicDBObjectsCount.add(basicDBObject);
        basicDBObjects.add(sort);
        if (pageSize > 0) {
            basicDBObjects.add(skip);
            basicDBObjects.add(limit);
        }
    }


    private String getDepartName(Document fetchData) {
        String departmentCode = fetchData.getString("department_code");
        //通过单位代码获取type
        Integer departmentType = StringUtils.getDepartmentType(departmentCode);
        String departmentName = fetchData.getString("department_name");
        //如果采集日志中没有单位名称  先从数据库查
        if(StringUtils.isEmpty(departmentName)){
            departmentName = businessDepartmentMapper.findDepartNameByDepartNum(departmentCode);
        }
        //如果还是没有那就是单位代码有问题
        if(StringUtils.isEmpty(departmentName)){
            return departmentName;
        }
        //如果不是四级就拼接部门名称
        StringBuilder stringBuilder = new StringBuilder();
        switch (departmentType) {
            case 2:
                //拼接获取第一级单位名称
                stringBuilder.append(
                        departmentMapper.findByDepartmentCode(
                                departmentCode.substring(0, 2) + "000000000"
                        ).getDepartmentName());

                break;
            case 3:
                //拼接获取第一级单位名称
                stringBuilder.append(
                        departmentMapper.findByDepartmentCode(
                                departmentCode.substring(0, 2) + "000000000"
                        ).getDepartmentName());
                //获取第二级单位名称
                stringBuilder.append(
                        departmentMapper.findByDepartmentCode(
                                departmentCode.substring(0, 4) + "0000000"
                        ).getDepartmentName());
                //如果是第一级单位 或者第四级单位 不进行处理
            default:
                break;
        }
        //拼接最后一级单位在返回
        return stringBuilder.append(departmentName).toString();
    }

    /**
     * 给定一个时间戳 返回时间戳一天前的时间戳
     */
    private Long getDate(long s) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(s));
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTime().getTime();
    }

    /**
     * 给定一个时间戳 返回时间戳一个月前的时间戳
     */
    private Long getMonth(String s) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyy-MM").parse(s));
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime().getTime();
    }
}
