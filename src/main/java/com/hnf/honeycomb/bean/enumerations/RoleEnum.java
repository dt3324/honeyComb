package com.hnf.honeycomb.bean.enumerations;

/**
 * @author lsj
 * 角色权限枚举类
 * 用于批量添加用户 的扩展性
 */
public enum RoleEnum {
    //普通用户对应的role_Id
    normal("普通用户", 2);
    private final String key;
    private final Integer value;

    RoleEnum(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public static RoleEnum getEnumByKey(String key) {
        if (null == key) {
            return null;
        }
        for (RoleEnum temp : RoleEnum.values()) {
            if (temp.getKey().equals(key)) {
                return temp;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public static void main(String[] args) {
        System.out.println(RoleEnum.normal.getKey());
        System.out.println(RoleEnum.normal.getValue());
    }

}
