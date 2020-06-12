package com.hnf.honeycomb.bean.vm.output;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.ResidentIdentityCardBean;
import com.hnf.honeycomb.bean.User;
import com.hnf.honeycomb.service.user.UserDepartmentService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Component
public class QueriedUserVmAssembler {

    @Resource
    private UserDepartmentService userDepartmentService;

    public QueriedUserVm userToLoginUserVM(User u) {
        QueriedUserVm result;
        final DepartmentBean tempDepartment = userDepartmentService.findByDepartmentId(u.getDepartmentId());
        if (tempDepartment == null) {
            throw new RuntimeException("请选择的需要查询的部门");
        }
        result = new QueriedUserVm()
                .setUserId(u.getUserId())
                .setUsername(u.getUsername())
                .setNickname(u.getNickname())
                .setIdNumber(Optional.ofNullable(u.getIDnumber()).map(ResidentIdentityCardBean::getRaw).orElse(""))
                .setRoleId(u.getRoleId())
                .setRoleName(u.getRoleName())
                .setToken(u.getToken())
                .setPoliceNumber(u.getPoliceNumber())
                .setRemark(u.getRemark())
                .setPhoneNumber(u.getPhoneNumber())
                .setvPhone(u.getPhone())
                .setActivate(u.getActivate())
                .setDepartmentName(tempDepartment.getDepartmentName())
                .setFkDepartmentType(tempDepartment.getDepartmentType().toDBValue())
                .setDepartmentCode(tempDepartment.getDepartmentCode())
                .setSoftDog(u.getSoftDogNumber())
                .setRegions(userDepartmentService.findParentDepart(tempDepartment.getDepartmentCode()).stream()
                        .sorted(Comparator.comparingInt(d -> d.getDepartmentCode().length()))
                        .collect(Collectors.toList()))
                .setUserLicenseBeans(u.getUserLicenseBeans())
                .setCaseTypeBeans(u.getCaseTypeBeans());
        return result;
    }
}
