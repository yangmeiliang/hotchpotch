package org.yml.plugin.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yaml
 * @since 2020/12/28
 */
@Getter
@Setter
public class BusinessException extends RuntimeException {

    public static final int DEFAULT_FAULT_CODE = 500;
    private int code;
    private String message;

    public BusinessException(String message) {
        this(DEFAULT_FAULT_CODE, message);
    }

    public BusinessException(int code, String message) {
        this(code, message, new Throwable());
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public static BusinessException create(String message) {
        return new BusinessException(DEFAULT_FAULT_CODE, message);
    }

    public static BusinessException create(int code, String message) {
        return new BusinessException(code, message);
    }
}
