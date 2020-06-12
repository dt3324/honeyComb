package com.hnf.honeycomb.accountUpload;

import com.csvreader.CsvReader;
import com.hnf.crypte.MD5Util;
import com.hnf.honeycomb.bean.AliPayInnerDoc;
import com.hnf.honeycomb.bean.BankInnerDoc;
import com.hnf.honeycomb.bean.Contans;
import com.hnf.honeycomb.bean.WxInnerDoc;
import com.hnf.honeycomb.bean.enumerations.AccountEnum;
import com.hnf.honeycomb.dao.DeviceMongoDao;
import com.hnf.honeycomb.util.FileMd5Util;
import com.hnf.honeycomb.util.MultipartFileToFileUtil;
import com.hnf.honeycomb.util.StringUtils;
import com.hnf.honeycomb.util.WorkBookUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.Document;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hnf.honeycomb.bean.Contans.A;
import static com.hnf.honeycomb.bean.Contans.C;

public class FileDetail implements Runnable {

    private File file;
    private Integer fileType;
    private String personId;
    private Date uploadTime;


    private static String DBNAME = "accountdata";
    private static String DETAIL = "accountdetail";
    private static String FILEORDER = "file_order";
    private static String PERSONID_FILE = "personId_file";

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    private static DeviceMongoDao deviceMongoDao;

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setDeviceMongoDao(DeviceMongoDao deviceMongoDao) {
        this.deviceMongoDao = deviceMongoDao;
    }

    public FileDetail(File file, Integer fileType, String personId) {
        this.file = file;
        this.fileType = fileType;
        this.personId = personId;
    }

    @Override
    public void run() {
        System.out.println("当前账单名字\t" + file.getName() + "\t文件类型" + fileType);


        switch (AccountEnum.getByValue(fileType)) {

            case YINGHANG:
                bankAccountUpload(file, personId, uploadTime);
                break;
            case WEIXIN:
                weiXinAccountUpload(file, personId, uploadTime);
                break;
            case ALIPAY:
                aliPayAccountUpload(file, personId, uploadTime);
                break;
            default:
                break;


        }

        System.out.println("任务完成  删除文件");
        MultipartFileToFileUtil.delteTempFile(file);
    }

    private void aliPayAccountUpload(File file, String personId, Date uploadTime) {
        List<Document> account_orderList = new ArrayList<>();
        List<Document> newDocData = new ArrayList<>();
        System.out.println("文件:  " + file.getName() + "  开始解析");
        String fileMd5 = FileMd5Util.calcMD5(file);
        try {
            List<String[]> csvList = getCsvList(file, "GBK");
            //获取第一行数据作为account
            String accountType = csvList.get(0)[0];
            if (!"支付宝交易记录明细查询".equals(accountType)) {
                throw new RuntimeException();
            }
            String[] phoneLin = csvList.get(1);
            String rowOne = phoneLin[0].substring(phoneLin[0].lastIndexOf(":") + 1);
            String phoneNum = rowOne.substring(0, rowOne.length() - 1).substring(1, rowOne.length() - 1).trim();
            //从第5行开始遍历文件
            for (int i = 5; i < csvList.size() - 7; i++) {
                String[] aCsvList = csvList.get(i);
                //读取有效行数据
                if (aCsvList.length > 2) {
                    Document doc = new Document();
                    Document accountOrderDoc = new Document();
                    doc.put("fileState", phoneNum);
                    accountOrderDoc.put("fileState", phoneNum);
                    AliPayInnerDoc.setBusiness_order(doc, aCsvList[A]);
                    AliPayInnerDoc.setAuchernum(doc, aCsvList[Contans.B]);
                    AliPayInnerDoc.setBusiness_time(doc, aCsvList[C]);
                    AliPayInnerDoc.setPayment_time(doc, aCsvList[Contans.D]);
                    AliPayInnerDoc.setLast_modify_time(doc, aCsvList[Contans.E]);
                    AliPayInnerDoc.setBusiness_location(doc, aCsvList[Contans.F]);
                    AliPayInnerDoc.setBusiness_type(doc, aCsvList[Contans.G]);
                    AliPayInnerDoc.setBusiness_name(doc, aCsvList[Contans.H]);
                    AliPayInnerDoc.setCommodity_name(doc, aCsvList[Contans.I]);
                    AliPayInnerDoc.setMoney(doc, Double.valueOf(aCsvList[Contans.J]));
                    AliPayInnerDoc.setLendingmark(doc, aCsvList[Contans.K]);
                    AliPayInnerDoc.setBusiness_state(doc, aCsvList[Contans.L]);
                    AliPayInnerDoc.setService_charge(doc, aCsvList[Contans.M]);
                    AliPayInnerDoc.setSuccess_refund(doc, aCsvList[Contans.N]);
                    AliPayInnerDoc.setRemarks(doc, aCsvList[Contans.O]);
                    AliPayInnerDoc.setCapital_state(doc, aCsvList[Contans.P]);
                    doc.put("type", 3);
//                    doc.put("personId", personId);
                    accountOrderDoc.put("fileName", fileMd5);
                    accountOrderDoc.put("business_order", aCsvList[A]);
                    account_orderList.add(accountOrderDoc);
                    newDocData.add(doc);
                }
            }
            accountIntoDB(personId, account_orderList, newDocData, AccountEnum.ALIPAY.getValue(), phoneNum, file, uploadTime, fileMd5);
        } catch (Exception e) {
            //记录错误日志
            updateFileLog(file, false, uploadTime);
        }


    }

    /**
     * 账单入库
     *
     * @param personId          用户身份证
     * @param account_orderList 账单-订单表
     * @param newDocData        账单明细表
     * @param fileType          文件类型 1,银行 2,微信 3,支付宝手机号
     * @param fileState         银行账号,微信昵称,支付宝手机号
     */
    private static void accountIntoDB(String personId, List<Document> account_orderList,
                                      List<Document> newDocData, Integer fileType,
                                      String fileState, File file, Date uploadTime, String fileMD5) {
        System.out.println("在睡");
        //detail表
        deviceMongoDao.insertExcel(DBNAME, DETAIL, newDocData);
        //file-order表
        deviceMongoDao.insertExcel(DBNAME, FILEORDER, account_orderList);
        //用户id_file 表
        Document doc = new Document("fileName", fileMD5);
        doc.append("personId", personId);
        doc.append("fileType", fileType);
        deviceMongoDao.insertDocument(DBNAME, PERSONID_FILE, doc);
        //修改log日志
        updateFileLog(file, true, uploadTime);


    }

    private static void updateFileLog(File file, Boolean flag, Date uploadTime) {
        String log = flag ? "导入成功" : "账单模板错误";
        Document oldFileDoc = new Document("filename", file.getName()).append("time", uploadTime);
        Document newFileDoc = new Document("state", log);
        deviceMongoDao.updateDocument("accountdata", "accountlogs", oldFileDoc, newFileDoc);
    }


    private List<String[]> getCsvList(File file, String word) throws IOException {
        List<String[]> csvList = new ArrayList<>();
        CsvReader reader = new CsvReader(file.getName(), ',', Charset.forName(word));
        while (reader.readRecord()) {
            //按行读取，并把每一行的数据添加到list集合
            csvList.add(reader.getValues());
        }
        reader.close();
        return csvList;
    }

    /**
     * 微信账单明细入库
     */
    private void weiXinAccountUpload(File file, String personId, Date uploadTime) {
        List<Document> account_orderList = new ArrayList<>();
        List<Document> newDocData = new ArrayList<>();
        String fileMd5 = FileMd5Util.calcMD5(file);
        try {
            List<String[]> csvList = getCsvList(file, "UTF-8");
            //验证格式
            String title = csvList.get(0)[0];
            if (!"\uFEFF微信支付账单明细".equals(title.trim())) {
                throw new RuntimeException();
            }

            //获取微信账单昵称
            String lineTwo = csvList.get(1)[0];
            String nickName = null;
            String regex = "(?<=\\[)(\\S+)(?=\\])";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(lineTwo);
            while (matcher.find()) {
                nickName = matcher.group();
                break;
            }
            //获取第一行数据作为account
            String accountType = csvList.get(1)[0];
            System.out.println(accountType);
            for (int x = 17; x < csvList.size(); x++) {
                Document doc = new Document();
                Document account_orderDoc = new Document();
                String[] aCsvList = csvList.get(x);
                if (aCsvList.length > 2) {
                    WxInnerDoc.setBusiness_time(doc, aCsvList[Contans.A]);
                    WxInnerDoc.setBusiness_type(doc, aCsvList[Contans.B]);
                    WxInnerDoc.setBusiness_name(doc, aCsvList[C]);
                    WxInnerDoc.setCommodity_name(doc, aCsvList[Contans.D]);
                    WxInnerDoc.setLendingmark(doc, aCsvList[Contans.E]);
                    WxInnerDoc.setMoney(doc, aCsvList[Contans.F]);
                    WxInnerDoc.setPaytype(doc, aCsvList[Contans.G]);
                    WxInnerDoc.setBusiness_state(doc, aCsvList[Contans.H]);
                    WxInnerDoc.setBusiness_order(doc, aCsvList[Contans.I]);
                    WxInnerDoc.setAuchernum(doc, aCsvList[Contans.J]);
                    WxInnerDoc.setRemarks(doc, aCsvList[Contans.K]);
                    doc.put("type", 2);
                    doc.put("fileState", nickName);
                    account_orderDoc.put("fileState", nickName);
                    account_orderDoc.put("fileName", fileMd5);
                    account_orderDoc.put("business_order", aCsvList[Contans.I]);
                    newDocData.add(doc);
                    account_orderList.add(account_orderDoc);
                }
            }
            accountIntoDB(personId, account_orderList, newDocData, AccountEnum.WEIXIN.getValue(), nickName, file, uploadTime, fileMd5);

        } catch (Exception e) {
            //记录错误日志
            updateFileLog(file, false, uploadTime);
        }
    }

    /**
     * 银行账单明细入库
     */
    private void bankAccountUpload(File file, String personId, Date uploadTime) {
        List<Document> account_orderList = new ArrayList<>();
        String fileMD5 = FileMd5Util.calcMD5(file);
        try {
            Workbook wb = WorkBookUtil.getWorkbook(file);
            //获取明细标签页
            Sheet sheet = wb.getSheet("银行卡明细结果列表");
            if (null == sheet) {
                throw new RuntimeException();
            }
            //获取有效行数
            int rowNum = sheet.getLastRowNum();
            //获取银行卡号
            String bankCardNum = null;
            //从第二行开始读取数据  获取到有效的列数
            List<Document> newDocData = new ArrayList<>();

            for (int i = 1; i <= rowNum; i++) {
                Row row = sheet.getRow(i);
                row.getCell(A).setCellType(CellType.STRING);
                if (StringUtils.isEmpty(row.getCell(A).getStringCellValue()) && "-".equals(row.getCell(C).getStringCellValue())) {
                    continue;
                }
                Document doc = new Document();
                Document account_orderDoc = new Document();
                bankCardNum = row.getCell(Contans.B).getStringCellValue();
                BankInnerDoc.setBankCardNum(doc, row.getCell(Contans.B).getStringCellValue());
                String contantC = row.getCell(C).getStringCellValue();
                if ("-".equals(contantC)) {
                    continue;
                }
                BankInnerDoc.setBusiness_name(doc, row.getCell(C).getStringCellValue());
                BankInnerDoc.setBusiness_card_id(doc, row.getCell(Contans.D).getStringCellValue());
                row.getCell(Contans.E).setCellType(CellType.STRING);
                BankInnerDoc.setMoney(doc, row.getCell(Contans.E).getStringCellValue());
                row.getCell(Contans.F).setCellType(CellType.STRING);
                BankInnerDoc.setBalance(doc, row.getCell(Contans.F).getStringCellValue());
                BankInnerDoc.setLendingmark(doc, row.getCell(Contans.G).getStringCellValue());
                BankInnerDoc.setBusiness_type(doc, row.getCell(Contans.H).getStringCellValue());
                BankInnerDoc.setBusiness_state(doc, row.getCell(Contans.I).getStringCellValue());
                row.getCell(Contans.J).setCellType(CellType.STRING);

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime ldt = LocalDateTime.parse(row.getCell(Contans.J).getStringCellValue(), dtf);
                DateTimeFormatter fa = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date parse = s.parse(ldt.format(fa));
                BankInnerDoc.setBusiness_time(doc, row.getCell(Contans.J).getStringCellValue());
                BankInnerDoc.setBusiness_bank(doc, row.getCell(Contans.K).getStringCellValue());
                BankInnerDoc.setTradepoint(doc, row.getCell(Contans.L).getStringCellValue());
                BankInnerDoc.setBusiness_order(doc, row.getCell(Contans.M).getStringCellValue());
                BankInnerDoc.setAuchernum(doc, row.getCell(Contans.N).getStringCellValue());
                BankInnerDoc.setTerminalnum(doc, row.getCell(Contans.O).getStringCellValue());
                BankInnerDoc.setCashlog(doc, row.getCell(Contans.P).getStringCellValue());
                BankInnerDoc.setBusiness_digest(doc, row.getCell(Contans.Q).getStringCellValue());
                BankInnerDoc.setMerchant_name(doc, row.getCell(Contans.R).getStringCellValue());
                BankInnerDoc.setIp(doc, row.getCell(Contans.S).getStringCellValue());
                BankInnerDoc.setMac(doc, row.getCell(Contans.T).getStringCellValue());
                doc.put("type", 1);
                account_orderDoc.put("business_order", row.getCell(Contans.M).getStringCellValue());
                account_orderDoc.put("fileName", fileMD5);
                account_orderDoc.put("fileState", bankCardNum);
                account_orderDoc.put("business_time", parse);
                String numLine = row.getCell(A).getStringCellValue();
                account_orderDoc.put("unique",numLine+row.getCell(Contans.J).getStringCellValue());
                doc.put("unique",numLine+row.getCell(Contans.J).getStringCellValue());
                account_orderList.add(account_orderDoc);
                newDocData.add(doc);
            }
            accountIntoDB(personId, account_orderList, newDocData, AccountEnum.YINGHANG.getValue(),
                    bankCardNum, file, uploadTime, fileMD5);
        } catch (Exception e) {
            //记录错误日志
            updateFileLog(file, false, uploadTime);
        }
    }

}
