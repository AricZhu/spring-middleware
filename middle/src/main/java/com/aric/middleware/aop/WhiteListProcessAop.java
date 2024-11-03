package com.aric.middleware.aop;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.aric.middleware.annotation.WhiteListAnnotation;
import com.aric.middleware.common.BizException;
import com.aric.middleware.common.ErrorCode;
import com.aric.middleware.configuration.WhiteListConfiguration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

@Aspect
@Component
public class WhiteListProcessAop {
    @Autowired
    private WhiteListConfiguration whiteListConfiguration;
    
    private static final Logger logger = LoggerFactory.getLogger(WhiteListProcessAop.class);

    @Pointcut("@annotation(com.aric.middleware.annotation.WhiteListAnnotation)")
    public void aopPoint() {

    }

    @Around("aopPoint()")
    public Object doRouter(ProceedingJoinPoint jp) throws Throwable {
        // 白名单开关关闭，直接放行
        Boolean isOpen = whiteListConfiguration.getIsOpen();
        if (!isOpen) {
            return jp.proceed();
        }

        Method method = getMethod(jp);
        WhiteListAnnotation annotation = method.getAnnotation(WhiteListAnnotation.class);
        String key = annotation.key();

        // 未添加白名单 key，直接放行
        if (StrUtil.isEmpty(key)) {
            return jp.proceed();
        }

        // 遍历方法参数，找出对应 key 的参数值
        Object[] argValues = jp.getArgs();
        Object[] argNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray();
        for (int i = 0; i < argNames.length; i++) {
            if (!key.equals(argNames[i])) {
                continue;
            }
            Object value = argValues[i];
            String[] whiteListUsers = whiteListConfiguration.getUsers().split(",");
            // 当前值包含在白名单中，放行
            if (ArrayUtil.contains(whiteListUsers, value)) {
                return jp.proceed();
            }
            // 当前值不包含在白名单中，抛出异常
            throw new BizException(ErrorCode.WHITE_LIST_ERROR);
        }

        return jp.proceed();
    }

    public Method getMethod(JoinPoint jp) {
        MethodSignature signature = (MethodSignature)jp.getSignature();
        return signature.getMethod();
    }
}
