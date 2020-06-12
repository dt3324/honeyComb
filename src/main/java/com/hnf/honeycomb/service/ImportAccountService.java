package com.hnf.honeycomb.service;

import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ImportAccountService {


    void fileUpload(List<MultipartFile> files, Integer fileType, String personId,String idNumber);

    Map<String, Object> findAccount(String personId, Integer type, String state, Integer pageSize, Integer pageNum);

    List<Document> showState(String personId, Integer fileType);

    Object group(String personId, Integer fileType);

    Map<String, Object> showLogs(Integer pageNum, Integer pageSize,String idNumber);
}
