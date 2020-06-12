package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class NoSuchUserException extends AbstractBusinessException {
    public NoSuchUserException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
