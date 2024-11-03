package com.aric.middleware.common;

public class Result {
    private Integer code;
    private String message;
    private Object result;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getResult() {
        return result;
    }

    public Result(Integer code, String message, Object result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static Result success(Object data) {
        return new Result(0, "", data);
    }

    public static Result fail() {
        return new Result(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage(), null);
    }

    public static Result fail(ErrorCode errorCode) {
        return new Result(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static Result fail(Integer errorCode, String errorMsg) {
        return new Result(errorCode, errorMsg, null);
    }

    public static Result fail(BizException exception) {
        return new Result(exception.getCode(), exception.getMessage(), null);
    }
}
