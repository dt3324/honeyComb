package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.bean.OperationBean;
import com.hnf.honeycomb.bean.RoleBean;
import com.hnf.honeycomb.bean.RoleLicenseBean;
import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.bean.exception.IllegalInputException;
import com.hnf.honeycomb.remote.user.*;
import com.hnf.honeycomb.service.user.RoleService;
import com.hnf.honeycomb.util.BuilderMap;
import com.hnf.honeycomb.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 角色业务接口实现层
 *
 * @author zhouhong
 */
@Service("roleService")
public class RoleServiceImpl implements RoleService {
    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OperationMapper operationMapper;

    @Resource
    private RoleOperationMapper roleOperationMapper;

    @Resource
    private RoleLicenseMapper roleLicenseMapper;

    @Override
    public Map<String, Object> findAll(Integer page, Long userId, Integer pageSize) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("start", (page - 1) * pageSize);
        userMap.put("end", pageSize);
        List<Map<String, Object>> roles = roleMapper.findAll(userMap);
        //查询角色总数
        Integer count = roleMapper.findCount(userId);
        return BuilderMap.of(String.class, Object.class).put("count", count).put("totalPage", Math.ceil(((float) count) / pageSize)).put("role", roles).get();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(String roleName, String roleAbleFunId, Long userId) {
        Map<String, Object> addRole = new HashMap<>();
        addRole.put("roleName", roleName);
        addRole.put("userId", userId);
        RoleBean roleBean = roleMapper.findByNameUnitId(addRole);
        if (roleBean != null) {
            throw IllegalInputException.of("该角色已存在");
        }
        if (StringUtils.isBlank(roleAbleFunId)) {
            throw IllegalInputException.of("请至少添加一个权限");
        }
        //添加角色
        roleMapper.add(addRole);
        roleBean = roleMapper.findByNameUnitId(addRole);
        //添加角色操作权限
        Map<String, Object> operationMap = new HashMap<>();
        String[] roleFunId = roleAbleFunId.split(",");
        int[] operation = new int[roleFunId.length];
        for (int i = 0; i < roleFunId.length; i++) {
            operation[i] = Integer.parseInt(roleFunId[i]);
            operationMap.put("roleId", roleBean.getRoleId());
            operationMap.put("operationId", operation[i]);
            roleOperationMapper.add(operationMap);
        }
    }

    @Override
    public List<OperationBean> findFun() {
        return operationMapper.find();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long roleId, String roleName, String roleAbleFunId) {
        if (Stream.of(roleName, roleAbleFunId).anyMatch(StringUtils::isBlank) || null == roleId) {
            throw IllegalInputException.of("角色Id，角色姓名，角色可用操作都不能为空", "9");
        }
        RoleBean roleBean = roleMapper.findByRoleId(roleId);
        if (roleBean == null) {
            throw new RuntimeException("未查询到角色");
        }
        if (roleBean.getUserId() == null) {
            throw new RuntimeException("默认角色不能修改");
        }
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("roleId", roleId);
        roleMap.put("roleName", roleName);
        //更新角色名称
        roleMapper.update(roleMap);

        //更新可操作功能
        roleOperationMapper.deleteByRoleId(roleId.toString());
        Map<String, Object> operationMap = new HashMap<>();
        String[] roleFunId = roleAbleFunId.split(",");
        int[] operation = new int[roleFunId.length];
        for (int i = 0; i < roleFunId.length; i++) {
            operation[i] = Integer.parseInt(roleFunId[i]);
            operationMap.put("roleId", roleId);
            operationMap.put("operationId", operation[i]);
            roleOperationMapper.add(operationMap);
        }
    }

    @Override
    public void delete(Long roleId) {
        //判断该角色是否未默认角色
        RoleBean roleBean = Optional.ofNullable(roleId).map(Long::intValue)
                .map(roleMapper::findByRoleId).orElse(null);
        if (roleBean == null) {
            throw IllegalInputException.of("要删除的角色已不存在", "1");
        }
        if (roleBean.getUserId() == null) {
            throw new RuntimeException("默认角色不能删除");
        }
        //判断是否绑定了用户
        List<User> user = userMapper.findByRoleId(roleId);
        if (!CollectionUtils.isEmpty(user)) {
            throw new RuntimeException("该角色绑定了用户，不能删除");
        }
        //删除角色可操作功能
        roleOperationMapper.deleteByRoleId(roleId.toString());
        //删除角色
        roleMapper.delete(roleId);
    }
    @Override
    public String findCanChectDepartCode(Long roleId) {
        RoleLicenseBean roleLicenseBean = roleLicenseMapper.findByRoleId(roleId);
        String canChectDepartCode = "";
        if (roleLicenseBean != null) {
            canChectDepartCode = roleMapper.findCanCheckDepartCodeByRoleId(roleId.intValue()).get(0);
        }
        return canChectDepartCode;
    }
}
