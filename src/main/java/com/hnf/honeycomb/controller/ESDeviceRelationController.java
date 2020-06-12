package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.EsDeviceService;
import com.hnf.honeycomb.util.JsonResult;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 一键搜中相关于设备的方法
 *
 * @author yy
 */
@RestController
public class ESDeviceRelationController extends AbstractController {

    @Resource
    private EsDeviceService esDeviceService;

    /**
     * //案件唯一标识相关联的人与设备
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "searchCaseInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> findRelationDeviceAndPersonInfoByCaseUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findRelationDeviceAndPersonInfoByCaseUnqiue(
                        getString(map.get("caseUniqueMark"))
                ));
    }

    /**
     * //人员查询相关联案件及设备
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "searchPersonInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> findRelationDeviceAndCaseInfoByPersonUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findRelationDeviceAndCaseInfoByPersonUnqiue(
                        getString(map.get("userNumber"))
                ));
    }

    /**
     * //查询聊天信息上下文
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findByMessageInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findMessageContextByOneMsgInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findMessageContextByOneMsgInfo(
                        getString(map.get("deviceUnique"), "")
                        , getString(map.get("phone"), "")
                        , getString(map.get("unique"), "")
                ));
    }


    /**
     * //查询qq用户具体信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "searchQQInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findQQUserInfoByQQNumber(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findQQUserInfoByQQNumber(
                        getString(map.get("uin"))
                ));
    }

    /**
     * //查询wx用户具体信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "searchWXInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findWXUserInfoByWXNumber(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findWXUserInfoByWXNumber(
                        getString(map.get("uin"))
                ));
    }

    /**
     * //查询QQ用户具体信息
     */
    @RequestMapping(value = "searchQQTroopInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> findQQTroopInfoByQQTroopNumber(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findQQTroopInfoByQQTroopNumber(
                        getString(map.get("troopUin"))
                ));
    }

    /**
     * //查询wx用户具体信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "searchWXTroopInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> findWXTroopInfoByWXTroopNumber(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findWXTroopInfoByWXTroopNumber(
                        getString(map.get("troopUin"))
                ));
    }

    /**
     * //查询QQ好友上下文
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findByQqFriendMessage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findQQFriendMsgContextByOneMsgInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findQQFriendMsgContextByOneMsgInfo(
                        getString(map.get("qqUserUin"))
                        , getString(map.get("qqFriendUin"))
                        , getString(map.get("unique"))
                ));
    }

    /**
     * //查询WX好友上下文
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findByWxFriendMessage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findWXFriendMsgContextByOneMsgInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findWXFriendMsgContextByOneMsgInfo(
                        getString(map.get("wxUserName"))
                        , getString(map.get("wxFriendName"))
                        , getString(map.get("unique"))
                ));
    }


    /**
     * //查询QQ群上下文
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "findByQqTroopMessage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findQQTroopMsgContextByOneMsgInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findQQTroopMsgContextByOneMsgInfo(
                        getString(map.get("qqTroopUin"))
                        , getString(map.get("unique"))
                ));
    }

    /**
     * //查询WX群上下文
     *
     * @return
     */
    @RequestMapping(value = "findByWxTroopMessage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Document>> findWXChatroomMsgContextByOneMsgInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findWXChatroomMsgContextByOneMsgInfo(
                        getString(map.get("wxTroopUin"))
                        , getString(map.get("unique"))
                ));
    }

    /**
     * //查询对应的电话号码
     */
    @RequestMapping(value = "findNumInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<List<Map<String, Object>>> findNumInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                esDeviceService.findNumInfo(
                        getString(map.get("searchNumber"))
                        , getString(map.get("searchType"))
                ));
    }
//
//    /**
//     * //通过QQ或者微信号查询对应人员的设备和案件信息
//     */
//    @RequestMapping(value = "findInfoByQqOrWx", produces = {"application/json;charset=UTF-8"})
//    public JsonResult<Object> findInfoByQqOrWx(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                esDeviceService.findInfoByQqOrWx(
//                        getString(map.get("uin"))
//                ));
//    }


}
