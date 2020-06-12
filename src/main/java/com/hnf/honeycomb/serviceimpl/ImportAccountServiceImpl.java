package com.hnf.honeycomb.serviceimpl;

import com.hnf.honeycomb.accountUpload.TaskQueue;
import com.hnf.honeycomb.bean.enumerations.AccountEnum;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.service.ImportAccountService;
import com.hnf.honeycomb.util.MultipartFileToFileUtil;
import com.hnf.honeycomb.util.RedisUtilNew;
import com.hnf.honeycomb.util.StringUtils;
import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Service
public class ImportAccountServiceImpl implements ImportAccountService {

    @Resource
    private RedisUtilNew redisUtilNew;

    private final DeviceMongoDao deviceMongoDao;

    public ImportAccountServiceImpl(DeviceMongoDao deviceMongoDao) {
        this.deviceMongoDao = deviceMongoDao;
    }


    @Override
    public void fileUpload(List<MultipartFile> mfiles, Integer fileType, String personId, String idNumber) {
        //MultipartFile转file
        List<File> fileList = new ArrayList<>();
        try {
            for (MultipartFile mfile : mfiles) {
                File file = MultipartFileToFileUtil.multipartFileToFile(mfile);
                fileList.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //更新缓存
        redisUtilNew.remove(DeviceServiceImpl.redisPersonName);
        Date time = Calendar.getInstance().getTime();
        //存入log
        mfiles.forEach(r -> {
            Document logDoc = new Document("filename", r.getOriginalFilename());
            logDoc.append("time", time);
            logDoc.append("idNumber", idNumber);
            logDoc.append("state", "正在导入");
            deviceMongoDao.insertDocument("accountdata", "accountlogs", logDoc);
        });
        //开启任务队列 处理账单
        TaskQueue taskQueue = TaskQueue.getInstance();
        taskQueue.setDeviceMongoDao(deviceMongoDao);
        taskQueue.setFileType(fileType);
        taskQueue.setUploadTime(time);
        if (!StringUtils.isEmpty(personId)) {
            taskQueue.setPersonId(personId);
        }
        taskQueue.startTask(fileList);
    }


    @Override
    public Map<String, Object> findAccount(String personId, Integer type, String fileState, Integer pageSize, Integer pageNum) {
        //查询用户下面 类型-文件名-state
        BasicDBObject id_fileQuery = new BasicDBObject();
        id_fileQuery.append("personId", personId);
        id_fileQuery.append("fileType", type);
        List<Document> personFileDocList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("accountdata", "personId_file", id_fileQuery);

        //获取和state绑定的 fileName的订单号
        ArrayList<String> fileNameList = new ArrayList<>();
        personFileDocList.forEach(r -> {
            fileNameList.add(r.getString("fileName"));
        });
        BasicDBObject orderQuery = new BasicDBObject();
        orderQuery.append("fileName", new BasicDBObject("$in", fileNameList));
        orderQuery.append("fileState", fileState);
        List<Document> fileOrderDocList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("accountdata", "file_order", orderQuery);

        //返回明细
        BasicDBObject detailQuery = null;
        if (!AccountEnum.YINGHANG.getValue().equals(type)) {
            List<String> orderDocList = new ArrayList<>();
            fileOrderDocList.forEach(r -> {
                orderDocList.add(r.getString("business_order"));
            });
            detailQuery = new BasicDBObject("business_order", new BasicDBObject("$in", orderDocList));
        } else {
            detailQuery = new BasicDBObject("bankcarnum", fileState);
        }
        BasicDBObject sort = new BasicDBObject("business_time", -1);
        List<Document> detailDocList = deviceMongoDao.findInfoByGatherNameAndQuery("accountdata", "accountdetail", detailQuery, sort, pageNum, pageSize);
        Long aLong = deviceMongoDao.countByGatherNameAndDBNameAndQuery("accountdata", "accountdetail", detailQuery);


        HashMap<String, Object> map = new HashMap<>();
        map.put("count", aLong);
        map.put("data", detailDocList);
        return map;
    }

    /**
     * 根据用户id 查询账单账号列表
     * 银行卡号  微信昵称  支付宝手机号
     *
     * @param personId
     * @param fileType
     * @return
     */
    @Override
    public List<Document> showState(String personId, Integer fileType) {
        BasicDBObject personDoc = new BasicDBObject("personId", personId);
        personDoc.append("fileType", fileType);
        List<Document> personFileList = deviceMongoDao.findInfoByDBNameAndGatherNameAndQuery("accountdata", "personId_file", personDoc);

        List<String> fileNameList = new ArrayList<>();
        personFileList.forEach(r -> {
            fileNameList.add(r.getString("fileName"));
        });

        BasicDBObject fileQuery = new BasicDBObject("fileName", new BasicDBObject("$in", fileNameList));
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$fileState"));
        List<Document> stateDocList = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery(
                "accountdata", "file_order", Arrays.asList(new BasicDBObject("$match", fileQuery), group));
        return stateDocList;
    }

    @Override
    public Object group(String personId, Integer fileType) {
        BasicDBObject personId1 = new BasicDBObject("personId", personId);
        BasicDBObject group = new BasicDBObject("$group", new BasicDBObject("_id", "$fileType"));
        List<Document> documents = deviceMongoDao.aggregateByGatheNameAndDBNameAndQuery("accountdata", "personId_file",
                Arrays.asList(new BasicDBObject("$match", personId1), group));
        return documents;
    }

    @Override
    public Map<String, Object> showLogs(Integer pageNum, Integer pageSize, String idNumber) {
        BasicDBObject idQuery = new BasicDBObject("idNumber", idNumber);
        Map<String, Object> map = new HashMap<>();
        BasicDBObject sort = new BasicDBObject("time", -1);
        List<Document> doc = deviceMongoDao.findInfoByGatherNameAndQuery("accountdata", "accountlogs", idQuery, sort, pageNum, pageSize);
        Long aLong = deviceMongoDao.countByGatherNameAndDBNameAndQuery("accountdata", "accountlogs", idQuery);
        map.put("result", doc);
        map.put("count", aLong);
        return map;
    }

    public static void main(String[] args) {
        Date time = Calendar.getInstance().getTime();
        System.out.println(time);

    }
}
