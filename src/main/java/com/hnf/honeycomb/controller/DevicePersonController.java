package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.DevicePersonService;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;

/**
 * 人员信息表现层实现
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("person")
public class DevicePersonController extends AbstractController {

    @Autowired
    private DevicePersonService devicePersonService;

    @Autowired
    private JwtService jwtService;

    /**
     * //查询人员列表
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> queryAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                devicePersonService.personList(
                        getInteger(map.get("pageNumber"))
                        , getString(map.get("personName"))
                        // 人员身份证
                        , getString(map.get("userNumber"))
                        , (List)(map.get("caseTypeId"))
                        ,getString(map.get("departmentCode"))
                )
        );
    }


    /**
     * //查询人员相关的案件和设备信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryByUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> queryByUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                devicePersonService.queryByUnique(
                        getString(map.get("caseUniqueMark"))
                        , getString(map.get("deviceUnique"))
                ));
    }

//
//    /**
//     * //通过设备唯一标识查询人员信息
//     *
//     * @param map
//     * @return
//     */
//    @RequestMapping(value = "/personQueryByDeviceUnique", produces = {"application/json;charset=UTF-8"})
//    public JsonResult<Object> personQueryByDeviceUnique(@RequestBody Map<String, Object> map) {
//        return new JsonResult<>(
//                devicePersonService.personQueryByDeviceUnique(
//                        getString(map.get("deviceUnique"))
//                ));
//    }

    /**
     * //通过对应的条件查询出对应的单位下的标采人员信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/findSisPerson", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findSisPerson(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(request.getHeader("token"));
        return new JsonResult<>(
                devicePersonService.findSisPerson(
                        getString(map.get("pNumber"))
                        , getString(map.get("searchPNumber"))
                        , getString(map.get("personName"))
                        , getString(map.get("personNumber"))
                        , getString(map.get("isGenerateBCP"))
                        , getString(map.get("isUploadSuccess"))
                        , getString(map.get("personSerialNum"))
                        , getString(map.get("personType"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                        , getString(jwt.get("unitCode"))
                        , null)
        );
    }


    /**
     * //标采人员的统计量
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/analysisSisCount", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> analysisSisCount(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                devicePersonService.analysisSisPerson(
                        getString(map.get("policeNumber"))
                        , getString(map.get("departmentCode"))
                        , null
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                ));
    }


    /**
     * //按时间统计标采人员
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/analysisSisPersonPreDate", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> analysisSisPersonPreDate(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                devicePersonService.analysisSisPersonPreDate(
                        getString(map.get("policeNumber"))
                        , getString(map.get("departmentCode"))
                        , null
                        , getLong(map.get("startTime"))
                        , getLong(map.get("endTime"))
                        , getString(map.get("type"))
                ));
    }


    /**
     * //查询上传省标采详情/**
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/findUploadSisDetail", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findUploadSisDetail(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                devicePersonService.findUploadSisFalseDetail(
                        getString(map.get("departmentCode"))
                        , getString(map.get("personNumber"))
                        , getInteger(map.get("scjgType"))
                ));
    }

}
