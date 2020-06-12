package com.hnf.honeycomb.bean.enumerations;

/**
 * 支付宝交易类型枚举
 *
 * @author cq
 */
public enum AlipayTransactionTypeEnum {
    /**
     * 余额宝
     */
    YUEBAO("余额宝", 0),
    /**
     * 转账
     */
    TRANSFER_ACC("转账", 1),
    /**
     * 收付款
     */
    PAY("收付款", 2),
    /**
     * 花呗
     */
    HUA_BEI("花呗", 3),
    /**
     * 借呗
     */
    JIE_BEI("借呗", 4),
    /**
     * 信用卡
     */
    CREDIT_CARD("信用卡", 5),
    /**
     * 充值
     */
    CHONG_ZHI("充值", 6),
    /**
     * 提现
     */
    GET_CASH("提现", 7),
    /**
     * 代付
     */
    AGENT_PAY("代付", 8),
    /**
     * 其它
     */
    OTHER("其它", 99);

    private String name;
    private int type;

    /**
     * @param name
     * @param type
     */
    AlipayTransactionTypeEnum(String name, int type) {
        this.name = name;
        this.type = type;
    }

    /**
     * 通过type获取name
     *
     * @param index
     * @return
     */
    public static String getName(int index) {
        for (AlipayTransactionTypeEnum a : AlipayTransactionTypeEnum.values()) {
            if (a.getType() == index) {
                return a.name;
            }
        }
        return null;
    }

    /**
     * 获取名字
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 获取类型
     *
     * @return
     */
    public int getType() {
        return type;
    }
}
