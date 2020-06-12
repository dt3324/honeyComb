package com.hnf.honeycomb.controller;

import com.hnf.honeycomb.service.DeviceService;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import com.hnf.honeycomb.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 设备数据表现层实现
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("device")
public class DeviceController extends AbstractController {

    @Resource
    private DeviceService deviceService;

    @Autowired
    private JwtService jwtService;

    /**
     * 查询所有设备数据
     */
    @RequestMapping(value = "/queryAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> queryAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String departmentCode = getString(map.get("departmentCode"));
        if(departmentCode == null){
            throw new RuntimeException("部门代码不能为空！");
        }
        String token = request.getHeader("token");
        String policeNumber = (String) jwtService.parseJWT(token).get("police");
        return new JsonResult<>(
                deviceService.findDeviceBySomeTerms(
                        getInteger(map.get("pageNumber")),
                        getString(map.get("deviceName")),
                        getInteger(map.get("type")),
                        getString(map.get("startTime")),
                        getString(map.get("endTime")),
                        policeNumber,
                        departmentCode,
                        getInteger(map.get("mineOnly")),
                        getInteger(map.get("qq")),
                        getInteger(map.get("wx")),
                        getInteger(map.get("phone")),
                        getInteger(map.get("gps")),
                        getInteger(map.get("collType")),
                        getString(map.get("policeNumber")))
        );
    }

    /**
     * //通过设备唯一标识查询设备相关人员、案件
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/findByDeviceUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findByDeviceUnique(@RequestBody Map<String, String> map) {
        return new JsonResult<>(
                deviceService.findByDeviceUnique(map.get("deviceUnique")));
    }

    /**
     * //查询设备详细信息
     *
     * @param map
     * @param request
     * @return
     */
    @RequestMapping(value = "/findByInfo", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findByInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String place = Utils.getLocalIp(request);
        return new JsonResult<>(
                deviceService.queryByDeviceUnique(
                        getInteger(map.get("userId"))
                        , getString(map.get("deviceUnique"))
                        , place)
        );
    }

    /**
     * //查询采集历史
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/findLogByDeviceUnique", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findLogByDeviceUnique(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceService.findLogByDeviceUnique(getString(map.get("deviceUnique"))));
    }

    /**
     * // 查詢用戶是否有設備詳情查看權限
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/isNotCheck", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> isNotCheck(@RequestBody Map<String, Object> map) {
        return new JsonResult<>(
                deviceService.isNotCheck(getString(map.get("deviceUnique"))
                        , getString(map.get("roleCheckPermissionCode"))
                        , getString(map.get("softDogOrPoliceNumber")))
        );
    }

}
