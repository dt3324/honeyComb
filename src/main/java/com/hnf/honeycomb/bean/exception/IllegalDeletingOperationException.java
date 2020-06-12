package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class IllegalDeletingOperationException extends AbstractBusinessException {

    public static IllegalDeletingOperationException of(String state, String type) {
        return new IllegalDeletingOperationException(state, type);
    }

    private IllegalDeletingOperationException(String state, String type) {
        super(state);
        this.setType(type);
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
