package com.hnf.honeycomb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hnf
 */
public class TimeUtil {
    /**
     * 将字符串日期转换为对应的javaDate
     *
     * @param date 字符串日期
     * @return
     * @throws ParseException 字符串不符合的原因
     */
    public static Date strToDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            return sdf.parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过对应
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Map<String, Date> getDateFromDateRepresent(String startDate, String endDate) {
        Map<String, Date> map = new HashMap<>();
        if (startDate != null && endDate != null && !startDate.trim().isEmpty() && !endDate.trim().isEmpty()) {
            if (startDate.trim().length() > 1) {
                map.put("startDate", new Date(com.hnf.honeycomb.util.Utils.timeToString(startDate)));
            } else {
                map.put("startDate", new Date(com.hnf.honeycomb.util.Utils.timeFormat(startDate)));
            }
            if (endDate.trim().length() > 1) {
                map.put("endDate", new Date(com.hnf.honeycomb.util.Utils.timeToString(endDate)));
            } else {
                map.put("endDate", new Date(Utils.timeFormat(endDate)));
            }
        }
        return map;
    }

    /**
     * 将传入的对应日期字符串转换为对应的日期时间戳
     *
     * @param str 对应的日期字符串
     * @return 返回对应的日期时间戳
     */
    public static Long parseDateFromStr(String str) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
    }

    /**
     * 将传入的日期字符串转换为对应的日期加一天
     *
     * @param str 对应的日期字符串
     * @return 返回对应的日期时间戳
     */
    public static Long parseDateAndAddOneDayFromStr(String str) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(str));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
    }

    /**
     * 将es中的查询出的时间转换为对应的日期格式
     *
     * @param date 传入的日期字符串 2017-08-10T07:25:43 yyyy-MM-dd
     * @return 返回对应的日期
     */
    public static Long parseStringToDate(String date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parseDate = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date.replaceAll("T", " ")));
            calendar.add(Calendar.HOUR, 8);
            parseDate = calendar.getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parseDate.getTime();
    }

    public static String localToGTM(String localDate) {
        SimpleDateFormat format;
        format = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Date resultDate;
        long resultTime = 0;
        if (null == localDate) {
            return localDate;
        } else {
            try {
                format.setTimeZone(TimeZone.getDefault());
                resultDate = format.parse(localDate);
                resultTime = resultDate.getTime();
                format.setTimeZone(TimeZone.getTimeZone("GMT00:00"));
                return format.format(resultTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return localDate;
    }

    public static void main(String[] args) {
////		System.out.println("date:"+TimeUtil.parseStringToDate("2017-08-10T07:25:43"));
//		System.out.println(TimeUtil.LocalToGTM("2017/01/01"));
//		Date nowTime = new Date(); // 要转换的时间
//		Calendar cal = Calendar.getInstance();
//		 cal.setTimeInMillis(nowTime.getTime());
//
////		 Log.i("OTH","北京时间：" + cal.getTime().toString().substring(0, 19));
////		 System.out.println(".......:"+cal.getTime().toString());
//		 cal.add(Calendar.HOUR, -8);
////		 System.out.println("...................:"+cal.getTime());
////		 Log.i("OTH","格林威治时间：" + cal.getTime());
        String str = "2017/01/01";
        System.out.println(TimeUtil.parseDateAndAddOneDayFromStr(str));
    }
}
