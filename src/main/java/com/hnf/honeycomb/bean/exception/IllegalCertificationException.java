package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class IllegalCertificationException extends AbstractBusinessException {
    private IllegalCertificationException() {
        super();
    }

    private IllegalCertificationException(String message, String type) {
        super(message);
        this.setType(type);
    }

    public static IllegalCertificationException of(String state, Long type) {
        return new IllegalCertificationException(state, null == type ? null : String.valueOf(type));
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
