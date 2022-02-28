package org.yml.plugin.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yaml
 * @since 2020/12/28
 */
@Getter
@Setter
public class HotchpotchException extends RuntimeException {

    public static final int DEFAULT_FAULT_CODE = 500;
    private final int code;
    private final String message;

    public HotchpotchException(String message) {
        this(DEFAULT_FAULT_CODE, message);
    }

    public HotchpotchException(int code, String message) {
        this(code, message, new Throwable());
    }

    public HotchpotchException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public static HotchpotchException create(String message) {
        return new HotchpotchException(DEFAULT_FAULT_CODE, message);
    }

    public static HotchpotchException create(int code, String message) {
        return new HotchpotchException(code, message);
    }

    public static HotchpotchException create(Throwable cause) {
        return new HotchpotchException(DEFAULT_FAULT_CODE, cause.getMessage(), cause);
    }

    public static HotchpotchException create(String message, Throwable cause) {
        return new HotchpotchException(DEFAULT_FAULT_CODE, message, cause);
    }
}
