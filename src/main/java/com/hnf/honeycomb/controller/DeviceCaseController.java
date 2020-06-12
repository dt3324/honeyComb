package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.DeviceCaseService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 案件表现层实现
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("case")
public class DeviceCaseController extends AbstractController {

    @Autowired
    private DeviceCaseService deviceCaseService;

    /**
     * /查询所有案件信息
     */
    @RequestMapping(value = "/queryAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> queryAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                deviceCaseService.findCaseByUnitTypeOrDog(
                        getInteger(map.get("pageNum"))
                        , getString(map.get("caseName"))
                        , getString(map.get("departmentCode"))
                )
        );
    }

    /**
     * //查询案件相关的人员和设备信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/queryByUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> queryByUnique(@RequestBody Map<String, String> map) {
        return new JsonResult<>(
                deviceCaseService.queryByUnique(
                        map.get("usernumber")
                        , map.get("deviceUnique")
                )
        );
    }

}
