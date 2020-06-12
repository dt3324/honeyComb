package com.hnf.honeycomb.util;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * @author hnf
 */
@Service("base64")
public class Base64 {

    private static char[] base64EncodeChars = new char[]{
            'B', 'A', 'D', 'C', 'F', 'E', 'H', 'G',
            'J', 'I', 'L', 'K', 'N', 'M', 'P', 'O',
            'R', 'Q', 'T', 'S', 'V', 'U', 'X', 'W',
            'Z', 'Y', 'b', 'a', 'd', 'c', 'f', 'e',
            'h', 'g', 'j', 'i', 'l', 'k', 'n', 'm',
            'p', 'o', 'r', 'q', 't', 's', 'v', 'u',
            'x', 'w', 'z', 'y', '1', '0', '3', '2',
            '5', '4', '7', '6', '9', '8', '/', '+'};
    private static byte[] base64DecodeChars = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 63, -1, -1, -1, 62,
            53, 52, 55, 54, 57, 56, 59, 58, 61, 60, -1, -1, -1, -1, -1, -1,
            -1, 1, 0, 3, 2, 5, 4, 7, 6, 9, 8, 11, 10, 13, 12, 15,
            14, 17, 16, 19, 18, 21, 20, 23, 22, 25, 24, -1, -1, -1, -1, -1,
            -1, 27, 26, 29, 28, 31, 30, 32, 32, 35, 34, 37, 36, 39, 38, 41,
            40, 43, 42, 45, 44, 47, 46, 49, 48, 51, 50, -1, -1, -1, -1, -1
    };

    //加密表中的ASCII码的值对应解密表的位，加密表的位对应解密表的值。
	/*private static char[] base64EncodeChars = new char[]{
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '+', '/'};
	private static byte[] base64DecodeChars = new byte[]{
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
        -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
        };
	 * 
	 */

    /**
     * 加密
     */
    public static String encode(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    /**
     * 解密
     */
    public byte[] decode(String str) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        byte[] data = str.getBytes("US-ASCII");
        int len = data.length;
        int i = 0;
        int b1, b2, b3, b4;
        while (i < len) {
            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }
            do {
                b2 = base64DecodeChars
                        [data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            sb.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return sb.toString().getBytes("iso8859-1");
                }
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            sb.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));
            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return sb.toString().getBytes("iso8859-1");
                }
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            sb.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return sb.toString().getBytes("iso8859-1");
    }

//	  public static void main(String[] args) throws UnsupportedEncodingException {
////		  String s = "123我是谁？》《qwertyWQRGFGXF+-*/";
////		  System.out.println("源码" + s);
////		  String x = encode(s.getBytes());
////		  System.out.println("加密" + x);
//		  Base64 base = new Base64();
//		  String x1 = new String(base.decode("4KrQ7InP"));
//		  System.out.println(new String(x1));
//		  //加密MTIz5oiR5piv6LCB77yf44CL44CK    [B@1690726  [B@1d92abb
//	}
}
