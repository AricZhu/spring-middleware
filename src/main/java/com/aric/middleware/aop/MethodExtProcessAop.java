package com.aric.middleware.aop;

import com.aric.middleware.annotation.MethodExtAnnotation;
import com.aric.middleware.common.BizException;
import com.aric.middleware.common.ErrorCode;
import com.aric.middleware.common.Result;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Aspect
@Component
public class MethodExtProcessAop {

    @Pointcut("@annotation(com.aric.middleware.annotation.MethodExtAnnotation)")
    public void pointcut() {}

    @Around("pointcut() && @annotation(methodExtAnnotation)")
    public Object doRouter(ProceedingJoinPoint jp, MethodExtAnnotation methodExtAnnotation) throws Throwable {
        Method method = getMethod(jp);
        String extMethodName = methodExtAnnotation.method();
        Method extMethod = null;
        // 根据方法名和参数类型找出自定义方法
        try {
            extMethod = jp.getTarget().getClass().getMethod(extMethodName, method.getParameterTypes());
        } catch (Exception e) {
            throw new BizException(ErrorCode.METHODEXT_NOTFOUND_ERROR);
        }

        // 判断自定义方法返回值
        if (!extMethod.getReturnType().getName().equals("boolean")) {
            throw new BizException(ErrorCode.METHODEXT_RETURNTYPE_ERROR);
        }

        // 根据自定义方法返回值决定是否继续运行
        boolean ret = (boolean) extMethod.invoke(jp.getThis(), jp.getArgs());

        return ret ? jp.proceed() : Result.fail(ErrorCode.METHODEXT_ERROR);
    }

    public Method getMethod(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        return signature.getMethod();
    }
}
