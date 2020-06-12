package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.ImportAccountService;
import com.hnf.honeycomb.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;
@RestController
@RequestMapping("importAccount")
public class ImportAccountController extends AbstractController {

    @Autowired
    private ImportAccountService importAccountService;

    /**
     * 账单上传入库
     *
     * @param files    上传的账单列表
     * @param fileType 文件类型
     */
    @RequestMapping(value = "fileUpload", produces = {"application/json;charset=UTF-8"})
    public void fileUpload(List<MultipartFile> files, Integer fileType, String personId,String idNumber) {
        importAccountService.fileUpload(files, fileType, personId,idNumber);
    }

    /**
     * 展示用户账单明细
     * @param map   personid 用户身份证 filetype 文件类型  filestate  文件标记(银行卡号,微信昵称,支付宝手机号)
     * @return
     */
    @RequestMapping(value = "showAccount",produces = {"application/json;charset=UTF-8"})
    public JsonResult findAccount(@RequestBody Map<String,String> map) {
        return new JsonResult<>(importAccountService.findAccount(getString(map.get("personId")),
                getInteger(map.get("fileType")),getString(map.get("fileState")),getInteger(map.get("pageSize")),
                getInteger(map.get("pageNum"))));
    }

    /**
     * 展示账单state
     * @param map   personId 用户身份证  fileType 文件类型
     * @return
     */
    @RequestMapping(value = "showState",produces =  {"application/json;charset=UTF-8"})
    public JsonResult showState(@RequestBody Map<String,Object> map){
        return new JsonResult<>(importAccountService.showState(getString(map.get("personId")), getInteger(map.get("fileType"))));
    }

    @RequestMapping(value = "group",produces =  {"application/json;charset=UTF-8"})
    public JsonResult group(@RequestBody Map<String,Object> map){
        return new JsonResult<>(importAccountService.group(getString(map.get("personId")), getInteger(map.get("fileType"))));
    }

    /**
     * 展示导入日志
     *
     * @param map pageNum 页码 pageSize 每页大小  idNumber 当前登录用户唯一标识
     * @return
     */
    @RequestMapping(value = "showLogs",produces =  {"application/json;charset=UTF-8"})
    public JsonResult showLogs(@RequestBody Map<String,Object> map){
        return new JsonResult<>(importAccountService.showLogs(getInteger(map.get("pageNum")),
                getInteger(map.get("pageSize")),getString(map.get("idNumber"))));
    }


}
