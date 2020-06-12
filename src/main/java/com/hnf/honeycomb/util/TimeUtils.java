package com.hnf.honeycomb.util;

import com.mongodb.BasicDBObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * 时间转换的工具类
 *
 * @author yy
 */
public class TimeUtils {
    private static final Integer WEEKCUT = 8;
    private static final Integer HALFMONTHCUT = 16;
    private static final Integer MONTHCUT = 31;
    private static final Integer THREEMONTHCUT = 91;
    private static final Integer HALFYEARCUT = 181;

    /**
     * 对传入的一个时间进行切割
     *
     * @param date
     * @param timeLimit
     * @return
     */
    public static List<Date> timeParse(Date date, Integer timeLimit) {
        List<Date> list = new ArrayList<Date>();
        switch (timeLimit) {
            case Utils.WEEK:
                list = parse(date, WEEKCUT, Calendar.DAY_OF_YEAR);
                break;
            case Utils.HALFMONTH:
                list = parse(date, HALFMONTHCUT, Calendar.DAY_OF_YEAR);
                break;
            case Utils.MONTH:
                list = parse(date, MONTHCUT, Calendar.DAY_OF_YEAR);
                break;
            case Utils.THREEMONTH:
                list = parse(date, THREEMONTHCUT, Calendar.DAY_OF_YEAR);
                break;
            case Utils.HALFYEAR:
                list = parse(date, HALFYEARCUT, Calendar.DAY_OF_YEAR);
                break;
            default:
                break;
        }
        return list;
    }

    /**
     * 通过时间以及时间间隔，计算出其的开始时间以及结束时间
     *
     * @param date      传入的时间
     * @param timeLimit 传入的时间间隔
     * @return 计算后的时间数组，第一个元素为最大时间，第二个元素为最小时间
     */
    public static List<Date> calDate(Date date, Integer timeLimit) {
        List<Date> list = new ArrayList<Date>();
        switch (timeLimit) {
            case Utils.WEEK:
                list = addDate(date, -7);
                break;
            case Utils.HALFMONTH:
                list = addDate(date, -15);
                break;
            case Utils.MONTH:
                list = addDate(date, -30);
                break;
            case Utils.THREEMONTH:
                list = addDate(date, -90);
                break;
            case Utils.HALFYEAR:
                list = addDate(date, -180);
                break;
            //用于计算单个的天
            case 23:
                list = addDate(date, 1);
                break;
            default:
                break;
        }
        return list;
    }

    public static List<Date> addDate(Date date, Integer adds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, adds);
        return Arrays.asList(calendar.getTime(), date);
    }

    /**
     * 将传入的时间进行切割
     *
     * @param date        前端传入的时间
     * @param cut         转换后需要的类型
     * @param timeBetween 需要切割的时间间隔
     * @return 转换后的日期数组
     */
    private static List<Date> parse(Date date, Integer cut, Integer timeBetween) {
        Calendar calendar = Calendar.getInstance();
        List<Date> list = new ArrayList<Date>();
        for (int i = 0; i < cut; i++) {
            list.add(date);
            calendar.setTime(date);
            calendar.add(timeBetween, -1);
            date = calendar.getTime();
        }
        return list;
    }

    /**
     * 增加小时的方法
     *
     * @param date  传入的日期
     * @param filed 增加的时间
     * @return 加减后的时间
     */
    public static Date addHours(Date date, Integer filed) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, filed);
        return calendar.getTime();
    }

    /**
     * 日期加减分的方法
     *
     * @param date  加减前的日期
     * @param filed 加减的分钟数
     * @return 进行日期加减后的日期
     */
    public static Date addMinutes(Date date, Integer filed) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, filed);
        return calendar.getTime();
    }

    /**
     * ֵ 获取一个数组中最大以及最小的日期
     *
     * @param dates 传入需要判断的日期集合
     * @return 最大以及最小时间的集合
     * 其中第一个元素的是最大时间，第二个元素为最小时间
     */
    public static List<Date> getMaxAndMin(List<List<Date>> dates) {
        Date max = dates.get(0).get(0);
        Date min = dates.get(0).get(0);
        for (List<Date> date : dates) {
            for (Date d : date) {
                if (max.getTime() < d.getTime()) {
                    max = d;
                }
                if (min.getTime() > d.getTime()) {
                    min = d;
                }
            }
        }
        return Arrays.asList(max, min);
    }

    /**
     * 通过对应的日期格式的字符串获取其的时间毫秒数
     *
     * @param date 对应的时间格式字符串
     * @return 返回转换后的long数据
     */
    public static Long getLongFromDateStr(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Long time = null;
        try {
            time = sdf.parse(date).getTime();

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 通过对应的日期格式查询出增加一天的long型数据
     *
     * @param date 传入的日期字符串
     * @return 返回对应的long型数据
     */
    public static Long getAddLongFromDateStr(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Long time = null;
        try {
            //			time = sdf.parse(date).getTime();
            Date date1 = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            date1 = calendar.getTime();
            time = date1.getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 通过对应的日期字符串获取对应的日期格式时间
     *
     * @param date 传入的日期字符串
     * @return 返回转换后的日期
     */
    public static Date getDateFromDateStr(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date time = null;
        try {
            time = sdf.parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    //	public static void main(String[] args) {
    //		try {
    //			Date date1 = DateUtils.parseDate("2017-02-04 12:12:12", new String[]{"yyyy-MM-dd HH:mm:ss"});
    //			System.out.println(date1);
    //		} catch (ParseException e) {
    //			e.printStackTrace();
    //		}
    //
    //	}
    public static void main(String[] args) {
        Date date = TimeUtils.parseStrToDate("2017年12月27日 12:12:12");
        System.out.println("date:" + date);
    }

    /**
     * 将对应的字符串装换为对应的时间
     *
     * @param date
     * @return
     */
    public static Date parseStrToDate2(String date) {
        //2017年12月21日 17:35:29
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date time = new Date();
        try {
            time = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 将对应的字符串装换为对应的时间
     *
     * @param date
     * @return
     */
    public static Date parseStrToDate(String date) {
        //2017年12月21日 17:35:29
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date time = new Date();
        try {
            time = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }


    /**
     * 通过选择的时间范围查询对应的条件
     *
     * @param type      对应的选择条件
     * @param startTime 自定义时间的开始时间
     * @param endTime   自定义时间的结束时间
     * @return 返回结果
     */
    public static BasicDBObject getSearchStartDateAndEndDateByTimeSelectType(
            Integer type, Long startTime, Long endTime) {
        if (startTime != null && endTime != null) {
            type = 4;
        } else if (type == null) {
            type = 0;
        }
        BasicDBObject timeQuery = new BasicDBObject();
        Date[] dates = new Date[2];
        ZonedDateTime current = ZonedDateTime.now();
        Date now = Date.from(current.toInstant());
        switch (type) {
            case 0://代表全部
                return timeQuery;
            case 1://代表一个月
                dates[0] = now;
                dates[1] = Date.from(current.minusMonths(1).toInstant());
                break;
            case 2://代表3个月
                dates[0] = now;
                dates[1] = Date.from(current.minusMonths(3).toInstant());
                break;
            case 3://代表一年
                dates[0] = now;
                dates[1] = Date.from(current.minusYears(1).toInstant());
                break;
            case 4://代表自定义
                dates[0] = new Date(endTime);
                dates[1] = new Date(startTime);
                break;
            default:
                throw new RuntimeException("选择的时间范围不合规");
        }

        timeQuery.append("$gte", dates[1]).append("$lte", dates[0]);
        return timeQuery;
    }

    /**
     * 根据时间段生成查询条件
     *
     * @param startDate 开始时间点
     * @param endDate   结束时间点
     * @return
     */
    public static BasicDBObject getTimeNode(String startDate, String endDate) {
        BasicDBObject tQuery = new BasicDBObject();
        Date startDate1 = null;
        Date endDate1 = null;
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                startDate1 = sdf.parse(startDate);
                endDate1 = sdf.parse(endDate);
                Double timeStart = (double) (startDate1.getHours() * 60 * 60 + startDate1.getMinutes() * 60);
                Double timeEnd = (double) (endDate1.getHours() * 60 * 60 + endDate1.getMinutes() * 60);
                if (timeStart != null && timeEnd != null) {
                    tQuery.append("$gte", timeStart);
                    tQuery.append("$lte", timeEnd);
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tQuery;
    }

    public static Date parseStrToDate1(String date) {
        //2017年12月21日 17:35:29
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        try {
            time = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}