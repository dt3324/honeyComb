package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.ElasticSearchService;
import com.hnf.honeycomb.service.EsInsertLogs;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.serviceimpl.user.JwtServiceImpl;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.ObjectUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 用于一键搜的web层
 *
 * @author yy
 */
@RestController
public class ElasticSearchController extends AbstractController {
    @Resource
    private EsInsertLogs esInsertLogs;
    @Resource
    private ElasticSearchService elasticSearchService;
    @Resource
    private JwtService jwtService;


    @RequestMapping(value = "prepareUpdate",produces = {"application/json;charset=UTF-8"})
    public JsonResult prepareUpdate(@RequestBody Map<String,Object> map, HttpServletRequest request){
        isOperate(request);
        elasticSearchService.prepareUpdate(
                ObjectUtil.getString(map.get("type")),
                ObjectUtil.getString(map.get("search")),
                (List<Map<String, String>>) (map.get("list")));
        return new JsonResult<>("ok");
    }

    /**
     * //用于统计案件的相关数量
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "countcase", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countCaseInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        esInsertLogs.insertSearchLog(
                getInteger(map.get("userId"))
                , request.getRemoteAddr()
                , getString(map.get("search"))
        );
        return new JsonResult<>(
                elasticSearchService.countCase(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime")))
        );
    }
    private void isOperate(HttpServletRequest request){
        String token = request.getHeader("token");
        List<Integer> operate = (List)jwtService.parseJWT(token).get("operate");
        if(!operate.contains(2)){
            throw new RuntimeException("暂无一键搜权限");
        }
    }

    /**
     * 用于统计涉案人员信息
     */
    @RequestMapping(value = "countperson", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countPersonInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countPerson(
                        getString(map.get("search"))
                ));
    }

    /**
     * 统计对应的设备
     */
    @RequestMapping(value = "countdevice", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countDeviceInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countDevice(
                        getString(map.get("search"))
                ));
    }

    /**
     * 统计对应的通讯录
     */
    @RequestMapping(value = "countcontact", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countContactPhone(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countContactPhoneNum(
                        getString(map.get("search"))
                ));
    }

    /**
     * 统计对应的短消息
     */
    @RequestMapping(value = "countmessage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countMessage(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countMsg(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }

    /**
     * 统计对应的通话记录
     */
    @RequestMapping(value = "countrecordcall", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countRecordCall(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countRecord(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }

    /**
     * 统计对应的qq用户
     */
    @RequestMapping(value = "countqquser", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countQQUser(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countQQUser(
                        getString(map.get("search"))
                ));
    }

    /**
     * 统计对应的wx用户
     */
    @RequestMapping(value = "countwxuser", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countWXUser(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countWXUser(
                        getString(map.get("search"))
                ));

    }

    /**
     * 统计对应的wx群聊天信息的方法
     */
    @RequestMapping(value = "countwxgroup", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countWXGroup(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countWXChatroom(
                        getString(map.get("search"))
                ));

    }

    /**
     * 统计对应的qq群聊天信息的方法
     */
    @RequestMapping(value = "countqqgroup", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countQQGroup(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countQQTroop(
                        getString(map.get("search"))
                ));
    }


    /**
     * 统计对应的qq好友聊天信息的方法
     */
    @RequestMapping(value = "countqqfriendmsg", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countQQFriendMsg(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countQqMsg(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }

    /**
     * 统计对应的wx好友聊天信息的方法
     */
    @RequestMapping(value = "countwxfriendmsg", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countWxFriendMsg(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countWxMsg(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }


    /**
     * 统计对应的wx群聊天信息的方法
     */
    @RequestMapping(value = "countwxgroupmsg", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countWxGroupMsg(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countWxChatRoomMsg(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }

    /**
     * 搜索wx群消息
     */
    @RequestMapping(value = "searchWxTroop", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> searchWxTroop(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.searchWxChatRoomMsg(
                        getString(map.get("search"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                )
        );
    }

    /**
     * 统计对应的qq群聊天信息的方法
     */
    @RequestMapping(value = "countqqgroupmsg", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Long> countQQGroupMsg(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.countQqTroopMsg(
                        getString(map.get("search"))
                        , getString(map.get("startTime"))
                        , getString(map.get("endTime"))
                ));
    }

    /**
     * 根据条件查询对应的条件
     */
    @RequestMapping(value = "findSearchResultBySearchInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findSearchResultBySearchInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        isOperate(request);
        return new JsonResult<>(
                elasticSearchService.findSearchResultBySearchInfo(
                        getString(map.get("search"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                        , getString(map.get("type"))
                )
        );
    }


}
