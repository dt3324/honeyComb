package com.hnf.honeycomb.serviceimpl.user;

import com.hnf.honeycomb.service.user.UserDeviceService;
import com.hnf.honeycomb.util.RedisUtilNew;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDeviceServiceImplTest {

    @Autowired
    private RedisUtilNew redisUtilNew;
    @Autowired
    private UserDeviceService userDeviceService;

    @Test
    public void aggregateDeviceInfo() {
        redisUtilNew.lPush("fetchChange", "1");
        System.out.println("当前未被处理消息条数为:");
    }

    @Test
    public void fetchPercentage() {
    }

    @Test
    public void fetchEntiretyPercentage() {
    }

    @Test
    public void fetchQualityExportExcel() throws IOException {
//        Map<String, String> s = userDeviceService.fetchQualityExportExcel("", "51072300000",
//                1, 20, "1", "1", "1", "1", "2019-1", "2020-2", null);
//        System.out.println(s);
    }

    @Test
    public void aggregateCaseDevicePersonInfo() {
    }

    //创建Excel对象
    @Test
    public void testExcel2() throws IOException {
        //创建工作薄对象
        HSSFWorkbook workbook=new HSSFWorkbook();//这里也可以设置sheet的Name
        //创建工作表对象
        HSSFSheet sheet = workbook.createSheet();
        //创建工作表的行
        HSSFRow row = sheet.createRow(0);//设置第一行，从零开始
        row.createCell(0).setCellValue(new Date());//第一行第一列为日期
        CellRangeAddress region=new CellRangeAddress(0, 0, 0, 5);
        sheet.addMergedRegion(region);
        row.createCell(8).setCellValue("aaaaaaaaaaaa");//第一行第三列为aaaaaaaaaaaa
        workbook.setSheetName(0,"sheet的Name");//设置sheet的Name

        //文档输出
        FileOutputStream out = new FileOutputStream("D:/aaaa.xls");
        workbook.write(out);
        out.close();
    }


}