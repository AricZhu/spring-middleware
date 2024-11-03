package com.aric.middleware.common;

public enum ErrorCode {
    SYSTEM_ERROR(500001, "系统错误"),
    WHITE_LIST_ERROR(500002, "白名单拦截"),
    TIMEOUT_ERROR(500003, "超时熔断"),
    RATELIMITER_ERROR(500004, "限流"),
    METHODEXT_ERROR(500005, "自定义方法拦截"),
    METHODEXT_NOTFOUND_ERROR(500006, "自定义拦截方法参数异常"),
    METHODEXT_RETURNTYPE_ERROR(500006, "自定义拦截方法返回异常"),
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
