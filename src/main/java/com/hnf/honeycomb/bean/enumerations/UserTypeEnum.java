package com.hnf.honeycomb.bean.enumerations;

import java.util.Arrays;

/**
 * @author admin
 */

public enum UserTypeEnum {
    NORMAL_USER(1),
    ADMIN(2);

    private final Integer userTypeCode;

    UserTypeEnum(Integer i) {
        this.userTypeCode = i;
    }

    public Integer getUserTypeCode() {
        return userTypeCode;
    }

    public static UserTypeEnum fromUserTypeCode(Integer i) {
        return Arrays.stream(UserTypeEnum.values()).filter(
                e -> e.userTypeCode.equals(i)
        ).findFirst().orElse(null);
    }
}
