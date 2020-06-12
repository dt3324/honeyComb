package com.hnf.honeycomb.util;

import com.hnf.honeycomb.bean.DepartmentBean;
import com.hnf.honeycomb.bean.enumerations.RoleEnum;
import com.hnf.honeycomb.remote.user.DepartmentMapper;
import com.hnf.honeycomb.util.ExcelCreateUserAction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查各种信息合法性
 *
 * @author lsj
 */
@Component
public class CheckLegal {
    private String name;
    private String policeNum;
    private String idNum;
    private String departmentCode;
    private String dogNum;
    private Map<String, Object> departmentMap = new HashMap<>();
    @Resource
    private DepartmentMapper dao;
    private static Pattern PATTERN_TEL_NUMBER
            = Pattern.compile("^1[3-9]\\d{9}$", Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_MOBILE_NUMBER
            = Pattern.compile("^1((3\\d)|(4[5-9])|(5[^4])|(6[56])|(7[^9])|(8\\d)|(9[189]))\\d{8}$");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoliceNum() {
        return policeNum;
    }

    public void setPoliceNum(String policeNum) {
        this.policeNum = policeNum;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDogNum() {
        return dogNum;
    }

    public void setDogNum(String dogNum) {
        this.dogNum = dogNum;
    }

    public CheckLegal() {
    }

    public CheckLegal(String name, String policeNum, String idNum, String departmentCode, String dogNum) {

        this.name = name;
        this.policeNum = policeNum;
        this.idNum = idNum;
        this.departmentCode = departmentCode;
        this.dogNum = dogNum;
    }

    public void checkLegalByIdNum(String idNum, int row, int column) throws ParseException {
        if (StringUtils.isEmpty(idNum)) {
            throw new RuntimeException("身份证" + row + "行" + column + "列错误: 空");
        }
        String error = ExcelCreateUserAction.getInstance().iDCardValidate(idNum.trim());
        if (!StringUtils.isEmpty(error)) {
            throw new RuntimeException("身份证" + row + "行" + column + "列错误: " + error);
        }
    }

    public void checkLegalByPhone(String phone, int row, int column) {
        if (!isMobilePhoneNumber(phone)) {
            throw new RuntimeException("电话号码" + row + "行" + column + "列错误");
        }
    }

    public void checkLegalByUserType(String userType, int row, int column) {
        if (RoleEnum.getEnumByKey(userType.trim()) == null) {
            throw new RuntimeException("请检查" + row + "行" + column + "列" + " : 账户类型不合法请修改");
        }
    }


    /**
     * @param code   部门编号
     * @param row    行
     * @param column 列
     * @return
     */
    public Long checkLegalByDepartmentId(String code, int row, int column) {
        if (!departmentMap.containsValue(code)) {
            DepartmentBean departmentBean = dao.findByDeCode(code);
            if (null != departmentBean) {
                Long departmentId = departmentBean.getDepartmentId();
                //如果有该部门则放入缓存
                departmentMap.put(code, departmentId);
                return departmentId;
            }
            //没有查到抛出异常
            throw new RuntimeException(row + "行" + column + "列" + "部门不存在，请先创建或导入部门");
        }
        return (Long) departmentMap.get(code);
    }

    /**
     * 通用判断
     *
     * @param telNum
     * @return
     */
    private static boolean isMobilePhoneNumber(String telNum) {
        return PATTERN_TEL_NUMBER.matcher(telNum).matches();
    }

    /**
     * 更严格的判断
     *
     * @param telNum
     * @return
     */
    private static boolean isStrictPhoneNumber(String telNum) {
        return PATTERN_MOBILE_NUMBER.matcher(telNum).matches();
    }

}
