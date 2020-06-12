package com.hnf.honeycomb.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
public class RedisCacheConfiguration {
    Logger logger = LoggerFactory.getLogger(RedisCacheConfiguration.class);

    @Bean
    public JedisPool redisPoolFactory(@Autowired RedisProperties redisProperties) {
        logger.info("JedisPool注入成功！！");
        logger.info("redis地址：" + redisProperties.getHost() + ":" + redisProperties.getPort());
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisProperties.getJedis().getPool().getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getJedis().getPool().getMaxWait().toMillis());

        JedisPool jedisPool = new JedisPool(
                jedisPoolConfig,
                redisProperties.getHost(),
                redisProperties.getPort(),
                Long.valueOf(redisProperties.getTimeout().toMillis()).intValue(),
                redisProperties.getPassword()
        );
        return jedisPool;
    }

}
