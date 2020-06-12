package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author lsj
 * @date 2018/9/12
 * 统计信息数量bean
 */
public class CountInformationBean implements Serializable {
    private static final long serialVersionUID = -205034799093733112L;

    private String search;
    private String forTime;
    private String endTime;
    private String countType;

    public String getCountType() {
        return countType;
    }

    public CountInformationBean(String search, String forTime, String endTime, String countType) {

        this.search = search;
        this.forTime = forTime;
        this.endTime = endTime;
        this.countType = countType;
    }

    public String getForTime() {
        return forTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSearch() {

        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public CountInformationBean() {

    }

    public CountInformationBean(String search, String forTime, String endTime) {

        this.search = search;
        this.forTime = forTime;
        this.endTime = endTime;
    }

    public CountInformationBean(String search, String forTime) {

        this.search = search;
        this.forTime = forTime;
    }

    public CountInformationBean(String search) {

        this.search = search;
    }

    public String getUniqueKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(search);
        stringBuilder.append(forTime);
        stringBuilder.append(endTime);
        stringBuilder.append(countType);
        return stringBuilder.toString();
    }

}
