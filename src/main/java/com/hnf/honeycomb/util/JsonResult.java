package com.hnf.honeycomb.util;

import org.springframework.dao.QueryTimeoutException;

import java.io.Serializable;

/**
 * @author hnf
 */
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;
    private static final JsonResult<Object> ERROR_RESULT = new JsonResult<>();
    private int state;
    private T data;
    private String message;

    public JsonResult() {

    }

    public JsonResult(QueryTimeoutException e) {
        state = 20;
    }

    public JsonResult(T t) {
        state = SUCCESS;
        data = t;
        message = "";
    }

    public JsonResult(Throwable e) {
        state = ERROR;
        data = null;
        message = e.getMessage();
    }

    public JsonResult(int i, Throwable e) {
        this.state = i;
        this.data = null;
        this.message = e.getMessage();
    }

    public int getState() {
        return state;
    }

    public JsonResult<T> setState(int state) {
        this.state = state;
        return this;
    }

    public T getData() {
        return data;
    }

    public JsonResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JsonResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public static JsonResult<Object> ofError(String message) {
        ERROR_RESULT.setMessage(message);
        ERROR_RESULT.setState(ERROR);
        return ERROR_RESULT;
    }
}
