package com.hnf.honeycomb.util;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 常量类
 *
 * @author lsj
 */

public class Constants {
    public static final String RESOURCES_PATH = "D:/temp/";
    public static final String SUFFIX = ".0";


    /**
     * 转换成double
     */
    //只保留小数点最后一位
    public static DecimalFormat INTEGER_DF = new DecimalFormat("#");

    public static Double getDoubleCellValueOne(Cell cell) {
        try {
            BigDecimal bd = new BigDecimal(getStringCellValue(cell));
            BigDecimal bd2 = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
            return Double.parseDouble(bd2.toString());
        } catch (Exception e) {
        }
        return Double.valueOf(0.0D);
    }

    public static String getStringValueTwo(Cell cell) {
        try {
            return INTEGER_DF.format(cell.getNumericCellValue());
        } catch (Exception e) {
        }
        return "";

    }

    /**
     * 转换成double
     */
    public static Double getDoubleCellValue(Cell cell) {
        try {
            return new Double(Double.parseDouble(getStringCellValue(cell)));
        } catch (Exception e) {
        }
        return Double.valueOf(0.0D);
    }

    /**
     * 把从excel中读取出来的数据的格式转换成String
     */
    public static String getStringCellValue(Cell cell) {
        String strCell = "";
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case FORMULA:
                try {
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        strCell = sdf.format(date);
                        break;
                    }
                    strCell = String.valueOf(cell.getStringCellValue());
                } catch (IllegalStateException e) {
                    strCell = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case STRING:
                strCell = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    break;
                }
                if (String.valueOf(cell.getNumericCellValue()).endsWith(SUFFIX)) {
                    strCell = INTEGER_DF.format(cell.getNumericCellValue());
                    break;
                }
                strCell = String.valueOf(cell.getNumericCellValue());

                break;
            case BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            case BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
        }

        return strCell;
    }

    /**
     * 把从excel中读取出来的日期格式的数据转换成常用日期格式
     */
    public static String getDateCellValue(Cell cell, String format) {
        String strCell = "";
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case _NONE:
                try {
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        strCell = sdf.format(date);
                        break;
                    }
                    strCell = String.valueOf(cell.getStringCellValue());
                } catch (IllegalStateException e) {
                    strCell = String.valueOf(cell.getNumericCellValue());
                }
                break;
            default:
                strCell = "";
        }
        return strCell;
    }

    /**
     * 把从excel中读取出来的电话号码的数据转换成number格式
     */
    public static String getPhoneNumber(Cell cell) {
        String strCell = "";
        if (cell == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#");
        switch (cell.getCellType()) {
            case NUMERIC:
                strCell = df.format(cell.getNumericCellValue());
                break;
            case STRING:
                strCell = String.valueOf(cell.getStringCellValue());
                break;
            default:
                strCell = "";
                break;
        }
        return strCell;
    }
}

