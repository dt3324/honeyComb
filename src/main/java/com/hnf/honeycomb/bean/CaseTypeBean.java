package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/17 10:31
 */
public class CaseTypeBean implements Serializable {
    private static final long serialVersionUID = 4303508005688150575L;
    private Long id;
    private String name;

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public CaseTypeBean setName(String name) {
        this.name = name;
        return this;
    }

    public CaseTypeBean setId(Long id) {
        this.id = id;
        return this;
    }
}
