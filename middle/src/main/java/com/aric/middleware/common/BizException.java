package com.aric.middleware.common;

public class BizException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BizException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BizException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BizException(Throwable e) {
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
        this.message = e.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
