package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.VirtualRemarkService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.Utils;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author hnf
 */
@RestController
@RequestMapping("/findDoc")
public class VirtualIdentityController extends AbstractController {

    @Resource
    private VirtualRemarkService virtualRemarkService;

    /**
     * //通过对应的条件查询对应的备注信息
     */
    @RequestMapping("/findPersonBaseInfo")
    public JsonResult<List<Map<String, Object>>> findRemark(
            @RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = request.getRemoteAddr();
        return new JsonResult<>(
                virtualRemarkService.findPersonExtendsInfoBySearchNum(
                        (Integer) map.get("userId")
                        , (String) map.get("searchNum")
                        , (String) map.get("type"), place)
        );

    }

    /**
     * //查询人员的基本信息
     */
    @RequestMapping("/findPersonBaseInfoByUserNumber")
    public JsonResult<Document> findPersonBaseInfo(@RequestBody Map<String, String> map) {
        System.out.println("请求到:num:" + map.get("num"));
        return new JsonResult<>(
                virtualRemarkService.findPersonBaseInfoByUNumber(map.get("num")));
    }

    /**
     * //查询备注信息
     */
    @RequestMapping(value = "/findRemarkByNumberAndType")
    public JsonResult<Map<String, Object>> findRemarkByNumberAnfType(
            @RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = Utils.getLocalIp(request);
        return new JsonResult<>(
                virtualRemarkService.findRemark(
                        (Integer) map.get("userId")
                        , (String) map.get("num")
                        , (String) map.get("type"), place
                )
        );
    }
}
