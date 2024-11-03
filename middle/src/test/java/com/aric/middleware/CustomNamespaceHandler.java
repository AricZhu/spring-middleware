package com.aric.middleware;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CustomNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("custom-tag", new CustomBeanDefinitionParser(CustomBean.class));
    }
}
