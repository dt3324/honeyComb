package com.hnf.honeycomb.bean;

import com.hnf.honeycomb.bean.enumerations.AlipayTransactionTypeEnum;
import com.hnf.honeycomb.util.StringUtils;
import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AliPayInnerDoc {



    public static void setBusiness_order(Document doc, String business_order) {
        doc.put("business_order", business_order);
    }

    public static void setAuchernum(Document doc, String auchernum) {
        doc.put("auchernum", auchernum);
    }

    public static void setBusiness_time(Document doc, String business_time) {
        String s =  business_time.replaceAll("-","/");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        doc.put("business_time", date);
    }
    public static void setPayment_time(Document doc,String payment_time){
        String s =  payment_time.replaceAll("-","/");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            date = null;
        }
        doc.put("payment_time",date);
    }

    public static void setLast_modify_time(Document doc, String last_modify_time) {
        String s =  last_modify_time.replaceAll("-","/");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        doc.put("last_modify_time", date);
    }

    public static void setBusiness_location(Document doc, String business_location) {
        doc.put("business_location", business_location);
    }

    public static void setBusiness_type(Document doc, String business_type) {
        doc.put("business_type", business_type);
    }

    public static void setBusiness_name(Document doc, String business_name) {
        doc.put("business_name", business_name);
    }


    public static void setCommodity_name(Document doc, String commodity_name) {
        doc.put("commodity_name", commodity_name);
    }

    public static void setMoney(Document doc, Double money) {
        doc.put("money", money);
    }

    public static void setLendingmark(Document doc, String lendingmark) {
        doc.put("lendingmark", lendingmark);
    }

    public static void setBusiness_state(Document doc, String business_state) {
        doc.put("business_state", business_state);
    }

    public static void setService_charge(Document doc, String service_charge) {
        doc.put("service_charge", Double.valueOf(service_charge));
    }

    public static void setSuccess_refund(Document doc, String success_refund) {
        doc.put("success_refund", Double.valueOf(success_refund));
    }

    public static void setRemarks(Document doc, String remarks) {
        doc.put("remarks", remarks);
    }

    public static void setCapital_state(Document doc, String capital_state) {
        doc.put("capital_state", capital_state);
    }

    public static void businessType(Document doc,String transaction){
        if (transaction.contains("余额宝")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.YUEBAO.getType()));
        } else if (transaction.contains("转账")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.TRANSFER_ACC.getType()));
        } else if (transaction.contains("收款") || transaction.contains("付款")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.PAY.getType()));
        } else if (transaction.contains("花呗")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.HUA_BEI.getType()));
        } else if (transaction.contains("借呗")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.JIE_BEI.getType()));
        } else if (transaction.contains("信用卡")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.CREDIT_CARD.getType()));
        } else if (transaction.contains("充值")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.CHONG_ZHI.getType()));
        } else if (transaction.contains("提现")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.GET_CASH.getType()));
        } else if (transaction.contains("代付")) {
            doc.put("transaction_source", String.valueOf(AlipayTransactionTypeEnum.AGENT_PAY.getType()));
        } else {
            doc.put("transaction_source", AlipayTransactionTypeEnum.OTHER.getType());
        }

    }

}
