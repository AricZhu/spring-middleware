package com.aric.middleware.service;

import com.aric.middleware.common.ErrorCode;
import com.aric.middleware.common.Result;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimiterProcess implements IRateLimiter {
    private final static Map<String, RateLimiter> rateLimiter = new HashMap<>();

    @Override
    public Object access(ProceedingJoinPoint jp, double permitsPerSecond) throws Throwable {
        // 无效设置不限流
        if (permitsPerSecond <= 0) {
            return jp.proceed();
        }

        String key = jp.getTarget().getClass().getName() + "." + jp.getSignature().getName();
        if (!rateLimiter.containsKey(key)) {
            rateLimiter.put(key, RateLimiter.create(permitsPerSecond));
        }

        RateLimiter rateLimiterIns = rateLimiter.get(key);

        if (rateLimiterIns.tryAcquire()) {
            return jp.proceed();
        }

        return Result.fail(ErrorCode.RATELIMITER_ERROR);
    }
}
