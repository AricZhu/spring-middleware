package com.aric.middleware.dbrouter.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DBRouterAnnotation {
    String key();
}
