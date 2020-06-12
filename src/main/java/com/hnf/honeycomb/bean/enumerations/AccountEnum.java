package com.hnf.honeycomb.bean.enumerations;

public enum AccountEnum {

    YINGHANG(1),
    WEIXIN(2),
    ALIPAY(3);

    private Integer value;

    AccountEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static AccountEnum getByValue(Integer value) {
        for (AccountEnum anEnum : AccountEnum.values()) {
            if (anEnum.getValue() == value) {
                return anEnum;
            }
        }
        return null;
    }

}
