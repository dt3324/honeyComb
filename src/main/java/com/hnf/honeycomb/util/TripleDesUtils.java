package com.hnf.honeycomb.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * 用于3DES加密工具类
 *
 * @author yy
 */
public class TripleDesUtils {

    public static byte[] hex(String key) {
        String f = DigestUtils.md5Hex(key);
        byte[] bkeys = null;
        try {
            bkeys = new String(f).getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] enk = new byte[24];
        for (int i = 0; i < 24; i++) {
            enk[i] = bkeys[i];
        }
        return enk;
    }

    /**
     * 对对应的字符串进行加密
     *
     * @param key    加密密钥
     * @param srcStr 需加密的字符串
     * @return 返回加密后的字符串
     */
    public static String encode3Des(String key, String srcStr) {
        byte[] keybyte = hex(key);
        byte[] src = null;
        try {
            src = srcStr.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            //加密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            String pwd = new String(Base64.encodeBase64(c1.doFinal(src)));
            return pwd;
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }


    /**
     * 对已加密的字符串进行解密
     *
     * @param key    加密时的密钥
     * @param desStr 已加密的字符串
     * @return 返回解密的字符串
     */
    public static String decode3Des(String key, String desStr) {
        Base64 base64 = new Base64();
        byte[] keybyte = hex(key);
        //		base64.de
//		desStr = toUTF8(desStr);
        byte[] src = Base64.decodeBase64(desStr.getBytes());
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");
            //解密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.DECRYPT_MODE, deskey);
            String pwd = new String(c1.doFinal(src), "UTF-8");
            return pwd;
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    public static String toUTF8(String str) {
        if (isEmpty(str)) {
            return "";
        }
        try {
            if (str.equals(new String(str.getBytes("GB2312"), "GB2312"))) {
                str = new String(str.getBytes("GB2312"), "UTF-8");
                System.out.println("GB");
                return str;
            }
        } catch (Exception exception) {
        }
        try {
            if (str.equals(new String(str.getBytes("ISO-8859-1"), "ISO-8859-1"))) {
                str = new String(str.getBytes("ISO-8859-1"), "UTF-8");
                System.out.println("ISO");
                return str;
            }
        } catch (Exception exception1) {
        }
        try {
            if (str.equals(new String(str.getBytes("GBK"), "GBK"))) {
                System.out.println("GBK");
                str = new String(str.getBytes("GBK"), "UTF-8");
                System.out.println("GBK");
                return str;
            }
        } catch (Exception exception3) {
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        // 如果字符串不为null，去除空格后值不与空字符串相等的话，证明字符串有实质性的内容
        if (str != null && !str.trim().isEmpty()) {
            // 不为空
            return false;
        }
        // 为空
        return true;
    }

    public static void main(String[] args) {
        //		ThreeDESCode a = new ThreeDESCode();
        String result = TripleDesUtils.encode3Des("hnf", "CN=安全审计员, O=jit, C=cn");
        System.out.println("result:" + result);
        String aa = TripleDesUtils.decode3Des("hnf", result);
        System.out.println("aa:" + aa);
    }
}
