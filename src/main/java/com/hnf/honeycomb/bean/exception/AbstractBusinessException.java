package com.hnf.honeycomb.bean.exception;

import org.springframework.http.HttpStatus;

/**
 * @author admin
 */
public abstract class AbstractBusinessException extends RuntimeException {
    /**
     * the code that will be returned as a part of JsonResponse, defaulted as {@code Integer.MAX_VALUE}
     */
    protected String type;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public AbstractBusinessException() {
        super();
        this.type = Integer.valueOf(Integer.MAX_VALUE).toString();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public AbstractBusinessException(String message) {
        super(message);
        this.type = Integer.valueOf(Integer.MAX_VALUE).toString();
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public AbstractBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.type = Integer.valueOf(Integer.MAX_VALUE).toString();
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public AbstractBusinessException(Throwable cause) {
        super(cause);
        this.type = Integer.valueOf(Integer.MAX_VALUE).toString();
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public HttpStatus getSupposedHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
