package com.aric.middleware.service;

import com.aric.middleware.common.BizException;
import com.aric.middleware.common.ErrorCode;
import com.aric.middleware.common.Result;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.aspectj.lang.ProceedingJoinPoint;

public class HystrixProcess extends HystrixCommand<Object> {
    private final ProceedingJoinPoint jp;
    private final int timeoutMs;
    private final String commandKey;

    public HystrixProcess(ProceedingJoinPoint jp, int timeoutMs, String commandKey) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(timeoutMs)));
        this.jp = jp;
        this.timeoutMs = timeoutMs;
        this.commandKey = commandKey;
    }

    @Override
    protected Object run() throws Exception {
        try {
            return jp.proceed();
        } catch (Throwable e) {
            throw new BizException(e);
        }
    }

    @Override
    protected Object getFallback() {
        return Result.fail(ErrorCode.TIMEOUT_ERROR);
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
