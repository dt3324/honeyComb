package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.UserLicenseBean;
import com.hnf.honeycomb.bean.enumerations.UserTypeEnum;
import com.hnf.honeycomb.bean.vm.input.CommonLoginVm;
import com.hnf.honeycomb.bean.vm.input.UserRegistingVm;
import com.hnf.honeycomb.bean.vm.input.UserUpdateVm;
import com.hnf.honeycomb.bean.vm.output.LoginedUserVmAssembler;
import com.hnf.honeycomb.bean.vm.output.QueriedUserVm;
import com.hnf.honeycomb.bean.vm.output.QueriedUserVmAssembler;
import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.service.user.RoleService;
import com.hnf.honeycomb.service.user.UserService;
import com.hnf.honeycomb.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * @author zhouhong
 * @ClassName UserController
 * @Description: TODO 用户管理表现层的实现
 * @date 2018年6月25日 上午10:38:15
 */
@RestController
@RequestMapping("user")
public @Valid class UserController extends AbstractController {

    @Autowired
    private UserService userService;

    @Autowired
    private QueriedUserVmAssembler queriedUserVmAssembler;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    /**
     * 用户登录
     */
    @RequestMapping(value = "/login", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Map<String, Object>> login(@RequestBody CommonLoginVm commonLoginVM, HttpServletResponse response, HttpServletRequest request) {
        // 获取登录IP
        String place = Utils.getLocalIp(request);
        final Map<String, Object> result = userService.login(UserTypeEnum.fromUserTypeCode(commonLoginVM.getType()), commonLoginVM.getName(), commonLoginVM.getPassword(), place, commonLoginVM.getTime());
        if (UserService.LoginStatus.SUCCESS.equals(result.get("type"))) {
            User user = (User) result.get("bean");
            result.put("bean", LoginedUserVmAssembler.userToLoginUserVm(user));

            // 添加可操作功能权限
            List<Integer> canOperation = (List<Integer>) result.get("operate");
            result.remove("operate");
            result.put("canOperation", canOperation);
            List<UserLicenseBean> userLicenses = user.getUserLicenseBeans();
            List<String> licenseUnit = new ArrayList<>();
            userLicenses.forEach(ul -> licenseUnit.add(ul.getDepartmentCode()));
            // 添加token
            Map<String, Object> bm = BuilderMap.of(String.class, Object.class)
                    .put("userId", user.getUserId())
                    .put("police", user.getPoliceNumber())
                    .put("roleId", user.getRoleId())
                    .put("operate", canOperation)
                    .put("license", licenseUnit)
                    .put("unit", user.getDepartmentCode())
                    .put("unitId", user.getDepartmentId())
                    .get();
            String token = userService.getToken(bm);
            response.setHeader("Access-Control-Expose-Headers", "token");
            response.setHeader("token", token);
        }
        return new JsonResult<>(result);
    }

    /**
     * @return
     * @ pageNum
     * @ pageSize
     * @ policeNumberAndNickName 查寻条件警号或者昵称
     * @ departmentCode 部门编号
     * @ activate 是否激活
     * @ roleId
     */
    @RequestMapping(value = "/findAll")
    public JsonResult<List<QueriedUserVm>> findAll(@RequestBody Map<String, Object> map,HttpServletRequest request) {
        String departmentCode = getString(map.get("departmentCode"));
        if (!TokenUtil.isAllowedManageUnit(request, departmentCode)) {
            return new JsonResult<>(new RuntimeException("非法操作"));
        }
        final Page<QueriedUserVm> temp = userService.findAll(
                getInteger(map.get("pageNum"))
                , getInteger(map.get("pageSize"))
                , getString(map.get("policeNumberAndNickName"))
                , departmentCode
                , getString(map.get("activate"))
                , null
                , getString(map.get("roleId"))
        ).map(
                queriedUserVmAssembler::userToLoginUserVM
        );
        return JsonResultWithPageable.succeed(
                temp.getContent(), temp.getTotalPages(), temp.getTotalElements()
        );
    }

    @RequestMapping(value = "/regist")
    @Transactional(rollbackFor = Exception.class)
    public JsonResult<String> regist(@RequestBody UserRegistingVm userRegistingVM, HttpServletRequest request) {
        final User user = userService.regist(
                StringUtils.trim(userRegistingVM.getName()),
                StringUtils.trim(userRegistingVM.getPassword()),
                StringUtils.trim(userRegistingVM.getNick()),
                StringUtils.trim(userRegistingVM.getConfirm()),
                Integer.valueOf(userRegistingVM.getRoleid()),
                StringUtils.trim(userRegistingVM.getDepartmentCode()),
                StringUtils.trim(userRegistingVM.getPoliceNumber()),
                StringUtils.trim(userRegistingVM.getIDnumber()),
                StringUtils.trim(userRegistingVM.getRemark()),
                StringUtils.trim(userRegistingVM.getPhonenumber()),
                StringUtils.trim(userRegistingVM.getVphone()),
                userRegistingVM.getActivate(),
                userRegistingVM.getCaseTypeIds(),
                StringUtils.trim(userRegistingVM.getCreateUser()));
        String token = request.getHeader("token");
        Map<String, Object> jwt = jwtService.parseJWT(token);
        String localIp = Utils.getLocalIp(request);
        Map<String, Object> operate = BuilderMap.of(String.class, Object.class).put("userId", user.getUserId())
                .put("nickName", user.getNickname()).put("policeNumber", user.getPoliceNumber())
                .put("departmentName", user.getDepartmentName()).put("departmentCode", user.getDepartmentCode())
                .get();
        userService.insertModifyLog(getString(jwt.get("police")), getString(jwt.get("unit")), localIp, 1, "添加用户", operate);
        return new JsonResult<>("ok - /user/findById?userid=" + user.getUserId());
    }

    /**
     * 删除用户信息
     *
     * @return
     * @ userId 用户ID
     * @ managerRoleId 操作人DI
     */
    @RequestMapping("/delete")
    public JsonResult<Object> delete(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        User user = userService.findById((Integer) map.get("userId"));
        if (userService.delete((Integer) map.get("userId"))) {
            //删除成功记录日志
            String token = request.getHeader("token");
            Map<String, Object> jwt = jwtService.parseJWT(token);
            String localIp = Utils.getLocalIp(request);
            Map<String, Object> operate = BuilderMap.of(String.class, Object.class).put("userId", user.getUserId())
                    .put("nickName", user.getNickname()).put("policeNumber", user.getPoliceNumber())
                    .put("departmentName", user.getDepartmentName()).put("departmentCode", user.getDepartmentCode())
                    .get();
            userService.insertModifyLog(getString(jwt.get("police")), getString(jwt.get("unit")), localIp,2, "删除用户", operate);
            final JsonResult<Object> result = new JsonResult<>("ok");
            result.setState(JsonResult.SUCCESS);
            result.setMessage("删除成功");
            return result;
        }
        final JsonResult<Object> result = new JsonResult<>();
        result.setState(JsonResult.ERROR);
        result.setMessage("删除失败");
        return result;
    }


    @PostMapping("/update")
    public JsonResult<User> updatePower(@RequestBody UserUpdateVm userUpdateVm, HttpServletRequest request) {
        String token = request.getHeader("token");
        Map<String, Object> jwt = jwtService.parseJWT(token);
        String localIp = Utils.getLocalIp(request);
        final User updated = userService.updatePower(userUpdateVm.getUserId(), userUpdateVm.getPassword(), userUpdateVm.getNickname()
                , userUpdateVm.getRoleId(), userUpdateVm.getDepartmentCode(), userUpdateVm.getRemark(), userUpdateVm.getIDnumber()
                , userUpdateVm.getPhonenumber(), userUpdateVm.getVphone(), userUpdateVm.getCaseTypeIds()
                , getString(jwt.get("police")), getString(jwt.get("unit")), localIp);
        final JsonResult<User> result = new JsonResult<>(updated);
        result.setMessage("ok");
        return result;
    }

    /**
     * 修改密码
     *
     * @return
     * @ userid 用户ID
     * @ oldPassword 旧密码
     * @ password 新密码
     * @ confirm 确认的密码
     */
    @RequestMapping("/updatePassword")
    public JsonResult<Map<String, Object>> update(@RequestBody Map<String, Object> map) {
        Map<String, Object> bean = Optional.ofNullable((Integer) map.get("userid"))
                .map(
                        i -> userService.updatePassword(
                                i
                                , String.valueOf(map.get("oldPassword"))
                                , String.valueOf(map.get("password"))
                                , String.valueOf(map.get("confirm"))
                        )
                ).orElseThrow(() -> new IllegalArgumentException("获取的用户id值是空值"));
        final JsonResult<Map<String, Object>> result = new JsonResult<>(bean);
        result.setMessage("ok");
        return result;
    }


    /**
     * 退出登录
     *
     * @param request
     * @return
     * @ userId 用户ID
     */
    @RequestMapping(value = "/logout", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.POST})
    public JsonResult<User> logout(@RequestBody Map<String, Integer> map, HttpServletRequest request) {
        String place = Utils.getLocalIp(request);
        final User bean = userService.logout(map.get("userId"), place);
        //删除对应的JWTKEY使得token失效
        jwtService.deleteJwtKey(bean.getPoliceNumber());
        return new JsonResult<>(bean);

    }

    /**
     * @param request
     * @return
     * @ userId
     * @ activate
     */
    @RequestMapping("/activate")
    public User activate(@RequestBody Map<String, Integer> map, HttpServletRequest request) {
        String place = Utils.getLocalIp(request);
        return userService.activate(map.get("userId"), map.get("activate"), place);

    }

    /**
     * departmentCode
     *
     * @return
     */
    @RequestMapping("/findUserByDepart")
    public JsonResult<List<QueriedUserVm>> findUserByDepart(@RequestBody Map<String, String> map) {

        final List<QueriedUserVm> bean = userService.findUserByDepart(map.get("departmentCode")).stream().map(
                queriedUserVmAssembler::userToLoginUserVM).collect(Collectors.toList());
        return JsonResultWithPageable.succeed(bean, 1, BigDecimal.valueOf(bean.size()).longValue());
    }

    @RequestMapping("/findByPoliceNumber")
    public JsonResult<User> findUserByPoliceNumber(@RequestBody Map<String, String> map) {
        return new JsonResult<>(
                userService.findUserByPoliceNumber(map.get("policeNumber"))
        );
    }

    /**
     * 添加用户的临时查询权限单位
     */
    @RequestMapping("/auth/addUserLicense")
    public JsonResult<Object> addUserLicense(@RequestBody Map<String,Object> userLicenses, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(request.getHeader("token"));
        userService.addUserLicense(userLicenses,
                getString(jwt.get("police")),
                getString(jwt.get("unit")),
                Utils.getLocalIp(request));
        return new JsonResult<>("ok");
    }

    /**
     * 删除用户的临时查询权限单位
     */
    @RequestMapping("/auth/deleteUserLicense")
    public JsonResult<Object> deleteUserLicense(@RequestBody UserLicenseBean userLicenseBean, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(request.getHeader("token"));
        userService.deleteUserLicense(userLicenseBean.getId()
                , getString(jwt.get("police")), getString(jwt.get("unit")), Utils.getLocalIp(request));
        return new JsonResult<>("ok");
    }

    /**
     * 修改用户的临时查询权限单位
     */
    @RequestMapping("/auth/updateUserLicense")
    public JsonResult<Object> updateUserLicense(@RequestBody UserLicenseBean userLicenseBean, HttpServletRequest request) {
        Map<String, Object> jwt = jwtService.parseJWT(request.getHeader("token"));
        userService.updateUserLicense(userLicenseBean
                , getString(jwt.get("police")), getString(jwt.get("unit")), Utils.getLocalIp(request));
        return new JsonResult<>("ok!");
    }

    /**
     * 查询用户的临时查询权限单位
     */
    @RequestMapping("/auth/findUserLicensesByUserId")
    public JsonResult<Object> findUserLicensesByUserId(@RequestBody UserLicenseBean userLicenseBean, HttpServletRequest request) {
        return new JsonResult<>(userService.findUserLicensesByUserId(userLicenseBean.getUserId()));
    }
}
