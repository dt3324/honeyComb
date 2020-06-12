package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.GisDeviceService;
import com.hnf.honeycomb.service.GeoService;
import com.hnf.honeycomb.util.JsonResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;

/**
 * 关于位置信息的接口
 *
 * @author yy
 */
@RestController
public class GeoController extends AbstractController {

    @Resource
    private GeoService geoService;
    @Resource
    private GisDeviceService gisDeviceService;

    /**
     * //统计对应的人的位置信息总数,前端使用的方法
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "countbytime.do", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> count(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return new JsonResult<>(
                geoService.countGeoCountPreDay(
                        getString(map.get("searchNum"))
                        , getString(map.get("userId"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                        , getString(map.get("typeSelect"))
                        , getInteger(map.get("timeLimit"))
                        , request.getRemoteAddr())
        );
    }

    /**
     * //查询对应的位置信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "find.do", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> find(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                geoService.findGeoBySomeTerm(getString(map.get("searchNum"))
                        , getString(map.get("startDate"))
                        , getString(map.get("endDate"))
                        , getString(map.get("typeSelect"))
                        , getInteger(map.get("timeLimit"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                ));
    }

    /**
     * //查询一个点周围的地理位置信息
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "gerWithin.do"
            , produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> gerWithinOnePoint(@RequestBody Map<String, Object> map) {

        String lat = getString(map.get("lat"));
        String lon = getString(map.get("lon"));
        Double radius = getDouble(map.get("radius"));
        String startDate = getString(map.get("startDate"));
        String endDate = getString(map.get("endDate"));
        String type = getString(map.get("type"));
        String search = getString(map.get("search"));
        String polygon = getString(map.get("polygon"));

        return new JsonResult<>(
                geoService.gerWithinOnePoint1(lat, lon, radius, startDate, endDate, type, search, polygon)
        );
    }

    /**
     * //模糊匹配对应的Gis搜索账号
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "likeNumberInfo.do", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findLikeNumber(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                gisDeviceService.findLikeNumberInfo(
                        getString(map.get("number"))
                        , getString(map.get("type"))
                ));
    }

    /**
     * @param map
     * @return
     */
    @RequestMapping(value = "findGeoByTime", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findGeoByTime(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                geoService.findGeoByTime(
                        getString(map.get("searchNum"))
                        , getString(map.get("date"))
                        , getString(map.get("typeSelect"))
                        , getInteger(map.get("timeLimit"))
                        , getInteger(map.get("page"))
                        , getInteger(map.get("pageSize"))
                )
        );
    }

}
