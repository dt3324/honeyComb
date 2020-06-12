package com.hnf.honeycomb.bean.enumerations;

/**
 * 操作功能
 * @author ...
 */
public enum Operation {
    /**
     * 权限列表
     */
    manager(1, "管理员"),
    elasticSearch(2, "一键搜"),
    relationship(3, "关系碰撞"),
    virtualIdentity(4, "虚拟身份"),
    gpsAnalysis(5, "时空分析"),
    deviceInfo(6, "设备数据"),
    collector(7, "标采人员"),
    modifyUserInfo(16, "用户信息修改");


    private final Integer operationId;
    private final String operationName;

    Operation(Integer operationId, String operationName) {
        this.operationId = operationId;
        this.operationName = operationName;
    }

    public static String getOperationName(int operationId) {
        for (Operation operation : Operation.values()) {
            if (operationId == operation.operationId) {
                return operation.operationName;
            }
        }
        return null;
    }

    public String getOperationName() {
        return operationName;
    }

    public Integer getOperationId() {
        return operationId;
    }
}
