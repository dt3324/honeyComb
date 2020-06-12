package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.FetchService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;


/**
 * @author zhouhong
 * @ClassName FetchController
 * @Description: TODO 小蜜蜂表现层实现
 * @date 2018年6月28日 上午10：38：15
 */
@RestController
@RequestMapping("fetch")
public class FetchController extends AbstractController {

    @Autowired
    private FetchService fetchService;

    /**
     * 对于小蜜蜂采集统计对应其的统计方法
     *
     * @return
     * @ departmentCode
     * @ pNumber
     * @ page
     */
    @RequestMapping(value = "/countSBeeCount", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> countSBeeCount(@RequestBody Map<String, Object> map,HttpServletRequest request) {
        String departmentCode = getString(map.get("departmentCode"));
        if (!TokenUtil.isAllowedManageUnit(request, departmentCode)) {
            return JsonResult.ofError("非法操作");
        }
        return new JsonResult<>(
                fetchService.countSBeeCount(
                        departmentCode
                        , getString(map.get("pNumber"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"), 20)));
    }

    /**
     * 查询大小蜜蜂采集量
     *
     * @return
     * @ departCode
     * @ pNumber
     * @ startDate
     * @ endDate
     */
    @RequestMapping(value = "/findCount", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> countFetchLogSmallAndBig(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String departmentCode = getString(map.get("departCode"));
        if (!TokenUtil.isAllowedManageUnit(request, departmentCode)) {
            return JsonResult.ofError("非法操作");
        }
        return new JsonResult<>(
                fetchService.countFetchLogSmallAndBig(
                        departmentCode
                        , getString(map.get("pNumber"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                ));
    }
}
