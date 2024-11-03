package com.aric.middleware.rpc.config.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RpcNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("provider", new MyBeanDefinitionParser(ProviderBean.class));
        registerBeanDefinitionParser("consumer", new MyBeanDefinitionParser(ConsumerBean.class));
    }
}
