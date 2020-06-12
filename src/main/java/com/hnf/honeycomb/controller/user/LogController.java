package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.LogService;
import com.hnf.honeycomb.util.JsonResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;


/**
 * 审计管理表现层实现
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("log")
public class LogController extends AbstractController {

    @Resource
    private LogService logService;


    /**
     * 查询各个模块日志总数
     */
    @RequestMapping(value = "/findAllLog", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findAllLog(@RequestBody Map<String, String> map, HttpServletRequest request) throws Exception {
        return new JsonResult<>(
                logService.findAllLog(
                        map.get("searchContent")
                        , map.get("startDate")
                        , map.get("endDate")
                        , map.get("departmentCode")
                ));
    }

    /**
     * 根据type查询对应模块的日志记录
     */
    @RequestMapping(value = "/findLogByType", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findLogByType(@RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
        return new JsonResult<>(
                logService.findLogByType(
                        getInteger(map.get("type"))
                        , getInteger(map.get("pageNumber"))
                        , getString(map.get("searchContent"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                        , getString(map.get("departmentCode"))
                ));
    }
}
