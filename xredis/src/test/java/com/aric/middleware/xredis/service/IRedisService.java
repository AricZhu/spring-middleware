package com.aric.middleware.xredis.service;

import com.aric.middleware.xredis.annotation.XRedisAnnotation;

@XRedisAnnotation
public interface IRedisService {
    String get(String key);

    void set(String key, String val);
}
