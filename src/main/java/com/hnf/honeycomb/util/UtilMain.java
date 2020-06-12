package com.hnf.honeycomb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hnf
 */
public class UtilMain {

    public static final int DAY = 0;
    public static final int WEEK = 1;
    public static final int HALFMONTH = 2;
    public static final int MONTH = 3;
    public static final int THREEMONTH = 4;
    public static final int HALFYEAR = 5;
    public static final String DATEFORMAT = "yyyy/MM/dd";
    public static final String IMEI = "IMEI";
    public static final String MAC = "MAC";
    public static final String COLL = "location3";

    public static List<Date> timeFormat(String date, Integer timeLimit) {

        List<Date> list = new ArrayList<Date>();
        try {
            Calendar calendar = Calendar.getInstance();
            if (timeLimit == WEEK) {
                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.WEEK_OF_MONTH, -1);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            } else if (timeLimit == HALFMONTH) {
                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.DAY_OF_MONTH, -15);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            } else if (timeLimit == MONTH) {
                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.MONTH, -1);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            } else if (timeLimit == THREEMONTH) {
                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.MONTH, -3);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            } else if (timeLimit == HALFYEAR) {
                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.MONTH, -6);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            } else if (timeLimit == DAY) {

                calendar.setTime(new SimpleDateFormat(DATEFORMAT).parse(date));
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                Date start = calendar.getTime();
                list.add(start);
                list.add(new SimpleDateFormat(DATEFORMAT).parse(date));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Double radius(Double meter) {
        return meter * 0.621 / 1000 / 3963.192;
    }

    /**
     * 将对应的字符串转换为对应的日期
     *
     * @param date 对应的日期字符串
     * @return 转换后的标准日期格式
     */
    public static Date parse(String date) {
        Date d = null;
        try {
            d = new SimpleDateFormat(DATEFORMAT).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Date parseDate(String date) {
        Date d = null;
        try {
            d = new SimpleDateFormat(DATEFORMAT).parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            d = calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static List<Date> cutDate(Date startDate, Date endDate) {
        List<Date> results = new ArrayList<Date>();
        results.add(endDate);
        while (startDate.getTime() < endDate.getTime()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            results.add(calendar.getTime());
            endDate = calendar.getTime();
        }
        return results;
    }


    public static List<String> byDate(String endDate) {

        List<String> results = new ArrayList<String>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date d;
        try {
            d = new SimpleDateFormat("yyyyMMdd").parse(endDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
            Date startDate = calendar.getTime();
            results.add(sdf.format(d));

            while (startDate.getTime() < d.getTime()) {
                calendar.setTime(d);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                results.add(sdf.format(calendar.getTime()));
                d = calendar.getTime();
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return results;
    }


    public static List<Long> strToLong(List<String> imeis) {
        List<Long> list = new ArrayList<Long>();
        for (String imei : imeis) {
            list.add(Long.valueOf(imei));
        }
        return list;
    }

    public static List<String> longToStr(List<Long> imeis) {
        List<String> list = new ArrayList<String>();
        for (Long imei : imeis) {
            list.add(imei.toString());
        }
        return list;
    }

    public static List<List<Double>> toPointArray(String str) {
        String replace1 = str.replaceAll("\\[", "");
        //		System.out.println(replace1);
        String replace2 = replace1.replaceAll("\\]", "");
        //		System.out.println(replace2);
        String[] strs = replace2.split(",");
        List<List<Double>> points = new ArrayList<List<Double>>();
        for (int i = 0; i < strs.length; i++) {
            if (i % 2 == 0) {
                List<Double> point = new ArrayList<Double>();
                points.add(Arrays.asList(Double.valueOf(strs[i]),
                        Double.valueOf(strs[i + 1])));
            }
        }
        return points;
    }

    public static List<Date> toDateArray(String dateStr) {
        //[1485933569396,1485933532073,1485933499256,1485933462101]
        String replace1 = dateStr.replaceAll("\\[", "");
        String replace2 = replace1.replaceAll("\\]", "");
        String[] strs = replace2.split(",");
        List<Date> date = new ArrayList<Date>();
        for (int i = 0; i < strs.length; i++) {
            date.add(new Date(Long.valueOf(strs[i])));
        }

        return date;
    }

    public static String getType(String selectType) {
        if ("qq".equals(selectType)) {
            return "qqNum";
        }
        if ("wx".equals(selectType)) {
            return "wxNum";
        }
        if ("phone".equals(selectType)) {
            return "phoneNum";
        }
        return null;
    }

    public static String read(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        int available = fis.available();
        byte[] buf = new byte[available];
        fis.read(buf);
        String string = new String(buf);
        fis.close();
        return string;
    }

}
