package com.hnf.honeycomb.util;

import com.hnf.honeycomb.config.security.JwtField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hnf.honeycomb.util.ObjectUtil.getString;

/**
 * 远程调用工具类
 *
 * @author xhz
 */
@Component
public class HttpUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    private static RestTemplate REST_TEMPLATE;
    private static JwtField JWT_FIELD;
    private static String USER_IP;
    private static String VIRTUAL_IP;
    private static HttpHeaders HTTP_HEADERS;

    public HttpUtil(@Autowired RestTemplate restTemplate
            , @Autowired JwtField jwtField
            , @Value("${spring.remote.user.host}") String userHost
            , @Value("${spring.remote.user.port}") String userPort
            , @Value("${spring.remote.virtual.host}") String virtualHost
            , @Value("${spring.remote.virtual.port}") String virtualPort) {
        REST_TEMPLATE = restTemplate;
        JWT_FIELD = jwtField;
        USER_IP = "http://" + userHost + ":" + userPort + "/";
        VIRTUAL_IP = "http://" + virtualHost + ":" + virtualPort + "/";
        init();
    }

    private void init() {
        HTTP_HEADERS = new HttpHeaders();
        // 定义请求参数类型，这里用json所以是MediaType.APPLICATION_JSON
        HTTP_HEADERS.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 服务器安全验证
        HTTP_HEADERS.add("token", JWT_FIELD.getJwtSecret());
    }

    /**
     * 远程调用
     *
     * @param url          路径
     * @param para         参数
     * @param responseType 返回类型
     * @return
     */
    public static <T> T post(String url, Object para, Class<T> responseType) {
        LOGGER.info(url);

        return REST_TEMPLATE.postForObject(url
                , new HttpEntity<>(para, HTTP_HEADERS)
                , responseType
        );
    }

    /**
     * 远程调用
     *
     * @param url          路径
     * @param para         参数
     * @param responseType 返回类型
     * @return
     */
    public static <T> T exchange(String url, Object para, ParameterizedTypeReference<T> responseType) {
        LOGGER.info(url);

        return REST_TEMPLATE.exchange(url
                , HttpMethod.POST
                , new HttpEntity<>(para, HTTP_HEADERS)
                , responseType
        ).getBody();
    }

    /**
     * 用户服务远程请求
     */
    public static <T> T userCall(String path, Object para, Class<T> responseType) {
        return post(USER_IP + path, para, responseType);
    }

    /**
     * 用户服务远程请求
     */
    public static <T> T userCall(String path, Object para, ParameterizedTypeReference<T> responseType) {
        return exchange(USER_IP + path, para, responseType);
    }

    /**
     * 虚拟身份远程请求
     */
    public static <T> T virtualCall(String path, Object para, Class<T> responseType) {
        return post(VIRTUAL_IP + path, para, responseType);
    }

    /**
     * 虚拟身份远程请求
     */
    public static <T> T virtualCall(String path, Object para, ParameterizedTypeReference<T> responseType) {
        return exchange(VIRTUAL_IP + path, para, responseType);
    }

    /**
     * 发送错误信息
     *
     * @param response
     * @param message
     * @param state
     */
    public static void sendError(HttpServletResponse response, String message, int state) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("{\"state\":" + state + ",\"data\":null,\"message\":\"" + message + "\"}");
        response.getWriter().close();
    }

    /**
     * 字段添加函数。因为之前的连表查询无法实现，使用此方法远程查询所需要的字段
     *
     * @param list    需要添加字段的集合
     * @param codeKey
     * @param nameKey
     * @param path
     */
    public static <T extends Map<String, Object>> void addField(List<T> list, String codeKey, String nameKey, String path) {
        Map<String, String> temp = new HashMap<>(list.size());
        // 获取编码集合，以Map-key形式存在并传输
        for (Map<String, Object> map : list) {
            if (map.get(codeKey) == null) {
                continue;
            }
            temp.put(
                    getString(
                            map.get(codeKey)
                    )
                    , null
            );
        }
        if (temp.size() < 1) {
            return;
        }
        // 获取编码-名称键值对
        temp = userCall(path
                , temp
                , new ParameterizedTypeReference<Map<String, String>>() {
                }
        );
        // 遍历List，给Map添加名称字段
        for (Map<String, Object> map : list) {
            if (map.get(codeKey) == null) {
                continue;
            }
            map.put(
                    nameKey,
                    temp.get(
                            getString(
                                    map.get(codeKey)
                            )
                    ));
        }
    }
}
