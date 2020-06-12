package com.hnf.honeycomb.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author xhz
 * @version 1.0
 * @date 2019/7/15 9:09
 */
public class ObjectUtil {

    public static final Pattern PATTERN_NUMBER_SHORT = Pattern.compile("[+-]?\\d{1,4}");
    public static final Pattern PATTERN_NUMBER_INTEGER = Pattern.compile("[+-]?\\d{1,9}");
    public static final Pattern PATTERN_NUMBER_LONG = Pattern.compile("[+-]?\\d{1,19}");
    public static final Pattern PATTERN_NUMBER_DOUBLE = Pattern.compile("[+-]?\\d{1,20}(\\.\\d+)?");


    /**
     * 输入正整数返回Integer，否则返回Null
     */
    public static Integer getInteger(Object obj) {
        return getInteger(obj, null);
    }

    /**
     * 输入正整数返回Integer，否则返回orElse
     */
    public static Integer getInteger(Object obj, Integer orElse) {
        if (obj != null && PATTERN_NUMBER_INTEGER.matcher(obj.toString()).matches()) {
            return Integer.valueOf(obj.toString());
        }
        return orElse;
    }

    /**
     * 输入正整数返回Integer，否则返回Null
     */
    public static Long getLong(Object obj) {
        return getLong(obj, null);
    }

    /**
     * 输入正整数返回Integer，否则返回orElse
     */
    public static Long getLong(Object obj, Long orElse) {
        if (obj != null && PATTERN_NUMBER_LONG.matcher(obj.toString()).matches()) {
            return Long.valueOf(obj.toString());
        }
        return orElse;
    }

    /**
     * 输入浮点数返回Double，null
     */
    public static Double getDouble(Object obj) {
        return getDouble(obj, null);
    }

    /**
     * 输入浮点数返回Double，否则返回orElse
     */
    public static Double getDouble(Object obj, Double orElse) {
        if (obj != null && PATTERN_NUMBER_DOUBLE.matcher(obj.toString()).matches()) {
            return Double.valueOf(obj.toString());
        }
        return orElse;
    }

    /**
     * 输入非空对象返回obj.toString()，否则返回null
     */
    public static String getString(Object obj) {
        return getString(obj, null);
    }

    /**
     * 输入非空对象返回obj.toString()，否则返回orElse
     */
    public static String getString(Object obj, String orElse) {
        if (obj != null) {
            return obj.toString().trim();
        }
        return orElse;
    }

    /**
     * 获取能转换为long的字符串
     */
    public static String getStringForLong(Object obj) {
        if (obj != null && PATTERN_NUMBER_LONG.matcher(obj.toString().trim()).matches()) {
            return obj.toString().trim();
        }
        return null;
    }

    /**
     * 获取能转换为Integer的字符串
     */
    public static String getStringForInteger(Object obj) {
        if (obj != null && PATTERN_NUMBER_INTEGER.matcher(obj.toString().trim()).matches()) {
            return obj.toString().trim();
        }
        return null;
    }

    /**
     * 输入非空对象返回(List<String>) obj，否则返回null
     */
    public static List<String> getList(Object obj) {
        return getList(obj, "");
    }

    /**
     * * 输入非空对象返回(List<T>) obj，否则返回null
     *
     * @param obj 输入需要转换的对象
     * @param <T> List<T> 范型类</T>
     * @return
     */
    public static <T> List<T> getList(Object obj, T returnType) {
        if (obj instanceof List) {
            return (List<T>) obj;
        }
        return null;
    }

}
