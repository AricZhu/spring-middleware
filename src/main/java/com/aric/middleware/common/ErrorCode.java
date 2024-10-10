package com.aric.middleware.common;

public enum ErrorCode {
    SYSTEM_ERROR(500001, "系统错误"),
    WHITE_LIST_ERROR(500002, "白名单拦截"),
    TIMEOUT_ERROR(500003, "超时熔断"),
    ;
    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
