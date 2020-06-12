package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.bean.OperationBean;
import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.service.user.RoleService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.*;

/**
 * 角色管理表现层
 *
 * @author zhouhong
 */
@RestController
@RequestMapping("role")
public class RoleController extends AbstractController {

    @Autowired
    private RoleService roleService;

    @Resource
    JwtService jwtService;

    /**
     * 查询所有角色
     *
     * @ page
     * @ userId
     * @ pageSize
     * @
     */
    @RequestMapping(value = "/findAll", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findAll(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        Map<String, Object> chain = jwtService.parseJWT(request.getHeader("token"));
        Long userId = getLong(chain.get("userId"));
        return new JsonResult<>(
                roleService.findAll(
                        getInteger(map.get("page"))
                        , userId
                        , getInteger(map.get("pageSize"))
                ));
    }

    /**
     * 新增角色
     *
     * @return
     * @ roleName
     * @ roleCheckPermissionCode
     * @ roleAbleFunId
     * @ userId
     */
    @RequestMapping(value = "/add", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> add(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(request.getHeader("token"));
        String operatorIds = getString(map.get("roleAbleFunId"));
        boolean has = TokenUtil.hasOperation(request, operatorIds);
        if (!has) {
            return JsonResult.ofError("没有权限！！！");
        }
        roleService.add(
                getString(map.get("roleName"))
                , operatorIds
                , getLong(jwt.get("userId"))
        );
        return new JsonResult<>("ok");
    }

    /**
     * 查询所有可操作功能
     *
     * @return
     */
    @RequestMapping(value = "/findFun", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findFun(HttpServletRequest request) {
        Map<String, Object> chain = jwtService.parseJWT(request.getHeader("token"));
        String operate = chain.get("operate").toString();
        List<OperationBean> fun = roleService.findFun();
        // 移除当前用户没有的权限
        for (int i = 0; i < fun.size(); i++) {
            if (!operate.contains(fun.get(i).getOperationId().toString())) {
                fun.remove(i--);
            }
        }
        return new JsonResult<>(fun);
    }

    /**
     * 更新角色
     *
     * @return
     * @ roleId
     * @ roleName
     * @ roleCheckPermissionCode
     * @ roleAbleFunId
     */
    @RequestMapping(value = "/update", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> update(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String operatorIds = getString(map.get("roleAbleFunId"));
        boolean has = TokenUtil.hasOperation(request, operatorIds);
        if (!has) {
            return JsonResult.ofError("没有权限！！");
        }
        roleService.update(
                getLong(map.get("roleId"))
                , getString(map.get("roleName"))
                , operatorIds);
        return new JsonResult<>("ok");
    }

    /**
     * 删除角色
     *
     * @return
     * @ roleId
     */
    @RequestMapping(value = "/delete", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST})
    public JsonResult<Object> delete(@RequestBody Map<String, Long> map) {
        roleService.delete(map.get("roleId"));
        return new JsonResult<>("ok");
    }

}
