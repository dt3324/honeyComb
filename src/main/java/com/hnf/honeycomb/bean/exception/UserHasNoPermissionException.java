package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public class UserHasNoPermissionException extends AbstractBusinessException {
    private UserHasNoPermissionException() {
        super();
    }

    private UserHasNoPermissionException(String message, String type) {
        super(message);
        this.setType(type);
    }

    public static UserHasNoPermissionException of(String state, Long type) {
        return new UserHasNoPermissionException(state, null == type ? null : String.valueOf(type));
    }

    @Override
    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
