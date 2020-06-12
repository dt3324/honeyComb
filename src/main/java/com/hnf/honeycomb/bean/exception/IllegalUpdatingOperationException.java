package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class IllegalUpdatingOperationException extends AbstractBusinessException {

    public static IllegalUpdatingOperationException of(String state, String type) {
        return new IllegalUpdatingOperationException(state, type);
    }

    private IllegalUpdatingOperationException(String state, String type) {
        super(state);
        this.setType(type);
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
