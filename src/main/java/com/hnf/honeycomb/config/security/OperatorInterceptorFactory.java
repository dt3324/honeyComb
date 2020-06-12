package com.hnf.honeycomb.config.security;

import com.hnf.honeycomb.bean.enumerations.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.hnf.honeycomb.util.HttpUtil.sendError;
import static com.hnf.honeycomb.util.TokenUtil.hasOperation;
import static com.hnf.honeycomb.util.TokenUtil.isSupperAdministrator;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/8/2 18:59
 */
@Configuration
public class OperatorInterceptorFactory {

    @Bean
    public ManagerInterceptor getManagerInterceptor() {
        return new ManagerInterceptor();
    }

    @Bean
    public EsInterceptor getEsInterceptor() {
        return new EsInterceptor();
    }

    @Bean
    public RelationshipInterceptor getRelationshipInterceptor() {
        return new RelationshipInterceptor();
    }

    @Bean
    public DeviceInterceptor getDeviceInterceptor() {
        return new DeviceInterceptor();
    }

    @Bean
    public GisInterceptor getGisInterceptor() {
        return new GisInterceptor();
    }

    @Bean
    public ModifyInterceptor getModifyInterceptor() {
        return new ModifyInterceptor();
    }

    @Bean
    public SisInterceptor getSisInterceptor() {
        return new SisInterceptor();
    }

    @Bean
    public VirtualInterceptor getVirtualInterceptor() {
        return new VirtualInterceptor();
    }

    @Bean
    public SupperInterceptor getSupperInterceptor() {
        return new SupperInterceptor();
    }


}

class ManagerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.manager);
        if (!has) {
            sendError(response, "没有管理员权限", 3);
        }
        return has;
    }
}

class EsInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.elasticSearch);
        if (!has) {
            sendError(response, "没有一键搜权限", 3);
        }
        return has;
    }
}

class RelationshipInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.relationship);
        if (!has) {
            sendError(response, "没有关系碰撞权限", 3);
        }
        return has;
    }
}

class VirtualInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.virtualIdentity);
        if (!has) {
            sendError(response, "没有虚拟身份权限", 0b11);
        }
        return has;
    }
}

class GisInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.gpsAnalysis);
        if (!has) {
            sendError(response, "没有时空分析权限", 0b11);
        }
        return has;
    }
}

class DeviceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.deviceInfo);
        if (!has) {
            sendError(response, "没有设备数据权限", 0b11);
        }
        return has;
    }
}

class SisInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.collector);
        if (!has) {
            sendError(response, "没有标采统计权限", 0b11);
        }
        return has;
    }
}

class ModifyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        boolean has = hasOperation(request, Operation.modifyUserInfo);
        if (!has) {
            sendError(response, "没有用户信息修改权限", 0b11);
        }
        return has;
    }
}

class SupperInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean has = isSupperAdministrator(request);
        if (!has) {
            sendError(response, "非法操作，没有超级管理员权限", 0b11);
        }
        return has;
    }
}
