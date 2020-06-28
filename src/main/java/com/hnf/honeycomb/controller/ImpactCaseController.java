package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.ImpactCaseService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getString;


/**
 * @author ...
 */
@RestController
public class ImpactCaseController extends AbstractController {

    @Autowired
    private ImpactCaseService impactCaseService;


    /**
     * 通过案件名模糊查询案件
     */
    @RequestMapping("/findCaseByCaseName")
    public JsonResult<List<Document>> findCaseByCaseName(@RequestBody Map<String, String> map, HttpServletRequest request) {
        return new JsonResult<>(
                impactCaseService.findCaseByCaseName(
                        TokenUtil.getDepartmentCode(request)
                        , map.get("caseName")
                ));

    }

    /**
     * 通过案件名查询关联的设备名
     */
    @RequestMapping("/findDeviceByCaseName")
    public JsonResult<List<Document>> findDeviceByCaseName(@RequestBody Map<String, String> map) {
        return new JsonResult<>(
                impactCaseService.findDeviceByCaseName(
                        map.get("query")
                ));

    }

    /**
     * 通过人员姓名模糊匹配人员
     */
    @RequestMapping("/findPersonByPersonName")
    public JsonResult<Object> findPersonByPersonName(@RequestBody Map<String, String> map, HttpServletRequest request) {
        return new JsonResult<>(
                impactCaseService.findPersonByPersonName(
                        TokenUtil.getDepartmentCode(request),
                        map.get("name")
                ));
    }

    /**
     * 通过人员姓名模糊匹配人员
     */
    @RequestMapping("/findPersonByPhone")
    public JsonResult<Object> findPersonByPhone(@RequestBody Map<String, String> map, HttpServletRequest request) {
        return new JsonResult<>(
                impactCaseService.findPersonByPhone(
                        TokenUtil.getDepartmentCode(request),
                        map.get("phone")
                ));
    }
}
