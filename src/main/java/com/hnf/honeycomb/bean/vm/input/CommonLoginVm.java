package com.hnf.honeycomb.bean.vm.input;

/**
 * @author admin
 */
public class CommonLoginVm {
    private Integer type;
    private String name;
    private String password;
    private String time;

    public void setType(Integer type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "CommonLoginVm{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
