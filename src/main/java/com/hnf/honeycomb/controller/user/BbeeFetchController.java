package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.BbeeFetchService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 用于加密狗号查询的相关接口
 *
 * @author yy
 */
@RestController
@RequestMapping(value = {"softDog","bBeeFetch"})
public class BbeeFetchController extends AbstractController {
    @Autowired
    private BbeeFetchService bBeeFetchService;

    /**
     * 根据各自搜索条件查询大蜜蜂采集数据
     */
    @RequestMapping(value = "/findAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String departmentCode = getString(map.get("departCode"));
        if (!TokenUtil.isAllowedManageUnit(request, departmentCode)) {
            return JsonResult.ofError("非法操作");
        }

        //需要查询的采集类型 1是大蜜蜂 2是小蜜蜂 3是海鑫
        List<Integer> type = new ArrayList<>();
        type.add(1);
        final Map<String, Object> list = bBeeFetchService.findAll(
                getInteger(map.get("pageNumber")),
                type,
                getString(map.get("policeNumber")),
                getString(map.get("selectType")),
                departmentCode,
                null,
                getInteger(map.get("pageSize"))
        );
        return new JsonResult<>(list);
    }

    /**
     * 根据各自搜索条件查询其他公司采集入库的数据包
     */
    @RequestMapping(value = "/findOtherAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findOtherAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String departmentCode = getString(map.get("departCode"));
        if (!TokenUtil.isAllowedManageUnit(request, departmentCode)) {
            return JsonResult.ofError("非法操作");
        }
        //需要查询的采集类型 1是大蜜蜂 2是小蜜蜂 3是海鑫
        List<Integer> type = new ArrayList<>();
        type.add(3);
        final Map<String, Object> list = bBeeFetchService.findAll(
                getInteger(map.get("pageNumber")),
                type,
                getString(map.get("policeNumber")),
                getString(map.get("selectType")),
                departmentCode,
                null,
                getInteger(map.get("pageSize"))
        );
        return new JsonResult<>(list);
    }

}
