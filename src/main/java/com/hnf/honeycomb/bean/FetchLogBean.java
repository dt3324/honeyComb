package com.hnf.honeycomb.bean;

/**
 * @author wx
 * @ClassName: FetchLogBean
 * @Description: TODO 设备统计采集次数
 * @date 2017年5月25日 下午3:29:50
 */
public class FetchLogBean {

    private Integer id;

    private Long fetchTime;

    private Integer keyId;

    private String deviceUnique;

    private String departmentCode;

    private String policeNumber;

    private Integer type;

    public Integer getId() {
        return id;
    }

    public FetchLogBean setId(Integer id) {
        this.id = id;
        return this;
    }

    public Long getFetchTime() {
        return fetchTime;
    }

    public FetchLogBean setFetchTime(Long fetchTime) {
        this.fetchTime = fetchTime;
        return this;
    }

    public Integer getKeyId() {
        return keyId;
    }

    public FetchLogBean setKeyId(Integer keyId) {
        this.keyId = keyId;
        return this;
    }

    public String getDeviceUnique() {
        return deviceUnique;
    }

    public FetchLogBean setDeviceUnique(String deviceUnique) {
        this.deviceUnique = deviceUnique;
        return this;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public FetchLogBean setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
        return this;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public FetchLogBean setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public FetchLogBean setType(Integer type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "FetchLogBean{" +
                "id=" + id +
                ", fetchTime=" + fetchTime +
                ", keyId=" + keyId +
                ", deviceUnique='" + deviceUnique + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", policeNumber='" + policeNumber + '\'' +
                ", type=" + type +
                '}';
    }
}