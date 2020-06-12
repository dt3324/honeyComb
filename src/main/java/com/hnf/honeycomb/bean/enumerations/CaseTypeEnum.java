package com.hnf.honeycomb.bean.enumerations;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/17 10:31
 */
public enum CaseTypeEnum {
    /**
     * 案件标签库类型
     */
    SecurityCase(1, "治安案件"),
    CriminalCase(2, "刑事案件"),
    DrugCase(3, "涉毒案件"),
    NetworkCase(4, "网络安全案件"),
    EconomicCase(5, "经济案件"),
    TerrorBlowCase(6, "涉恐涉爆案件");

    CaseTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    private final Integer id;
    private final String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
