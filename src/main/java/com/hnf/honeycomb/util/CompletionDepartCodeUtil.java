package com.hnf.honeycomb.util;

/**
 * @author admin
 */
public class CompletionDepartCodeUtil {
    /**
     * 补全部门code
     *
     * @return
     */
    public static String completion(String departmentCode) {
        int length = departmentCode.length();
        switch (length) {
            case 2:
                departmentCode += "000000000";
                break;
            case 4:
                departmentCode += "0000000";
                break;
            case 6:
                departmentCode += "00000";
                break;
            default:
                break;
        }
        return departmentCode;
    }

    public static void main(String[] args) {
        System.out.println(completion("5101").length());
        System.out.println(completion("51010400000").length());
    }
}
