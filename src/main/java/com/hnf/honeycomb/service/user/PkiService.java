package com.hnf.honeycomb.service.user;

import java.util.Map;

/**
 * pki认证登录业务层接口
 *
 * @author zhouhong
 */
public interface PkiService {

    /**
     * 用户应用服务器生成认证原文
     *
     * @return
     */
    String jitGWRandom();

    /**
     * 网关认证登录
     *
     * @param type         登录类型
     * @param authMode     认证方式
     * @param token        token值
     * @param originalData 服务端生成的认证原文
     * @param original     客户端原文
     * @param signedData   签名结果，即数据认证包
     * @param remoteAddr   远程地址
     * @param place        认证登录IP
     * @return
     */
    Map<String, Object> jitGWAuth(Integer type, String authMode, String token, String originalData, String original,
                                  String signedData, String remoteAddr, String place);
}
