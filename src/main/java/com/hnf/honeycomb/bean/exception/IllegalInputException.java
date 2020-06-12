package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class IllegalInputException extends AbstractBusinessException {

    public static IllegalInputException of(String state, String type) {
        return new IllegalInputException(state, type);
    }

    public static IllegalInputException of(String state) {
        return new IllegalInputException(state);
    }

    private IllegalInputException(String state, String type) {
        super(state);
        this.setType(type);
    }

    private IllegalInputException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
