package com.hnf.honeycomb.bean.enumerations;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 */

public enum DepartmentType implements CustomizedPersistentEnum {
    NATION(0),
    PROVINCE(1),
    CITY(2),
    COUNTY(3),
    TOWN(4);

    private final Integer level;
    public static final DepartmentType END_POINT = TOWN;

    @Override
    @JsonValue
    @Autowired
    public Integer toDBValue() {
        return level;
    }

    DepartmentType(Integer i) {
        this.level = i;
    }

    public static DepartmentType fromDBValue(Integer level) {
        final List<DepartmentType> matches = Arrays.stream(DepartmentType.values())
                .filter(e -> e.level.equals(level)).collect(Collectors.toList());
        if (matches.size() > 1) {
            throw new IllegalStateException("一个level只能对应一个departmentType，但" + level + "对应了多个");
        }
        if (matches.size() < 1) {
            throw new IllegalArgumentException(level + "不是一个合法的departmentType");
        }
        return matches.get(0);
    }

    public DepartmentType childLevel() {
        switch (this) {
            case NATION:
                return PROVINCE;
            case PROVINCE:
                return CITY;
            case CITY:
                return COUNTY;
            case COUNTY:
                return TOWN;
            default:
                throw new IllegalArgumentException(this.level + "是一个没有下级单位的级别");
        }
    }

    public Integer getLevel() {
        return level;
    }
}
