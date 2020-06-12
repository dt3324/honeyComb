package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.DeviceInfoService;
import com.hnf.honeycomb.util.JsonResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;


/**
 * 用于设备数据详情页面的查询
 *
 * @author yy
 */
@RestController
@RequestMapping("deviceInfo")
public class DeviceInfoController extends AbstractController {

    @Resource
    private DeviceInfoService deviceInfoService;

    /**
     * 设备唯一标识查询人员
     */
    @RequestMapping(value = "findRelationPersonsByDeviceUnique"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findRelationPersonsByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findRelationPersonsByDeviceUnique(
                        getString(map.get("deviceUnique"))
                )
        );
    }

    /**
     * 设备唯一标识查询案件
     */
    @RequestMapping(value = "findRelationCasesByDeviceUnique"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findRelationCasesByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findRelationCasesByDeviceUnique(
                        getString(map.get("deviceUnique"))
                )
        );
    }

    /**
     * 设备唯一标识查询设备相关信息
     */
    @RequestMapping(value = "findDeviceInfoByDeviceUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findDeviceInfoByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findDeviceInfoByDeviceUnique(getString(map.get("deviceUnique")))
        );
    }

    /**
     * 设备唯一标识查询通讯录
     */
    @RequestMapping(value = "findContectInfoByDeviceUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findContectInfoByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findContactInfoByDeviceUnique(
                        getString(map.get("deviceUnique"))
                        , getInteger(map.get("page"))
                )
        );
    }

    /**
     * 设备唯一标识查询短消息详情
     */
    @RequestMapping(value = "findMsgCountByDeviceUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findMsgCountByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findMsgCountByDeviceUnique(
                        getString(map.get("deviceUnique"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                ));
    }

    /**
     * 查询通话记录详情
     */
    @RequestMapping(value = "findRecordCallByDeviceUnqiue", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findRecordCallByDeviceUnqiue(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findRecordCallByDeviceUnique(
                        getString(map.get("deviceUnique"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                ));
    }

    /**
     * 查询两个人之间的短消息详情
     */
    @RequestMapping(value = "findOne2OneMsgByDeviceUnqiueAndPhone"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findOne2OneMsgByDeviceUnqiueAndPhone(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findOne2OneMsgByDeviceUniqueAndPhone(
                        getString(map.get("deviceUnique"))
                        , getString(map.get("telePhone"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), 100)
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询一对一通话记录
     */
    @RequestMapping(value = "findOne2OneRecordByDeviceUnqiueAndPhone", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findOne2OneRecordByDeviceUnqiueAndPhone(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findOne2OneRecordByDeviceUniqueAndPhone(
                        getString(map.get("deviceUnique"))
                        , getString(map.get("phone"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询qq号下面的所有好友及群列表
     */
    @RequestMapping(value = "findQQUserDetailByQQUin", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findQQUserDetailByQQUin(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findQQUserDetailByQQUin(
                        getString(map.get("qqUserUin"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询微信号下面的所有好友及群列表
     */
    @RequestMapping(value = "findWXUserDetailByWXUin", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findWXUserDetailByWXUin(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findWXUserDetailByWXUin(
                        getString(map.get("wxUserName"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                ));
    }

    /**
     * 查询wx好友与好友之间的聊天信息
     */
    @RequestMapping(value = "findOne2OneWXFriendMsgByTwoWXUserName", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findOne2OneWXFriendMsgByTwoWXUserName(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findOne2OneWXFriendMsgByTwoWXUserName(
                        getString(map.get("wxUserName"))
                        , getString(map.get("wxFriendName"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), 100)
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询qq好友聊天账号
     */
    @RequestMapping(value = "findOne2OneQQFriendMsgByTwoQQUin"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findOne2OneQQFriendMsgByTwoQQUin(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findOne2OneQQFriendMsgByTwoQQUin(
                        getString(map.get("qqUserUin"))
                        , getString(map.get("qqFriendUin"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), 100)
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询wx群聊天信息
     */
    @RequestMapping(value = "findWXTroopUinMsgByWXTroopUin"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findWXTroopUinMsgByWXTroopUin(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findWXTroopUinMsgByWXTroopUin(
                        getString(map.get("wxTroopUin"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), 100)
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 查询qq群聊天详情
     */
    @RequestMapping(value = "findQQTroopUinMsgByQQTroopUin"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findQQTroopUinMsgByQQTroopUin(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceInfoService.findQQTroopUinMsgByQQTroopUin(
                        getString(map.get("qqTroopUin"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), 100)
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

}
