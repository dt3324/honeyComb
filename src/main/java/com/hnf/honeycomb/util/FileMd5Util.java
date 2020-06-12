package com.hnf.honeycomb.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileMd5Util {

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();
//
//    public static void main(String[] args) {
//
//        long beginTime = System.currentTimeMillis();
//        File file = new File("E:/云舒测试文件/20180628-北大社-中国古文字学通论.pdf");
//        //File file = new File("E:/拉鲁斯法汉双解词典(12新)/制作文件库/其他/SJ00040936 拉鲁斯法汉双解词典(内文排版).zip");
//        String md5 = calcMD5(file);
//        long endTime = System.currentTimeMillis();
//        System.out.println("MD5:" + md5 + "\n 耗时:" + ((endTime - beginTime) / 1000) + "s");
//    }

    /**
     * 计算文件 MD5
     * @param file
     * @return 返回文件的md5字符串，如果计算过程中任务的状态变为取消或暂停，返回null， 如果有其他异常，返回空字符串
     */
    public static String calcMD5(File file) {
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int len;
            while ((len = stream.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
            return toHexString(digest.digest());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String toHexString(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

}
