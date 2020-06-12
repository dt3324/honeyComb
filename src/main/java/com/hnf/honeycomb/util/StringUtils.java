package com.hnf.honeycomb.util;


import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.MapUtils.getString;

/**
 * 用于字符串操作的工具类
 *
 * @author yy
 */
public class StringUtils {

    /**
     * 判断对应的字符串是否为空
     *
     * @param str 所需判断的字符串
     * @return 返回是否为空
     */
    public static Boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }



    /**
     * 将对应集合中的数据中对应的字符串转换为新的字符串
     *
     * @param list    需替换的list
     * @param old     老的字符串
     * @param replace 新的字符串
     * @return 返回替换后的结果
     */
    public static List<String> replaceArray(List<String> list, String old, String replace) {
        if (old == null || replace == null) {
            return null;
        }
        List<String> rst = null;
        if (list != null) {
            rst = list.stream().map((t) -> {
                String x = t.replaceAll(old, replace);
                x = x.toLowerCase();
                return x;
//                return t;
            }).collect(Collectors.toList());
        }
        return rst;
    }

    /**
     * 将对应list中的空字符串移除
     *
     * @param list list集合
     * @return 返回去除null及empty字符串的集合
     */
    public static List<String> removeNullStrAndEmptyStr(List<String> list) {
        return list.stream().filter(StringUtils::isNotEmptyStr).collect(Collectors.toList());
    }

    /**
     * 将对应碰撞的账号进行去重
     *
     * @param array 传入的数组
     * @return 返回的数组
     */
    public static String[] bongDuplicateremove(String[] array) {
        List<String> list = new ArrayList<>();
        list.add(array[0]);
        for (int i = 1; i < array.length; i++) {
            if (!list.toString().contains(array[i])) {
                list.add(array[i]);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 获取高亮文本
     *
     * @param normalText
     * @param highLightWord
     * @return High Light Text
     */
    public static String getHighLightText(String normalText, String highLightWord) {
        return normalText.replaceAll(highLightWord, "<span style=\"color:red\" class=\"high-light\">" + highLightWord + "</span>");
    }

    /**
     * @param list          集合
     * @param key           需要高亮的主键
     * @param highLightWord 需要高亮的关键词
     * @param <T>           Document等
     */
    public static <T extends Map<String, Object>> void getHighLightText(List<T> list, String key, String highLightWord) {
        list.parallelStream().forEach(map ->
                map.put(
                        key,
                        getHighLightText(
                                getString(map, key, ""), highLightWord)
                )
        );
    }

    /**
     * 获取上一版本的单位代码
     *
     * @param departmentCode 当前code
     * @return 上一版本部门code
     */
    public static String getOldDepartmentCode(String departmentCode) {
        departmentCode = departmentCode.trim();
        if (departmentCode.endsWith("000000000")) {
            // 省级单位
            return departmentCode.substring(0, 2);
        }
        if (departmentCode.endsWith("0000000")) {
            // 市级单位
            return departmentCode.substring(0, 4);
        }
        if (departmentCode.endsWith("00000")) {
            // 区县级单位
            return departmentCode.substring(0, 6);
        }
        // 部门
        return departmentCode;
    }

    /**
     * 补全部门code
     *
     * @return
     */
    public static String getNewDepartmentCode(String departmentCode) {
        int length = departmentCode.length();
        switch (length) {
            case 2:
                departmentCode += "000000000";
                break;
            case 4:
                departmentCode += "0000000";
                break;
            case 6:
                departmentCode += "00000";
                break;
            default:
                break;
        }
        return departmentCode;
    }

    /**
     * 从部门代码判断部门等级
     */
    public static Integer getDepartmentType(String departmentCode) {
        if (departmentCode == null || departmentCode.trim().isEmpty()) {
            return null;
        }
        if (departmentCode.endsWith("000000000")) {
            return 1;
        }
        if (departmentCode.endsWith("0000000")) {
            return 2;
        }
        if (departmentCode.endsWith("00000")) {
            return 3;
        }
        return 4;
    }

    /**
     * 判断字符串是否不为空
     * ""   false
     * "    "   true
     * "123"    true
     *
     * @param str 输入字符串
     * @return true 不为空，false为空
     */
    public static boolean isNotEmptyStr(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * ""   false
     * "    "   true
     * "123"    true
     *
     * @param str 输入字符串
     * @return true 不为空，false为空
     */
    public static boolean isNotEmptyStr(String... str) {
        boolean result = true;
        for (String s : str) {
            result &= isNotEmptyStr(s);
        }
        return result;
    }

    /**
     * 判断字符串长度是否在给定范围内
     *
     * @param str       输入字符串
     * @param minLength 字符串极小值限定
     * @return
     */
    public static boolean legalString(String str, int minLength) {
        return legalString(str, minLength, Long.MAX_VALUE);
    }

    /**
     * 判断字符串长度是否在给定范围内
     *
     * @param str       输入字符串
     * @param maxLength 字符串极大值限定
     * @return
     */
    public static boolean legalString(String str, long maxLength) {
        return legalString(str, 0, maxLength);
    }

    /**
     * 判断字符串长度是否在给定范围内
     *
     * @param str       输入字符串
     * @param minLength 字符串极小值限定
     * @param maxLength 字符串极大值限定
     * @return 是否合法
     */
    public static boolean legalString(String str, int minLength, long maxLength) {
        return str != null && str.length() >= minLength && str.length() <= maxLength;
    }

    public static List<String> removeNullStrAndEmptyStrThenTrim(List<String> list) {
        return list.stream().filter(StringUtils::isNotEmptyStr).map(String::trim).collect(Collectors.toList());
    }
}
