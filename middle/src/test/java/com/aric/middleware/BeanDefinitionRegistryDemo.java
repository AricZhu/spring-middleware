package com.aric.middleware;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class BeanDefinitionRegistryDemo implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(CustomBean.class)
                .getBeanDefinition();

        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue("xiaoming");
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(18);

        registry.registerBeanDefinition("customBean", beanDefinition);
    }
}
