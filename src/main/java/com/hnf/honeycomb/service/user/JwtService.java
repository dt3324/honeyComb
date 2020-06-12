package com.hnf.honeycomb.service.user;

import javax.crypto.SecretKey;
import java.util.Map;

/**
 * @author admin
 */
public interface JwtService {


    /**
     * 创建json web token
     *
     * @param claims    头部放入的参数
     * @param ttlMillis 过期时间
     * @return
     * @throws Exception
     */
    String createJWT(Map<String, Object> claims, long ttlMillis) throws Exception;

    /**
     * 由字符串生成加密key
     *
     * @return
     */
    SecretKey generalKey();

    /**
     * 解密jwt
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    Map<String, Object> parseJWT(String jwt);

    /**
     * 删除REDIS中对应的JWT的KEY
     *
     * @param policeNumber
     */
    void deleteJwtKey(String policeNumber);

    /**
     * 将token验证登录的令牌和对应警号存入数据库
     *
     * @param policeNumber 警号
     * @param key          警号
     * @return
     */
    void addJwtKey(String policeNumber, SecretKey key);

}
