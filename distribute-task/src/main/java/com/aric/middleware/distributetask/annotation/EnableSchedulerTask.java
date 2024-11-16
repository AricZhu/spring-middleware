package com.aric.middleware.distributetask.annotation;

import com.aric.middleware.distributetask.config.SchedulerAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SchedulerAutoConfig.class})
public @interface EnableSchedulerTask {
}
