package com.hnf.honeycomb.util;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 集合工具类
 *
 * @author yy
 */
public class CollectionUtils {
    /**
     * 查询对应的集合是否为空
     *
     * @param coll 对应的集合
     * @return 返回是否为空
     */
    public static Boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static Boolean mapIsEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 构造Map
     */
    public static <T> Map<String, T> ofMap(String key, T t) {
        Map<String, T> map = new HashMap<>(1);
        map.put(key, t);
        return map;
    }

}
