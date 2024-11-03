package com.aric.middleware.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface HystrixAnnotation {
    int timeoutMs() default 1000;
}
