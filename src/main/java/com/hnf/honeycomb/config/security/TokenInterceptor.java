package com.hnf.honeycomb.config.security;

import com.hnf.honeycomb.service.user.JwtService;
import com.hnf.honeycomb.util.RedisUtilNew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.hnf.honeycomb.util.HttpUtil.sendError;
import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/6/28 17:19
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    private static final int NUM = 5;
    @Autowired
    private JwtService jwtService;
    @Resource
    private RedisUtilNew redisUtilNew;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        String ip = request.getRemoteAddr();
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        logger.info(ip + " 请求的 " + request.getRequestURI());
        if (StringUtils.isEmpty(token)) {
            if (RequestMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            sendError(response, "token为空", 3);
            return false;
        }
        Map<String, Object> chain = this.jwtService.parseJWT(token);
        String policeNumber = getString(chain.get("police"));
        boolean exist = hasRole(policeNumber);
        if (!exist) {
            sendError(response, "登录已失效，请重新登录!", 4);
        }
        return exist;
    }

    /**
     * 查询Redis中是否有当前警号的登录记录
     *
     * @param policeNumber
     * @return
     */
    private boolean hasRole(String policeNumber) {
        Object getResult = this.redisUtilNew.get(RedisUtilNew.USER_LOGIN_CHECK + policeNumber);
        return getResult != null && getResult.toString().length() > NUM;
    }


}
