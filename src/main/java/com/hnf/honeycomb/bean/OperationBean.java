package com.hnf.honeycomb.bean;

import java.io.Serializable;

/**
 * @author admin
 */
public class OperationBean implements Serializable {
    private static final long serialVersionUID = -205034799093733116L;

    private Integer operationId;

    private String operationName;


    public OperationBean() {
        super();
        // TODO Auto-generated constructor stub
    }

    public OperationBean(String operationName) {
        super();
        this.operationName = operationName;
    }

    public Integer getOperationId() {
        return operationId;
    }

    public void setOperationId(Integer operationId) {
        this.operationId = operationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public String toString() {
        return "OperationBean [operationId=" + operationId + ", operationName=" + operationName + "]";
    }

}
