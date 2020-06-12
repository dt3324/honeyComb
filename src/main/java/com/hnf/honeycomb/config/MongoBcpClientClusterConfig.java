package com.hnf.honeycomb.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDriverInformation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

import static com.hnf.honeycomb.util.ObjectUtil.getInteger;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

@Configuration
@ConfigurationProperties(prefix = "mongo.bcp")
public class MongoBcpClientClusterConfig {
    public static final String MONGO_SIS_BCP = "mongoBcp";

    private String clusterUsername;
    private String clusterPassword;
    private String clusterDb;
    private List<Map<String,Object>> clusterHosts;

    @Bean(name = MONGO_SIS_BCP)
    public MongoClient getMongoClient() {
        List<ServerAddress> addresses = new LinkedList<>();
        clusterHosts.stream().filter(map -> getString(map.get("ip")) != null && getInteger(map.get("port")) != null)
                .forEach(map -> addresses.add(new ServerAddress(getString(map.get("ip")), getInteger(map.get("port")))));
        MongoClientOptions options = MongoClientOptions.builder().build();
        MongoDriverInformation information = MongoDriverInformation.builder().build();
        if (StringUtils.hasText(clusterPassword)) {
            MongoCredential credential = MongoCredential.createCredential(
                clusterUsername, clusterDb, clusterPassword.toCharArray()
            );
            return new MongoClient(addresses, credential, options, information);
        } else {
            return new MongoClient(addresses, options);
        }
    }

    public void setClusterUsername(String clusterUsername) {
        this.clusterUsername = clusterUsername;
    }

    public void setClusterPassword(String clusterPassword) {
        this.clusterPassword = clusterPassword;
    }

    public void setClusterDb(String clusterDb) {
        this.clusterDb = clusterDb;
    }

    public void setClusterHosts(List<Map<String,Object>> clusterHosts) {
        this.clusterHosts = clusterHosts;
    }
}
