package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.service.user.UserDeviceService;
import com.hnf.honeycomb.util.JsonResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;

/**
 * 设备数据表现层实现
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("device")
public class UserDeviceController extends AbstractController {

    @Resource
    private UserDeviceService userDeviceService;
    @Resource
    private JwtService jwtService;

    /**
     * 采集数据详情
     * @return
     * @ userId
     * @ startTime
     * @ endTime
     */
    @RequestMapping(value = "/aggregateDataInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> aggregateDataInfo(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(getString(request.getHeader("token")));
        return new JsonResult<>(
                userDeviceService.aggregateDataInfo(
                        getString(jwt.get("unit")),
                        getLong(map.get("startTime")),
                        getLong(map.get("endTime"))
                ));
    }

    /**
     * 采集设备详情 安卓版本号 添加苹果版本号
     * @return
     * @ userId
     * @ startTime
     * @ endTime
     */
    @RequestMapping(value = "/aggregateDeviceInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> aggregateDeviceInfo(@RequestBody Map<String, String> map, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(getString(request.getHeader("token")));
        String departCode = getString(map.get("departCode"));
        if(departCode == null){
            departCode = getString(jwt.get("unit"));
        }
        return new JsonResult<>(
                userDeviceService.aggregateDeviceInfo(
                        departCode,
                        getLong(map.get("startTime")),
                        getLong(map.get("endTime"))
                ));
    }

    /**
     * 统计案件 设备 人员详情
     * 采集质量统计
     * @return
     * @ userId
     * @ startTime
     * @ endTime
     */
    @RequestMapping(value = "/aggregateCaseDevicePersonInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> aggregateCaseDevicePersonInfo(@RequestBody Map<String, String> map){

        return new JsonResult<>(
                userDeviceService.aggregateCaseDevicePersonInfo(
                        getString(map.get("personKeyWord")),
                        getString(map.get("departmentCode")),
                        getInteger(map.get("pageNum")),
                        getInteger(map.get("pageSize")),
                        getString(map.get("WX")),
                        getString(map.get("QQ")),
                        getString(map.get("fetchPhone")),
                        getString(map.get("GPS")),
                        getLong(map.get("startTime")),
                        getLong(map.get("endTime"))
                ));
    }

    /**
     * 采集质量统计导出excel 导出到本地
     * @return
     * @ userId
     * @ startTime
     * @ endTime
     */
    @RequestMapping(value = "/fetchQualityExportExcel", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, String>> fetchQualityExportExcel(@RequestBody Map<String, String> map, HttpServletResponse response){
        Map<String, String> result = userDeviceService.fetchQualityExportExcel(
                getString(map.get("personKeyWord")),
                getString(map.get("path")),
                getString(map.get("departmentCode")),
                getInteger(map.get("pageNum")),
                getInteger(map.get("pageSize")),
                getString(map.get("WX")),
                getString(map.get("QQ")),
                getString(map.get("fetchPhone")),
                getString(map.get("GPS")),
                getLong(map.get("startTime")),
                getLong(map.get("endTime")),
                response
        );
        return new JsonResult<>(result);

    }
    @RequestMapping(value = "/downloadExcel", produces = {"application/json;charset=UTF-8"})
    public JsonResult<String> downloadExcel(@RequestBody Map<String, String> map, HttpServletResponse response){
        userDeviceService.downloadExcel(map.get("fileName"),response);
        return new JsonResult<>("下载成功");

    }


    /**
     * 统计QQ WX 通讯录的百分比
     * @return
     * @ departCode
     * @ policeNumber
     * @ type QQ WX 通讯录
     */
    @RequestMapping(value = "/fetchPercentage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> fetchPercentage(@RequestBody Map<String, Object> map) throws ParseException {
        return new JsonResult<Map<String, Object>>().setData(userDeviceService.fetchPercentage(map));
    }

    @RequestMapping(value = "/fetchEntiretyPercentage", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> fetchEntiretyPercentage(@RequestBody Map<String, Object> map) throws ParseException {
        return new JsonResult<Map<String, Object>>().setData(userDeviceService.fetchEntiretyPercentage(map));
    }
}
