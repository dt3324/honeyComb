package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author wx  用户判断登出实体bean
 * @ClassName: UserTokenBean
 * @Description: TODO
 * @date 2017年7月4日 上午11:18:14
 */
public class UserTokenBean implements Serializable {

    private static final long serialVersionUID = -1485579162817758925L;
    private Integer loginTokenId;

    private String policeNumber;

    private String token;

    private String ipAddress;

    private Long loginTime;

    public UserTokenBean() {
        super();
        // TODO Auto-generated constructor stub
    }

    public UserTokenBean(Integer loginTokenId, String policeNumber, String token, String ipAddress, Long loginTime) {
        this.loginTokenId = loginTokenId;
        this.policeNumber = policeNumber;
        this.token = token;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
    }

    public Integer getLoginTokenId() {
        return loginTokenId;
    }

    public void setLoginTokenId(Integer loginTokenId) {
        this.loginTokenId = loginTokenId;
    }

    public String getPoliceNumber() {
        return policeNumber;
    }

    public void setPoliceNumber(String policeNumber) {
        this.policeNumber = policeNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((loginTokenId == null) ? 0 : loginTokenId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserTokenBean other = (UserTokenBean) obj;
        if (loginTokenId == null) {
            if (other.loginTokenId != null) {
                return false;
            }
        } else if (!loginTokenId.equals(other.loginTokenId)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "UserTokenBean{" +
                "loginTokenId=" + loginTokenId +
                ", policeNumber='" + policeNumber + '\'' +
                ", token='" + token + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", loginTime=" + loginTime +
                '}';
    }
}
