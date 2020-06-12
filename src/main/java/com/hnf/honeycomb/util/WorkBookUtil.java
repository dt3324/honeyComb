package com.hnf.honeycomb.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author ：zjl
 * @date ：Created in 2019/5/15 14:22
 * @Description :
 */
@Slf4j
public class WorkBookUtil {

    private static final String EXCEL_XLS = ".xls";
    private static final String EXCEL_XLSX = ".xlsx";

    /**
     * 判断Excel的版本,获取Workbook
     * workbook是类型，是工作簿，一个EXCEL文件（包含多个工作表）就是一个工作簿
     * workbooks是application的一个属性，是当前EXCEL进程打开的所有工作簿数组，使用workbooks[1]、workbooks[2]可以访问他们。
     * 理解一下一个Excel的文件的组织形式，一个Excel文件对应于一个workbook(HSSFWorkbook)，
     * 一个workbook可以有多个sheet（HSSFSheet）组成，一个sheet是由多个row（HSSFRow）组成，一个row是由多个cell（HSSFCell）组成。
     * 基本操作步骤：
     * 1、用HSSFWorkbook打开或者创建“Excel文件对象”
     * 2、用HSSFWorkbook对象返回或者创建Sheet对象
     * 3、用Sheet对象返回行对象，用行对象得到Cell对象
     * 4、对Cell对象读写。
     *
     * @param file
     * @return
     */
    public static Workbook getWorkbook(File file) {
        Workbook wb = null;
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);  //与根据File类对象的所代表的实际文件建立链接创建fileInputStream对象
            if (file.getName().endsWith(EXCEL_XLS)) {
                wb = new HSSFWorkbook(in);  //  HSSFWorkbook     excel的文档对象
            } else if (file.getName().endsWith(EXCEL_XLSX)) {
                wb = new XSSFWorkbook(in);  //  HSSFWorkbook     excel的文档对象
            }
        } catch (IOException e) {
            log.error("获取workbook异常{}", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("workbook输入流关闭异常！{}", e);
                }
            }
        }
        return wb;
    }

    /**
     * 根据文件类型返回不同的workbook
     * @param file
     * @return
     */
    public static Workbook getWorkBookByInputStream(MultipartFile file) {
        InputStream is = null;
        Workbook wb = null;

        try {
            is = file.getInputStream(); //能获取表单上传文件基本信息(文件名和后缀名)
            /**
             * 通过文件后缀名称判断excel 属于新版还是老板
             * .xlsx 后缀是高级版本要用XSSFWorkbook对象解析
             * .xls 后缀是低级版本,使用HSSFWorkbook 解析
             */
            if (!file.isEmpty() && file.getOriginalFilename().endsWith("xlsx")) {
                try {
                    wb = new XSSFWorkbook(is);
                } catch (IOException e) {
                    log.error("文件解析失败,请确认导入正确的话单文件", e);
                }
            } else if (!file.isEmpty() && file.getOriginalFilename().endsWith("xls")) {
                try {
                    wb = new HSSFWorkbook(is);
                } catch (IOException e) {
                    log.error("文件解析失败");
                }
            }
        } catch (IOException e) {
            log.error("返回workbook输入流异常{}", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("workbook输入流关闭异常！{}", e);
                }
            }
        }
        return wb;
    }

    /**
     * 获取MF文件流转换成File文件
     * @param ins
     * @param file
     */
    public static void inputStreamToFile(InputStream ins,File file){
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while( (bytesRead = ins.read(buffer,0,8192)) != -1){
                os.write(buffer,0,bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            log.error("转换出错",e);
        }
    }

}
