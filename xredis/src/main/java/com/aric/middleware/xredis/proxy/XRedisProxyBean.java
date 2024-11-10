package com.aric.middleware.xredis.proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class XRedisProxyBean<T> implements FactoryBean<T> {
    private Class<T> mapperInterface;

    @Autowired
    private Jedis jedis;

    public XRedisProxyBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("get")) {
                return jedis.srandmember(args[0].toString());
            }
            if (name.equals("set")) {
                return jedis.sadd(args[0].toString(), args[1].toString());
            }
            return "代理";
        };

        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{mapperInterface}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
