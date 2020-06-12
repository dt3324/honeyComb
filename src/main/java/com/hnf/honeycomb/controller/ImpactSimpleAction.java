package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.ImpactSimpleService;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.util.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;


/**
 * 新碰撞关系的相关接口
 *
 * @author yy
 */
@RestController
public class ImpactSimpleAction extends AbstractController {
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static Logger logger = LoggerFactory.getLogger(ImpactSimpleAction.class);

    @Resource
    private ImpactSimpleService impactSimpleService;

    @Resource
    private JwtService jwtService;

    /**
     * 通过的节点相关信息查询对应的节点信息
     */
    @RequestMapping("/findNodeMsgByNodeDetail")
    public JsonResult<Object> findNodeMsgByNodeDetail(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.findNodeMsgByNodeDetail(
                        getInteger(map.get("nodeType"))
                        , getString(map.get("deviceUniques"))
                        , getString(map.get("searchNum"))
                        , getInteger(map.get("searchType"))
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("timeSelectType"))
                ));
    }

//    @RequestMapping("/impactByStraightPhone")
//    public JsonResult<Map<String, Object>> impactByStraightRelation(HttpServletRequest request, @RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                impactSimpleService.impactByPhoneNumbers(
//                        getString(map.get("deviceUniques"))
//                        , getInteger(map.get("userId"))
//                        , request.getRemoteAddr()
//                        , getLong(map.get("startTime"))
//                        , getLong(map.get("endTime"))
//                        , getInteger(map.get("countLimit"))
//                        , getInteger(map.get("timeSelectType"))
//                ));
//    }

    /**
     * 碰撞单位下所有设备
     */
    @RequestMapping("/impactByDepartment")
    public JsonResult<Map<String, Object>> impactByDepartment(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByDepartment(
                        getString(map.get("department")),
                        getString(map.get("type")),
                        getInteger(map.get("userId")),
                        place,
                        getLong(map.get("startTime")),
                        getLong(map.get("endTime")),
                        getInteger(map.get("countLimit")),
                        getInteger(map.get("timeSelectType"))
                ));
    }

    /**
     * 新的碰撞qq共同好友
     */
    @RequestMapping("/impactByQQ")
    public JsonResult<Map<String, Object>> impactByQQ(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByQQNumbers(getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                ));
    }

    /**
     * 新的碰撞QQ群
     */
    @RequestMapping("/impactByQQTroop")
    public JsonResult<Map<String, Object>> impactByQQTroop(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByQQtroop(
                        getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                ));
    }

    /**
     * 新的碰撞wx好友
     */
    @RequestMapping("/impactByWX")
    public JsonResult<Map<String, Object>> impactByWX(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByWXNumbers(
                        getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                )
        );
    }

    /**
     * 新的碰撞wx群
     */
    @RequestMapping("/impactByWXTroop")
    public JsonResult<Map<String, Object>> impactByWXTroop(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByWXtroop(
                        getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                )
        );
    }

    /**
     * 新的碰撞不是通讯录好友
     */
    @RequestMapping("/impactByPhoneIsNotFriend")
    public JsonResult<Map<String, Object>> impactByPhoneIsNotFriend(@RequestBody Map<String, Object> map, HttpServletRequest request) {

        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByPhoneIsNotFriend(
                        getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                )
        );
    }

    /**
     * 新的碰撞通讯录
     */
    @RequestMapping("/impactByPhoneIsFriend")
    public JsonResult<Map<String, Object>> impactByPhoneIsFriend(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                impactSimpleService.impactByPhoneAndIsFriend(
                        getString(map.get("deviceUniques"))
                        , getInteger(map.get("userId"))
                        , "", "", place
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getInteger(map.get("countLimit"))
                        , getInteger(map.get("timeSelectType"))
                ));
    }

//    /**
//     * 新增碰撞业务
//     */
//    @RequestMapping("/insertImpact")
//    public JsonResult<Long> insertImpact(@RequestBody Map<String, Object> map) throws Exception {
//        return new JsonResult<>(
//                impactSimpleService.insertImpactHistory(
//                        getInteger(map.get("userId"))
//                        , getString(map.get("type"))
//                        , getString(map.get("deviceUnique"))
//                        , getString(map.get("time"))
//                        , getString(map.get("project"))
//                        , getString(map.get("explain"))
//                ));
//    }


//    /**
//     * 查询碰撞业务
//     */
//    @RequestMapping("/findImpact")
//    public JsonResult<List<Document>> findImpact(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                impactSimpleService.findImpactHistoryByUserId(
//                        getInteger(map.get("userId"))
//                        , getString(map.get("project"))
//                        , getString(map.get("deviceName"))
//                        , getInteger(map.get("page"))
//                        , getInteger(map.get("pageSize"))
//                ));
//    }

//    /**
//     * 通过业务唯一编码查询关联业务的设备信息
//     */
//    @RequestMapping("/findProjectName")
//    public JsonResult<List<Document>> findProjectName(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                impactSimpleService.findImpactProjectName(
//                        getInteger(map.get("userId"))
//                ));
//    }

//    /**
//     * 通过业务唯一编码查询关联业务的设备信息
//     */
//    @RequestMapping("/findImpactHistoryByUnique")
//    public JsonResult<Document> findImpactHistoryByUnique(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                impactSimpleService.findImpactHistoryByUnique(
//                        getString(map.get("impactUnique"))
//                ));
//    }

    /**
     * 修改碰撞业务
     */
    @RequestMapping("/updateImpact")
    public JsonResult<Integer> updateImpact(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(impactSimpleService.updateImpactHistory(
                getString(map.get("unique"))
                , getString(map.get("explain"))
                , getString(map.get("project"))
                , getString(map.get("deviceUnique"))
        ));
    }

    /**
     * 删除碰撞业务
     */
    @RequestMapping("/deleteImpact")
    public JsonResult<Integer> deleteImpact(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.deleteImpactHistory(
                        getString(map.get("unique"))
                ));
    }

//    /**
//     * 查询账号对应的好友（群）信息
//     */
//    @RequestMapping("/findFriendList")
//    public JsonResult<Map> findFriendList(@RequestBody Map<String, Object> map) throws Exception {
//        return new JsonResult<>(
//                impactSimpleService.findFriendList(
//                        getInteger(map.get("type"))
//                        , getString(map.get("uin"))
//                        , getString(map.get("startDate"))
//                        , getString(map.get("endDate"))
//                        , getString(map.get("searchContent"))
//                ));
//    }

    /**
     * 查询账号对应的好友（群）信息详情
     */
    @RequestMapping("/findMsgDetails")
    public JsonResult<Map> findMsgDetails(@RequestBody Map<String, Object> map) throws Exception {
        return new JsonResult<>(
                impactSimpleService.findMsgDetails(
                        getInteger(map.get("type"))
                        , getInteger(map.get("pageNumber"))
                        , getInteger(map.get("pageSize"), DEFAULT_PAGE_SIZE)
                        , getString(map.get("uin"))
                        , getString(map.get("fuin"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                        , getString(map.get("searchContent"))
                        , getInteger(map.get("timeSelectType"))
                ));
    }

//    /**
//     * 查询设备碰撞业务
//     */
//    @RequestMapping("/findImpactHistory")
//    public JsonResult<Object> findImpactHistory(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                impactSimpleService.findImpactHistory(
//                        getInteger(map.get("userId"))
//                        , getString(map.get("project"))
//                        , getString(map.get("deviceName"))
//                        , getInteger(map.get("page"))
//                        , getInteger(map.get("pageSize"))
//                ));
//    }

    /**
     * 添加碰撞设备存入REDIS（新）
     */
    @RequestMapping("/impactAddDevices")
    public JsonResult<Object> impactAddDevices(@RequestBody Map<String, Object> map) {
        logger.debug("进入服务端");
        String aString = impactSimpleService.impactAddDevices(
                getString(map.get("policeNumber"))
                , getString(map.get("deviceUnique"))
                , getString(map.get("personName"))
                , getString(map.get("idNumber"))
                , getString(map.get("departmentName")));
        logger.debug("aString:" + aString);
        return new JsonResult<>(
                aString);
    }

    /**
     * 查询REDIS中的碰撞设备信息
     */
    @RequestMapping("/impactfindDevices")
    public JsonResult<Object> impactfindDevices(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.impactfindDevices(
                        getString(map.get("policeNumber"))
                ));
    }

    /**
     * 删除某人下面的碰撞设备信息
     */
    @RequestMapping("/impactdeleteDeviceByName")
    public JsonResult<Object> impactdeleteDeviceByName(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.impactdeleteDeviceByName(
                        getString(map.get("policeNumber"))
                        , getString(map.get("personName"))
                        , getString(map.get("idNumber"))
                        ,getString(map.get("departmentName"))
                ));
    }

    /**
     * 删除所有的碰撞设备列表，即清空设备购物车
     */
    @RequestMapping("/impactdeleteDevices")
    public JsonResult<Object> impactdeleteDevices(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.impactdeleteDevices(
                        getString(map.get("policeNumber"))
                ));
    }

    /**
     * 通过案件或者人员查询设备
     */
    @RequestMapping("/findDevice")
    public JsonResult<Object> findDevice(@RequestBody Map<String, Object> map,HttpServletRequest request) {
        String token = request.getHeader("token");
        String departmentCode = jwtService.parseJWT(token).get("unit").toString();
        logger.debug("进入服务端调用！");
        return new JsonResult<>(
                impactSimpleService.findDevice(
                        getString(map.get("caseUniqueMark"))
                        , getString(map.get("userNumber"))
                        ,departmentCode
                ));
    }

    /**
     * 新增设备碰撞业务（新）
     */
    @RequestMapping("/insertImpactHistoryNew")
    public JsonResult<Object> insertImpactHistoryNew(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.insertImpactHistoryNew(
                        getInteger(map.get("userId"))
                        , getString(map.get("type"))
                        , getList(map.get("idNumber"))
                        , getString(map.get("time"))
                        , getString(map.get("project"))
                        , getString(map.get("explain"))
                ));
    }

    /**
     * 查询设备碰撞业务（新）
     */
    @RequestMapping("/findImpactHistoryNew")
    public JsonResult<Object> findImpactHistoryNew(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.findImpactHistoryNew(
                        getInteger(map.get("userId"))
                        , getString(map.get("project"))
                        , getString(map.get("personName"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                ));
    }

    /**
     * 从案件添加碰撞设备到“购物车”
     */
    @RequestMapping("/impactAddDevicesByCase")
    public JsonResult<Object> impactAddDevicesByCase(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactSimpleService.impactAddDevicesByCase(
                        getString(map.get("policeNumber"))
                        , getString(map.get("caseUniqueMark"))
                        , getString(map.get("departmentName"))
                ));
    }

}


