package com.hnf.honeycomb.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/12 17:59
 */
public class BuilderMap<K, V> implements Serializable {
    private static final long serialVersionUID = 3100672179041264102L;
    private Map<K, V> map;

    private BuilderMap() {
        map = new HashMap<>();
    }

    /**
     * 创建<K><V>泛型限定的BuilderMap
     */
    public static <K, V> BuilderMap<K, V> of(K key, V value) {
        return new BuilderMap<K, V>().put(key, value);
    }

    /**
     * 创建<K><V>类模板限定的BuilderMap
     */
    public static <K, V> BuilderMap<K, V> of(Class<K> key, Class<V> value) {
        return new BuilderMap<>();
    }

    public BuilderMap<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> get() {
        return this.map;
    }
}
