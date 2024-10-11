package com.aric.middleware.aop;

import com.aric.middleware.annotation.RateLimiterAnnotation;
import com.aric.middleware.service.RateLimiterProcess;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimiterProcessAop {
    private final static Logger logger = LoggerFactory.getLogger(RateLimiterProcessAop.class);

    @Autowired
    private RateLimiterProcess rateLimiterProcess;

    @Pointcut("@annotation(com.aric.middleware.annotation.RateLimiterAnnotation)")
    public void pointcut() {}

    @Around("pointcut() && @annotation(rateLimiterAnnotation)")
    public Object doRouter(ProceedingJoinPoint jp, RateLimiterAnnotation rateLimiterAnnotation) throws Throwable {
        logger.info("### rate limit for method {}, {}", jp.getSignature().getName(), rateLimiterAnnotation.permitPerSecond());
        return rateLimiterProcess.access(jp, rateLimiterAnnotation.permitPerSecond());
    }
}
