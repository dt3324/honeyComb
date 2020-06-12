package com.hnf.honeycomb.bean;

import org.apache.poi.ss.usermodel.DateUtil;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class BankInnerDoc {


    public static void setNum(Document doc, String num) {
        doc.put("num", num);
    }

    public static void setBankCardNum(Document doc, String bankCardNum) {
        doc.put("bankcarnum", bankCardNum.trim());
    }

    public static void setBusiness_name(Document doc, String business_name) {
        doc.put("business_name", business_name.trim());
    }

    public static void setBusiness_card_id(Document doc, String business_card_id) {
        doc.put("business_card_id", business_card_id.trim());
    }

    public static void setMoney(Document doc, String money) {
        doc.put("money", Double.valueOf(money.trim()));
    }

    public static void setBalance(Document doc, String balance) {
        doc.put("balance", Double.valueOf(balance.trim()));
    }

    public static void setLendingmark(Document doc, String lendingmark) {
        doc.put("lendingmark", lendingmark.trim());
    }

    public static void setBusiness_type(Document doc, String business_type) {
        doc.put("business_type", business_type.trim());
    }

    public static void setBusiness_state(Document doc, String busintss_state) {
        doc.put("business_state", busintss_state.trim());
    }

    public static void setBusiness_time(Document doc, String business_time) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime ldt = LocalDateTime.parse(business_time, dtf);
            DateTimeFormatter fa = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date parse = s.parse(ldt.format(fa));
            doc.put("business_time", parse);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void setBusiness_bank(Document doc, String business_bank) {
        doc.put("business_bank", business_bank.trim());
    }

    public static void setTradepoint(Document doc, String tradepoint) {
        doc.put("tradepoint", tradepoint.trim());
    }

    public static void setBusiness_order(Document doc, String business_order) {
        doc.put("business_order", business_order.trim());
    }

    public static void setAuchernum(Document doc, String auchernum) {
        doc.put("auchernum", auchernum.trim());
    }

    public static void setTerminalnum(Document doc, String terminalnum) {
        doc.put("terminalnum", terminalnum.trim());
    }

    public static void setCashlog(Document doc, String cashlog) {
        doc.put("cashlog", cashlog.trim());
    }

    public static void setBusiness_digest(Document doc, String business_digest) {
        doc.put("business_digest", business_digest.trim());
    }

    public static void setMerchant_name(Document doc, String merchant_name) {
        doc.put("merchant_name", merchant_name.trim());
    }

    public static void setIp(Document doc, String ip) {
        doc.put("ip", ip.trim());
    }

    public static void setMac(Document doc, String mac) {
        doc.put("MAC", mac);
    }

}
