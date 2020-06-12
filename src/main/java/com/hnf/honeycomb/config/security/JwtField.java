package com.hnf.honeycomb.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/11 16:24
 */
@Configuration
@ConfigurationProperties(prefix = "spring.user")
public class JwtField {
    private Long timeLength;
    private Long count;
    private Long operationTimeBetween;
    private Long ttlMillis;
    private String jwtSecret;

    public JwtField setTimeLength(Long timeLength) {
        this.timeLength = timeLength;
        return this;
    }

    public JwtField setCount(Long count) {
        this.count = count;
        return this;
    }

    public JwtField setOperationTimeBetween(Long operationTimeBetween) {
        this.operationTimeBetween = operationTimeBetween;
        return this;
    }

    public JwtField setTtlMillis(Long ttlMillis) {
        this.ttlMillis = ttlMillis;
        return this;
    }

    public JwtField setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
        return this;
    }

    public Long getTimeLength() {
        return timeLength;
    }

    public Long getCount() {
        return count;
    }

    public Long getOperationTimeBetween() {
        return operationTimeBetween;
    }

    public Long getTtlMillis() {
        return ttlMillis;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }
}
