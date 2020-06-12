package com.hnf.honeycomb.bean;


import org.bson.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WxInnerDoc {


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

    public static void setBusiness_type(Document doc, String business_type) {
        doc.put("business_type", business_type);
    }

    public static void setBusiness_name(Document doc, String business_name) {
        doc.put("business_name", business_name);
    }

    public static void setCommodity_name(Document doc, String commodity_name) {
        doc.put("commodity_name", commodity_name);
    }

    public static void setLendingmark(Document doc, String lendingmark) {
        doc.put("lendingmark", lendingmark);
    }

    public static void setPaytype(Document doc, String paytype) {
        doc.put("paytype", paytype);
    }

    public static void setBusiness_state(Document doc, String business_state) {
        doc.put("business_state", business_state);
    }

    public static void setAuchernum(Document doc, String auchernum) {
        doc.put("auchernum", auchernum);
    }

    public static void setBusiness_order(Document doc, String business_order) {
        doc.put("business_order", business_order);
    }

    public static void setMoney(Document doc, String money) {
        String substring = money.substring(1);
        doc.put("money", Double.valueOf(substring));
    }
    public static void setRemarks(Document doc, String remarks) {
        doc.put("remarks", remarks);
    }



}
