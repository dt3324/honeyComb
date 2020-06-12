package com.hnf.honeycomb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author danny
 */
@Configuration
@ConfigurationProperties(prefix = "tomcat")
public class TomcatUrlConfig {
    private String tomcatUrl;

    public String getTomcatUrl() {
        return tomcatUrl;
    }

    public void setTomcatUrl(String tomcatUrl) {
        this.tomcatUrl = tomcatUrl;
    }
}
