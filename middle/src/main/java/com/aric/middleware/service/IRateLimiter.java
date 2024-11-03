package com.aric.middleware.service;

import org.aspectj.lang.ProceedingJoinPoint;

public interface IRateLimiter {
    Object access(ProceedingJoinPoint jp, double permitsPerSecond) throws Throwable;
}
