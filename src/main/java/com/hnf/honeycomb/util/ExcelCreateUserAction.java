package com.hnf.honeycomb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hnf
 */
public class ExcelCreateUserAction {
    private static ExcelCreateUserAction ourInstance = new ExcelCreateUserAction();
    private static Pattern PATTERN_IS_DATE
            = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
    private static Pattern PATTERN_IS_NUMBER = Pattern.compile("^\\d+$");

    private ExcelCreateUserAction() {
    }

    public static ExcelCreateUserAction getInstance() {
        return ourInstance;
    }

    public String iDCardValidate(String iDStr) throws ParseException {
        // 记录错误信息
        String tipInfo = null;
        String ai = "";
        // 判断号码的长度 18位
        if (iDStr.length() != 18) {
            tipInfo = "请输入18位身份证号码";
            return tipInfo;
        }
        // 18位身份证前17位位数字
        if (iDStr.length() == 18) {
            ai = iDStr.substring(0, 17);
        }
        if (isNumeric(ai) == false) {
            tipInfo = "身份证号码除最后一位外都应为数字";
            return tipInfo;
        }

        // 判断出生年月是否有效// 年份
        String strYear = ai.substring(6, 10);
        // 月份
        String strMonth = ai.substring(10, 12);
        // 日期
        String strDay = ai.substring(12, 14);
        if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
            tipInfo = "身份证出生日期无效。";
            return tipInfo;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                    || (gc.getTime().getTime() - s.parse(
                    strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                tipInfo = "身份证生日不在有效范围";
                return tipInfo;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            tipInfo = "身份证月份无效";
            return tipInfo;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            tipInfo = "身份证日期无效";
            return tipInfo;
        }

        // 判断地区码是否有效
        Hashtable areacode = getAreaCode();
        //如果身份证前两位的地区码不在Hashtable，则地区码有误
        if (areacode.get(ai.substring(0, 2)) == null) {
            tipInfo = "身份证地区编码错误";
            return tipInfo;
        }

        if (isVarifyCode(ai, iDStr) == false) {
            tipInfo = "身份证校验码无效，不是合法的身份证号码";
            return tipInfo;
        }

        return tipInfo;
    }


    /**
     * 判断第18位校验码是否正确
     * 第18位校验码的计算方式：
     * 　　1. 对前17位数字本体码加权求和
     * 　　公式为：S = Sum(Ai * Wi), i = 0, ... , 16
     * 　　其中Ai表示第i个位置上的身份证号码数字值，Wi表示第i位置上的加权因子，其各位对应的值依次为： 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
     * 　　2. 用11对计算结果取模
     * 　　Y = mod(S, 11)
     * 　　3. 根据模的值得到对应的校验码
     * 　　对应关系为：
     * 　　 Y值：     0  1  2  3  4  5  6  7  8  9  10
     * 　　校验码： 1  0  X  9  8  7  6  5  4  3   2
     */
    private boolean isVarifyCode(String ai, String iDStr) {
        String[] varifyCode = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        String[] wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum = sum + Integer.parseInt(String.valueOf(ai.charAt(i))) * Integer.parseInt(wi[i]);
        }
        int modValue = sum % 11;
        String strVerifyCode = varifyCode[modValue];
        ai = ai + strVerifyCode;
        if (iDStr.length() == 18) {
            if (ai.equals(iDStr) == false) {
                return false;
            }
        }
        return true;
    }


    /**
     * 将所有地址编码保存在一个Hashtable中
     *
     * @return Hashtable 对象
     */

    private Hashtable getAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 判断字符串是否为数字,0-9重复0次或者多次
     *
     * @param strnum
     * @return
     */
    private boolean isNumeric(String strnum) {
        Matcher isNum = PATTERN_IS_NUMBER.matcher(strnum);
        return isNum.matches();
    }

    /**
     * 功能：判断字符串出生日期是否符合正则表达式：包括年月日，闰年、平年和每月31天、30天和闰月的28天或者29天
     *
     * @return
     */
    private boolean isDate(String strDate) {
        Matcher m = PATTERN_IS_DATE.matcher(strDate);
        return m.matches();
    }
}
