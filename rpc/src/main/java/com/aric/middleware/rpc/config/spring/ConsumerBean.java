package com.aric.middleware.rpc.config.spring;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.ProviderConfig;
import com.aric.middleware.rpc.network.Request;
import com.aric.middleware.rpc.proxy.ConsumerBeanProxy;
import com.aric.middleware.rpc.register.RedisRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * <rpc:consumer id="helloService" nozzle="com.aric.middleware.rpcprovider.export.HelloService" />
 */
public class ConsumerBean implements FactoryBean {
    private final Logger logger = LoggerFactory.getLogger(ConsumerBean.class);

    private String nozzle;

    public String getNozzle() {
        return nozzle;
    }

    public void setNozzle(String nozzle) {
        this.nozzle = nozzle;
    }

    @Override
    public Object getObject() throws Exception {
        ProviderConfig provider = RedisRegistryCenter.getProvider(this.nozzle);
        if (null == provider) {
            throw new RuntimeException("未找到注册的接口: " + this.nozzle);
        }
        logger.info("找到注册的接口: {}", JSON.toJSONString(provider));

        Request request = new Request();
        request.setUuid(String.valueOf(UUID.randomUUID()));
        request.setProviderConfig(provider);

        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{Class.forName(nozzle)},
                new ConsumerBeanProxy(request)
        );
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return Class.forName(nozzle);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
