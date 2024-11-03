package com.aric.middleware.rpc.annotation;

import com.aric.middleware.rpc.config.ServerAutoConfiguration;
import com.aric.middleware.rpc.config.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(ServerProperties.class)
@Import({ServerAutoConfiguration.class})
public @interface EnableRpc {
}
