package com.hnf.honeycomb.util;

import com.hnf.crypte.MD5Util;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hnf
 */
public class Utils {

    public static final int DAY = 0;
    public static final int WEEK = 1;
    public static final int HALFMONTH = 2;
    public static final int MONTH = 3;
    public static final int THREEMONTH = 4;
    public static final int HALFYEAR = 5;
    public static final int END = 6;
    public static final String DATEFORMAT = "yyyy/MM/dd HH:mm:ss";
    private static final String DATE_STAD = "yyyy-MM-dd HH:mm:ss";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    public static Long timeFormat(String timeLimit) {

        int time = Integer.valueOf(timeLimit);
        //		List<Long> list = new ArrayList<Long>();
        //		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {


            Date date = new Date(System.currentTimeMillis());
            if (time == WEEK) {
                calendar.setTime(date);
                calendar.add(Calendar.WEEK_OF_MONTH, -1);
                Date start = calendar.getTime();
                calendar.setTime(start);
                //				list.add(calendar.getTimeInMillis());

            } else if (time == HALFMONTH) {
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, -15);
                Date start = calendar.getTime();
                calendar.setTime(start);

                //				list.add(calendar.getTimeInMillis());

            } else if (time == MONTH) {
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                Date start = calendar.getTime();
                calendar.setTime(start);

                //				list.add(calendar.getTimeInMillis());

            } else if (time == THREEMONTH) {
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -3);
                Date start = calendar.getTime();
                calendar.setTime(start);

                //				list.add(calendar.getTimeInMillis());

            } else if (time == HALFYEAR) {
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -6);

                Date start = calendar.getTime();
                calendar.setTime(start);

                //				list.add(calendar.getTimeInMillis());

            } else if (time == DAY) {

                //				list.add(calendar.getTimeInMillis());
                return (long) 0;
            } else if (time == END) {

                //				list.add(calendar.getTimeInMillis());
                return System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }

    public static Long timeToString(String someDay) {

        Calendar calendar = Calendar.getInstance();

        try {

            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(someDay));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }

//	/**
//	 * 将日期字符串转换为对应的Long型毫秒数
//	 * @param date 对应的字符串日期
//	 * @return 对应的时间毫秒数
//	 */
//	public static Long getLongMillFromStr(String date){
//		Calendar calendar = Calendar.getInstance();
//		try {
//			calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(date));
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		return calendar.getTimeInMillis();
//	}

    /**
     * 将日期字符串转换为对应的Long型毫秒数
     *
     * @param date 对应的字符串日期
     * @return 对应的时间毫秒数
     */
    public static Date getLongMillFromStr(String date) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(calendar.getTimeInMillis());
    }


    public static Long toString(String someDay) {

        Calendar calendar = Calendar.getInstance();

        try {

            calendar.setTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(someDay));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }


    public static String getPassword(String pwd) {
        return MD5Util.MD5(pwd);
    }

    public static String getMD5(String name, String date) {
        return MD5Util.MD5(name + date);
    }


    /**
     * 将传入的文件转换为字符串
     *
     * @param path 输入的文件
     * @return 返回内容的字符串
     * @throws FileNotFoundException 找不到路径
     * @throws IOException
     */
    public static String read(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        int available = fis.available();
        byte[] buf = new byte[available];
        fis.read(buf);
        String string = new String(buf);
        fis.close();
        return string;
    }

    public static String longToString(Long someDay) {


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String string = format.format(new Date(someDay));

        return string;

    }


    public static List<Date> getDatesBtnDay(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_STAD);
        List<Date> results = new ArrayList<Date>();
        try {
            Date startDate = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date endDate = calendar.getTime();
            results.add(startDate);
            results.add(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static List<Long> getDateDay(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_STAD);
        List<Long> results = new ArrayList<>();
        try {
            Date startDate = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            results.add(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Long endDate = calendar.getTimeInMillis();
            results.add(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 判断一个字符串是否为数字
     *
     * @param str 所需判断的字符串
     * @return 返回是否为数字字符串
     */
    public static boolean isNumeric(String str) {

        return NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 对传入对应的字符串进行对空格的区分
     *
     * @param search 传入的字符串
     * @return 返回切割后的字符
     */
    public static String getStrByCutSpace(String search) {
        String[] strs = search.split(" ");
        StringBuffer stringBuffer = new StringBuffer();

        for (String str : strs) {
            if (str != null && !str.trim().isEmpty()) {
                stringBuffer.append(" +\"");
                stringBuffer.append(str);
                stringBuffer.append("\"");
            }
        }
        //		stringBuffer.append("\"");
        System.out.println(stringBuffer);
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        //		System.out.println(isNumeric("qq123123"));
        String[] arry = new String[]{"321", "123", "213", "231", "123", "321", "123"};
        for (String i : arry) {
            System.out.println("1:" + i);
        }
        System.out.println("arry:" + arry);
        String[] newArray = Utils.bongDuplicateremove(arry);
        for (String i : newArray) {
            System.out.println("1:" + i);
        }
        System.out.println("newArray:" + newArray);
    }

    /**
     * 关系碰撞中使用
     * 用于将qq,wx,通讯录中的返回好友根据搜索结果排序
     *
     * @param qqDetails 对应的账号列表
     * @return
     */
    public static List<Map<String, Object>> sortQqDetails(List<Map<String, Object>> qqDetails) {
        if (qqDetails == null || qqDetails.isEmpty()) {
            return qqDetails;
        }
        qqDetails.sort((qq1, qq2) -> {
            Object qq1TypeObj = qq1.get("type");
            Object qq2TypeObj = qq2.get("type");
            //判定是否为空
            if (qq1TypeObj == null) {
                return 1;
            }
            HashSet<?> qq1TypeSet = HashSet.class.cast(qq1TypeObj);
            HashSet<?> qq2TypeSet = HashSet.class.cast(qq2TypeObj);
            Integer qq1Type = 0;
            Integer qq2Type = 0;
            for (Object i : qq1TypeSet) {
                qq1Type += Integer.valueOf(i.toString());
            }

            for (Object i : qq2TypeSet) {
                qq2Type += Integer.valueOf(i.toString());
            }
            if (qq1Type < qq2Type) {
                return 1;
            }
            return -1;
        });
        return qqDetails;
    }

    /**
     * 将对应碰撞的账号进行去重
     *
     * @param array 传入的数组
     * @return 返回的数组
     */
    public static String[] bongDuplicateremove(String[] array) {
        Set<String> hset = new TreeSet<>(Arrays.asList(array));
        return hset.toArray(new String[hset.size()]);
    }

    /**
     * 去重
     *
     * @param list 对应的list集合
     * @return 返回对应的数据
     */
    public static <T> List<T> getDistinctList(List<T> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public static List<Document> removeDuplicateDoc(List<Document> list, String filedName) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).getString(filedName).equals(list.get(i).getString(filedName))) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * 通过传入的一个对象,判定传入的对象是否是WX公众号
     *
     * @param impactWXDoc 对应的WX账号
     * @return 返回是否为公众号
     */
    public static Boolean isWXOfficalAccount(Document impactWXDoc) {
        if (impactWXDoc == null) {
            return false;
        }
//		System.out.println("impactWXDoc:"+impactWXDoc);
        //		System.out.println("impactWXDoc:"+impactWXDoc);
        String impactWXType = impactWXDoc.getString("type");
        String impactWXVerifyFlag = impactWXDoc.getString("verifyFlag");
        String username = impactWXDoc.getString("username");

        Boolean is = !(("0".equals(impactWXVerifyFlag)) && (!"24".equals(impactWXVerifyFlag)) && (!"33".equals(impactWXType)) && (!"35".equals(impactWXType))
                && (!"filehelper".equals(username)) && (!username.startsWith("fake_")) && (!username.endsWith("@chatroom")) && (!username.endsWith("app")));
//		System.out.println(!username.endsWith("app"));
//		System.out.println("is:"+is);
        return is;
    }


    /**
     * 通过传入一个字符串判定其是否为手机公众号
     *
     * @param phone 对应的手机号
     * @return
     */
    public static Boolean isPhoneOfficalAccount(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return true;
        }
        return phone.length() != 11;
    }

    /**
     * 将对应的Map对象转换为对应的Document对象
     *
     * @param map 对应的map对象
     * @return 返回转换后的document
     */
    public static Document mapCastDocument(Map<String, Object> map) {
        if (CollectionUtils.mapIsEmpty(map)) {
            return new Document();
        }
        return new Document(map);
    }

    public static Date addDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //把日期往后增加一天,整数  往后推,负数往前移动
        return calendar.getTime();
    }

    /**
     * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
     *
     * @param request
     * @return ip
     */
    public static String getLocalIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");

        String ip = null;
        if (realIp == null) {
            if (forwarded == null) {
                ip = remoteAddr;
            } else {
                ip = remoteAddr + "/" + forwarded.split(",")[0];
            }
        } else {
            if (realIp.equals(forwarded)) {
                ip = realIp;
            } else {
                if (forwarded != null) {
                    forwarded = forwarded.split(",")[0];
                }
                ip = realIp + "/" + forwarded;
            }
        }
        return ip;
    }

}
