package com.hnf.honeycomb.bean.vm.output;

import com.hnf.honeycomb.bean.ResidentIdentityCardBean;
import com.hnf.honeycomb.bean.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author admin
 */
public class LoginedUserVmAssembler {
    public static LoginedUserVm userToLoginUserVm(User u) {
        return new LoginedUserVm()
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
                .setDepartmentName(u.getDepartmentName())
                .setFkDepartmentType(u.getDepartmentType())
                .setDepartmentCode(u.getDepartmentCode())
                .setUserLicenseBeans(u.getUserLicenseBeans())
                .setCaseTypeBeans(u.getCaseTypeBeans());
    }
}
