package com.aric.middleware.distributetask.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerTaskDesc {
    String cron() default "0/5 * * * * ?";
    boolean autoStart() default true;
    String desc() default "";
}
