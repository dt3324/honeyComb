package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.dao.GisBaseMongoDao;
import com.hnf.honeycomb.service.GisDeviceService;
import com.hnf.honeycomb.util.CollectionUtils;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author admin
 */
@Service
public class GisDeviceServiceImpl implements GisDeviceService {

    @Resource
    private GisBaseMongoDao gisBaseMongoDao;


    @Override
    public Map<String, HashSet<String>> findImeiAndMacByNumAndType(String num, String type) {
        Map<String, HashSet<String>> result = new HashMap<>(3);
        HashSet<String> imeis = new HashSet<>();
        HashSet<String> macs = new HashSet<>();
        BasicDBObject query = new BasicDBObject();
        //对应的查询条件
        switch (type) {
            case "IMEI":
                //获取对应的RMI
                imeis.add(num.trim());
                result.put("imei", imeis);
                break;
            case "MAC":
                //获取对应的mac
                result.put("mac", macs);
                macs.add(num.trim());
                break;
            case "qq":
                //通过QQ号查询对应的imei及mac
                query.append("uin", num.trim());
                List<Document> qqs = gisBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                        "infoData2", "t_qquser", query);
                if (CollectionUtils.isEmpty(qqs)) {
                    break;
                }
                result = this.findImeiAndMacByDeviceUnqiue(qqs);
                break;
            case "phone":
                //通过手机号查询对应的IMEI及MAC
                query.append("phone", num.trim());
                List<Document> persons = gisBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                        "infoData2", "t_person", query);
                if (CollectionUtils.isEmpty(persons)) {
                    break;
                }
                result = this.findImeiAndMacByDeviceUnqiue(persons);
                break;
            case "wx":
                //通过WX查询对应的IMEI及MAC
                query.append("username", num.trim());
                List<Document> wxs = gisBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                        "infoData2", "t_wxuser", query);
                if (CollectionUtils.isEmpty(wxs)) {
                    break;
                }
                result = this.findImeiAndMacByDeviceUnqiue(wxs);
                break;
            case "deviceUnique":
                //通过设备device_unique查询对应的IMEI及MAC
                List<String> strings = Arrays.asList(num.trim());
                ArrayList<Document> documents = new ArrayList<>();
                for (String string : strings) {
                    Document document = new Document();
                    document.append("device_unique", Arrays.asList(string));
                    documents.add(document);
                }
                result = findImeiAndMacByDeviceUnqiue(documents);
                break;
            default:
                throw new RuntimeException("对应的搜索类型错误");
        }
        return result;
    }


    @Override
    public Map<String, HashSet<String>> findImeiAndMacByDeviceUnqiue(List<Document> qqs) {

        HashMap<String, HashSet<String>> result = new HashMap<>(3);
        List<String> deviceUniques = new ArrayList<>();
        if (CollectionUtils.isEmpty(qqs)) {
            return result;
        }
        qqs.forEach(t -> {
            //将deviceUniques 转化成String 格式的数据，并加入data 集合
            List devices = (List)t.get("device_unique");
            for (Object obj : devices) {
                deviceUniques.add(obj.toString());
            }
        });
        if (CollectionUtils.isEmpty(deviceUniques)) {
            return result;
        }
        HashSet<String> imeis = new HashSet<>();
        HashSet<String> macs = new HashSet<>();
        deviceUniques.forEach(t -> {
            BasicDBObject query = new BasicDBObject();
            query.append("device_unique", t.trim());
            List<Document> devices = gisBaseMongoDao.findInfoByDBNameAndGatherNameAndQuery(
                    "infoData2", "t_device", query);
            if (!CollectionUtils.isEmpty(devices)) {
                devices.forEach(t1 -> {
                    imeis.add(t1.getString("imei"));
                    macs.add(t1.getString("wifi_mac"));
                });
            }
        });
        result.put("imei", imeis);
        result.put("mac", macs);
        return result;
    }


    @Override
    public HashSet<String> findLikeNumberInfo(String number, String type) {
        if (StringUtils.isEmpty(number)) {
            throw new RuntimeException("对应搜索的账号为null");
        }
        if (StringUtils.isEmpty(type)) {
            throw new RuntimeException("对应的账号类型为null");
        }
        HashSet<String> result = new HashSet<>();
        //结果
        number = number.trim();
        type = type.trim();
        BasicDBObject query = new BasicDBObject();
        //查询条件
        List<Document> docs;
        switch (type) {
            case "qq":
                Pattern qqPattern = Pattern.compile("^" + number + ".*$", Pattern.CASE_INSENSITIVE);
                query.append("uin", qqPattern);
                docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData2", "t_qquser", query, null, 1, 10);
                for (Document doc : docs) {
                    result.add(doc.getString("uin"));
                }
                break;
            case "wx":
                Pattern wxPattern = Pattern.compile("^" + number + ".*$", Pattern.CASE_INSENSITIVE);
                query.append("username", wxPattern);
                docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData2", "t_wxuser", query, null, 1, 10);
                for (Document doc : docs) {
                    result.add(doc.getString("username"));
                }
                break;
            case "phone":
                //不用去查通讯录,因为其没有Gis数据
                Pattern pattern = Pattern.compile("^" + number.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                query.append("phone", pattern);
                docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData2", "t_person", query, null, 1, 10);
                if (!CollectionUtils.isEmpty(docs)) {
                    result = this.getPhoneFromPerson(docs, number);
                }
                break;
            case "IDNumber":
                //类型为身份证号
                break;
            case "IMEI":
                Pattern imeiPattern =
                        Pattern.compile("^" + number.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                query.append("imei", imeiPattern);
                docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData2", "t_device", query, null, 1, 10);
                for (Document doc : docs) {
                    result.add(doc.getString("imei"));
                }
                break;
            case "MAC":
                Pattern macPattern =
                        Pattern.compile("^" + number.trim() + ".*$", Pattern.CASE_INSENSITIVE);
                query.append("wifi_mac", macPattern);
                docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                        "infoData2", "t_device", query, null, 1, 10);
                for (Document doc : docs) {
                    result.add(doc.getString("wifi_mac"));
                }
                break;
            default:
                throw new RuntimeException("对应的搜索类型错误");
        }
        return result;
    }

    /**
     * 通过对应的从MongoDB集合中获取对应的电话号码集合
     *
     * @param persons persons
     * @return 返回获取的电话号码
     */
    private HashSet<String> getPhoneFromPerson(List<Document> persons, String search) {
        HashSet<String> phones = new HashSet<>();
        if (persons == null || persons.isEmpty()) {
            return phones;
        }
        persons.forEach(t -> {
            Object phoneObj = t.get("phone");
            ArrayList<?> phoneList = (ArrayList) phoneObj;
            if (phoneList != null && !phoneList.isEmpty()) {
                phoneList.forEach(phone -> {
                    if (phone.toString().startsWith(search)) {
                        phones.add(phone.toString());
                    }
                });
            }
        });
        return phones;
    }

    @Override
    public void test() {
        BasicDBObject query = new BasicDBObject();
        Pattern imeiPattern =
                Pattern.compile("^" + "WX102895539".trim(), Pattern.CASE_INSENSITIVE);
        query.append("username", imeiPattern);
        Long time1 = System.currentTimeMillis();
        System.out.println(">>>>>>>>>>>>>>>>>>>>query:" + query);
        List<Document> docs = gisBaseMongoDao.findInfoByGatherNameAndQuery(
                "infoData2", "t_wxuser", query, null, 1, 2);
        Long time2 = System.currentTimeMillis();
        System.out.println("耗时:" + (time2 - time1));
        docs.forEach(t -> System.out.println("t:" + t));
    }


}
