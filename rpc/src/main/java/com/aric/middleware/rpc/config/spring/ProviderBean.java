package com.aric.middleware.rpc.config.spring;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.LocalServer;
import com.aric.middleware.rpc.config.ProviderConfig;
import com.aric.middleware.rpc.register.RedisRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 将接口信息注册到注册中心
 * <rpc:provider id="helloServiceRpc" nozzle="com.aric.middleware.rpc.provider.export.HelloService"
 *                   ref="helloService"/>
 */
public class ProviderBean implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(ProviderBean.class);

    private String nozzle;
    private String ref;

    public String getNozzle() {
        return nozzle;
    }

    public void setNozzle(String nozzle) {
        this.nozzle = nozzle;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setNozzle(nozzle);
        providerConfig.setRef(ref);
        providerConfig.setHost(LocalServer.getHost());
        providerConfig.setPort(LocalServer.getPort());

        RedisRegistryCenter.addProvider(providerConfig);

        logger.info("注册生产者：{}", JSON.toJSONString(providerConfig));
    }
}
