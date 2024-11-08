package com.aric.middleware.dbrouter.aop;

import cn.hutool.core.bean.BeanUtil;
import com.aric.middleware.dbrouter.annotation.DBRouterAnnotation;
import com.aric.middleware.dbrouter.config.DBRouterConfig;
import com.aric.middleware.dbrouter.utils.ThreadLocalContext;
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
public class DBRouterAop {
    private final Logger logger = LoggerFactory.getLogger(DBRouterAop.class);

    @Autowired
    private DBRouterConfig dbRouterConfig;

    @Pointcut("@annotation(com.aric.middleware.dbrouter.annotation.DBRouterAnnotation)")
    public void pointcut() {}

    @Around("pointcut() && @annotation(dBRouterAnnotation)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouterAnnotation dBRouterAnnotation) {
        String key = dBRouterAnnotation.key();

        Object arg = jp.getArgs()[0];
        Object dbKeyAttr = BeanUtil.getProperty(arg, key);

        // 计算扰动
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 计算库、表索引
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - (dbIdx - 1) * dbRouterConfig.getTbCount();

        String dbKey = String.format("%02d", dbIdx);
        String tbKey = String.format("%02d", tbIdx);

        logger.info("值: {}, 扰动计算: {}, 库表索引: {}, {}", dbKeyAttr, idx, dbKey, tbKey);

        ThreadLocalContext.setDbKey(dbKey);
        ThreadLocalContext.setTbKey(tbKey);

        try {
            return jp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            ThreadLocalContext.clearDbkey();
            ThreadLocalContext.clearTbKey();
        }
    }
}
