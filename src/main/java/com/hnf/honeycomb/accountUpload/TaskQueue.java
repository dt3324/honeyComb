package com.hnf.honeycomb.accountUpload;

import com.hnf.honeycomb.dao.DeviceMongoDao;

import javax.xml.crypto.Data;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueue {
    private final Integer TRHREAD_NUM = Runtime.getRuntime().availableProcessors();

    private Integer fileType;

    private DeviceMongoDao deviceMongoDao;

    private String personId;

    private Date uploadTime;

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
    //创建锁

    public void setDeviceMongoDao(DeviceMongoDao deviceMongoDao) {
        this.deviceMongoDao = deviceMongoDao;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    private AtomicInteger index = new AtomicInteger();

    private ThreadPoolExecutor threadPoolExecutor;
    //任务队列
    private ConcurrentLinkedQueue<FileDetail> blockingQueue = new ConcurrentLinkedQueue<>();
    //初始化
    private static final TaskQueue INSTANCE = new TaskQueue();

    public static TaskQueue getInstance(){
        INSTANCE.initalize();
        return INSTANCE;
    }

    private void initalize() {
        threadPoolExecutor  = new ThreadPoolExecutor(TRHREAD_NUM,
                TRHREAD_NUM * 2,
                1000,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(10),
                runnable -> {
                    Thread thread = new Thread(runnable, "账单导入线程" + index.getAndIncrement());
                    thread.setDaemon(false);
//                    System.out.println("创建线程"+thread.getName());
                    return thread;},
                new ThreadPoolExecutor.CallerRunsPolicy());

    }

    //创建任务
    private void makeTask(List<File> fileList){
        for (File file : fileList) {
            FileDetail fileDetail = new FileDetail(file,fileType,personId);
            fileDetail.setDeviceMongoDao(deviceMongoDao);
            fileDetail.setUploadTime(uploadTime);
            blockingQueue.offer(fileDetail);
        }
    }

    public void startTask(List<File> fileList){
        makeTask(fileList);
        //分发任务
        for (int i = 0; i < fileList.size(); i++) {
            threadPoolExecutor.submit(blockingQueue.poll());
        }
    }

}
