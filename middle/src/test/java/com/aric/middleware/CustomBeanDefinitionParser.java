package com.aric.middleware;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class CustomBeanDefinitionParser implements BeanDefinitionParser {
    private Class<?> clazz;

    public CustomBeanDefinitionParser(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String age = element.getAttribute("age");

        builder.addPropertyValue("name", name);
        builder.addPropertyValue("age", Integer.valueOf(age));

        parserContext.getRegistry().registerBeanDefinition(id, builder.getBeanDefinition());
        return builder.getBeanDefinition();
    }
}
