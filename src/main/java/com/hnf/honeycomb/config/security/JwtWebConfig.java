package com.hnf.honeycomb.config.security;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/6/28 17:20
 */
@SpringBootConfiguration
public class JwtWebConfig extends WebMvcConfigurationSupport {
    @Resource
    private TokenInterceptor tokenInterceptor;
    @Resource
    private DeviceInterceptor deviceInterceptor;
    @Resource
    private EsInterceptor esInterceptor;
    @Resource
    private GisInterceptor gisInterceptor;
    @Resource
    private ManagerInterceptor managerInterceptor;
    @Resource
    private ModifyInterceptor modifyInterceptor;
    @Resource
    private RelationshipInterceptor relationshipInterceptor;
    @Resource
    private SisInterceptor sisInterceptor;
    @Resource
    private SupperInterceptor supperInterceptor;
    @Resource
    private VirtualInterceptor virtualInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 登录和注销默认不拦截
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/logout", "/jitGWAuth", "/jitGWRandom", "/jitDownloadVCTK");
        // 设备信息查询
        registry.addInterceptor(deviceInterceptor)
                .addPathPatterns("/device/**", "/case/**", "/deviceInfo/**", "/person/**")
                .excludePathPatterns("/device/aggregateDeviceInfo");
        // 管理员权限
        registry.addInterceptor(managerInterceptor)
                .addPathPatterns("/user/findAll","/softDog/findAll", "/fetch/*", "/log/*", "/role/*", "/device/aggregateDeviceInfo");
        //一键搜
        registry.addInterceptor(esInterceptor)
                .addPathPatterns("/count*")
                .excludePathPatterns("/count*.do");
        // 时空分析
        registry.addInterceptor(gisInterceptor)
                .addPathPatterns("/*.do");
        // 修改用户信息
        registry.addInterceptor(modifyInterceptor)
                .addPathPatterns("/role/add", "/role/update", "/role/delete", "/user/update");
        // 关系碰撞
        registry.addInterceptor(relationshipInterceptor)
                .addPathPatterns("/impactBy*");
        // 标采信息
//        registry.addInterceptor(sisInterceptor);
        // 超级管理员
        registry.addInterceptor(supperInterceptor)
                .addPathPatterns("/depart/add", "/depart/update", "/depart/delete");
        // 虚拟身份
        registry.addInterceptor(virtualInterceptor)
                .addPathPatterns("/findDoc/**");
    }
}
