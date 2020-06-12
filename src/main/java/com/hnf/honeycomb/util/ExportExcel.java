package com.hnf.honeycomb.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.OutputStream;
import java.util.List;

/**
 * @author hnf
 */
public class ExportExcel {

    /**
     * 显示的导出表的标题
     */
    private String title;
    /**
     * 导出表的列名
     */
    private String[] rowName;
    private List<List<Object>> dataList;
    private List<String[]> query;

    /**
     * 构造函数，传入要导出的数据
     *
     * @param title    标题
     * @param rowName  名字
     * @param dataList 内容
     */
    public ExportExcel(String title, List<String[]> query, String[] rowName, List<List<Object>> dataList) {
        this.dataList = dataList;
        this.rowName = rowName;
        this.query = query;
        this.title = title;
    }

    /**
     * 导出数据
     */
    public void export(OutputStream out) {
        try {
            //创建工作薄对象
            HSSFWorkbook workbook = new HSSFWorkbook();
            //创建工作表对象
            HSSFSheet sheet = workbook.createSheet();
            //创建工作表的行//设置第一行，从零开始
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue(title);
            CellRangeAddress region = new CellRangeAddress(0, 0, 0, 5);
            sheet.addMergedRegion(region);

            int size = 0;
            if (query != null) {
                size = query.size();
                // 定义查询条件
                // 将列头设置到sheet的单元格中
                for (int n = 0; n < size; n++) {
                    HSSFRow rowRowName = sheet.createRow(n + 1);
                    for (int i = 0; i < query.get(n).length; i++) {
                        HSSFCell cellRowName = rowRowName.createCell(i);
                        cellRowName.setCellValue(query.get(n)[i]);
                    }
                }
            }

            // 定义所需列数
            int columnNum = rowName.length;
            HSSFRow rowRowName = sheet.createRow(size + 1);

            // 将列头设置到sheet的单元格中
            for (int n = 0; n < columnNum; n++) {
                HSSFCell cellRowName = rowRowName.createCell(n);
                HSSFRichTextString text = new HSSFRichTextString(rowName[n]);
                cellRowName.setCellValue(text);
            }

            // 将查询到的数据设置到sheet对应的单元格中
            for (int i = 0; i < dataList.size(); i++) {
                // 遍历每个对象
                List<Object> list = dataList.get(i);
                // 创建所需的行数
                HSSFRow row = sheet.createRow(i + size + 2);
                for (int j = 0; j < list.size(); j++) {
                    HSSFCell cell = row.createCell(j);
                    cell.setCellValue(list.get(j).toString());
                }
            }

            try {

                workbook.write(out);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception ignored) {

        }
    }

}
