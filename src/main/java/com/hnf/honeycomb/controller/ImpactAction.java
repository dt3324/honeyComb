package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.ImpactService;
import com.hnf.honeycomb.util.JsonResult;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 碰撞的相关接口
 *
 * @author yy
 */
@RestController
public class ImpactAction extends AbstractController {
    @Resource
    private ImpactService impactService;


    /**
     * 通过对应的人员账号查询对应的条件
     */
    @RequestMapping("/findImpactHistoryByUId")
    public JsonResult<List<Document>> findImpactHistoryByUserId(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactService.findImpactHistoryByUserId(
                        getInteger(map.get("userId"))
                        , getString(map.get("project"))
                        , getString(map.get("searchNum"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                ));
    }

    /**
     * 删除对应的碰撞历史
     */
    @RequestMapping("/deleteImpactHistory")
    public JsonResult<Integer> deleteImpactHistory(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactService.deleteImpactHistory(
                        getString(map.get("unique"))
                ));
    }

    /**
     * 修改对应的碰撞历史
     */
    @RequestMapping("/updateImpactHistory")
    public JsonResult<Integer> updateImpactHistory(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactService.updateImpactHistory(
                        getString(map.get("unique"))
                        , getString(map.get("explain"))
                        , getString(map.get("project"))
                        , getString(map.get("searchNum"))
                ));
    }

    /**
     * 通过号码查询拥有人的姓名
     */
    @RequestMapping("/findNumInfo")
    public JsonResult<Object> findNumInfo(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                impactService.findNumInfo(
                        getString(map.get("searchType"))
                        , getString(map.get("searchNumber"))
                ));
    }
}
