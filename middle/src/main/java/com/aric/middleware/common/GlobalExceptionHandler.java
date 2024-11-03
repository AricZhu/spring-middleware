package com.aric.middleware.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException e) {
        return Result.fail(e);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), e.getMessage());
    }
}
