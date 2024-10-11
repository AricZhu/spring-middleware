package com.aric.middleware.aop;

import com.aric.middleware.annotation.HystrixAnnotation;
import com.aric.middleware.service.HystrixProcess;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class HystrixProcessAop {
    private static final Logger logger = LoggerFactory.getLogger(HystrixProcessAop.class);

    @Pointcut("@annotation(com.aric.middleware.annotation.HystrixAnnotation)")
    public void pointcut() {}

    @Around("pointcut() && @annotation(hystrixAnnotation)")
    public Object doRouter(ProceedingJoinPoint jp, HystrixAnnotation hystrixAnnotation) {
        logger.info("### hystrix for method {}, and timeout is {}", jp.getSignature().getName(), hystrixAnnotation.timeoutMs());
        String commandKey = jp.getTarget().getClass().getName() + "." + jp.getSignature().getName();
        HystrixProcess hystrixProcess = new HystrixProcess(jp, hystrixAnnotation.timeoutMs(), commandKey);
        return hystrixProcess.execute();
    }
}
