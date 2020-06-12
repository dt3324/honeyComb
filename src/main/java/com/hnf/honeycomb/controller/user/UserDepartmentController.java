package com.hnf.honeycomb.controller.user;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.controller.AbstractController;
import com.hnf.honeycomb.bean.exception.IllegalInputException;
import com.hnf.honeycomb.bean.vm.input.DepartmentCreatingVm;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import com.hnf.honeycomb.util.JsonResult;
import com.hnf.honeycomb.util.JsonResultWithPageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.hnf.honeycomb.util.ObjectUtil.getLong;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * @author zhouhong
 * @ClassName DepartmentController
 * @Description: TODO 部门管理表现出实现
 * @date 2018年6月26日 上午10：38：15
 */
@RestController
@RequestMapping("depart")
public class UserDepartmentController extends AbstractController {

    @Autowired
    private UserDepartmentService userDepartmentService;

    /**
     * 通过单位编号查找其下部门
     *
     * @return
     * @ departmentCode
     * @deprecated since v2, cause unitType is removed
     */
    @RequestMapping(value = "/findDepartmentsByCode", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.POST})
    public JsonResult<List<DepartmentBean>> findChildDepartmentsByCode(@RequestBody Map<String, String> map) {
        return new JsonResult<>(userDepartmentService.findChildDepartmentByCode(map.get("departmentCode")));
    }

    /**
     * 根据父code跟关键词进行查询
     * @param map departmentCode
     * @param map keyWord 查询的关键词
     * @return
     */
    @RequestMapping(value = "/findListByCodeAndWord", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.POST})
    public JsonResult<List<DepartmentBean>> findListByCodeAndWord(@RequestBody Map<String, String> map){
        List<DepartmentBean> departmentBeans = userDepartmentService.findListByCodeAndWord(map);
        return new JsonResult<>(departmentBeans);
    }

    /**
     * /通过单位编号查找部门
     *
     * @ departmentCode
     */
    @RequestMapping(value = "/findDepartmentByCode", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.POST})
    public JsonResult<List<DepartmentBean>> findCurrentDepartmentByCode(@RequestBody Map<String, String> map) {
        return new JsonResult<>(Arrays.asList(userDepartmentService.findByCode(map.get("departmentCode"))));
    }

    /**
     * 新增部门
     */
    @RequestMapping(value = "/add", produces = {"application/json;charset=UTF-8"})
    @ResponseStatus(HttpStatus.OK)
    public JsonResult<Object> add(@RequestBody DepartmentCreatingVm departmentCreatingVM) {
        System.out.println(departmentCreatingVM.getManagerRoleId() + "managerRoleId");
        if (Stream.of(
                StringUtils.isBlank(departmentCreatingVM.getDepartmentName()),
                departmentCreatingVM.getDepartmentCode() == null,
                departmentCreatingVM.getParentCode() == null
        ).anyMatch(Boolean.TRUE::equals)) {
            throw IllegalInputException.of("必要的数据输入为空", "2");
        }
        userDepartmentService.add(
                departmentCreatingVM.getDepartmentName(),
                departmentCreatingVM.getDepartmentCode(),
                departmentCreatingVM.getParentCode()
        );
        return new JsonResult<>("ok - /findDepartmentByCode?departmentCode=" + departmentCreatingVM.getDepartmentCode());
    }

    /**
     * 修改部门
     *
     * @return
     * @ departmentId
     * @ newDepartmentName
     * @ newDepartmentCode
     */
    @RequestMapping(value = "/update", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> update(@RequestBody Map<String, Object> map) {
        userDepartmentService.update(
                getLong(map.get("departmentId"))
                , getString(map.get("newDepartmentName"))
                , getString(map.get("newDepartmentCode")));
        return new JsonResult<>("ok");
    }

    /**
     * 删除部门
     *
     * @return
     * @ departmentId
     */
    @RequestMapping(value = "/delete", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> delete(@RequestBody Map<String, Long> map) {
        userDepartmentService.delete(map.get("departmentId"));
        return new JsonResult<>("ok");
    }

    /**
     * @return List
     * @throws
     * @Title: findParentDepart
     * @Description: TODO 查询该单位的全部上级单位
     * @ departmentCode
     */
    @RequestMapping(value = "/findParentDepart", produces = {"application/json;charset=UTF-8"}, method = {RequestMethod.GET, RequestMethod.POST})
    public JsonResult<List<DepartmentBean>> findParentDepart(@RequestBody Map<String, String> map) {
        if (StringUtils.isBlank(map.get("departmentCode"))) {
            throw IllegalInputException.of("输入的departmentCode不能为空", "1");
        }
        final List<DepartmentBean> list = userDepartmentService.findParentDepart(map.get("departmentCode"));
        return JsonResultWithPageable.succeed(list, 1, BigDecimal.valueOf(list.size()).longValue());
    }

    /**
     * @return
     */
    @RequestMapping(value = "/findProvince", produces = {"application/json;charset=UTF-8"})
    public JsonResult<Object> findProvince() {
        return new JsonResult<>(userDepartmentService.findProvince());
    }
}
