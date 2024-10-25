package com.aric.middleware.mybatis.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;

public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {
    private String basePackage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String packageSearchPath = "classpath*:" + basePackage.replace(".", "/") + "/**/*.class";
        PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = pathResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                // 获取类的元数据
                SimpleMetadataReaderFactory simpleMetadataReaderFactory = new SimpleMetadataReaderFactory();
                MetadataReader metadataReader = simpleMetadataReaderFactory.getMetadataReader(resource);

                // 从元数据构造出 BeanDefinition
                ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);

                // com.aric.middleware.mybatis.dao.IUserDao -> IUserDao
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));
                beanDefinition.setResource(resource);
                beanDefinition.setSource(resource);
                beanDefinition.setScope("singleton");

                // 添加构造函数的操作，下面的代理对象需要使用到
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());

                // 设置代理类为实际的 Bean 类
                beanDefinition.setBeanClass(MapperFactoryBean.class);

                BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);

                // 注册 Bean
                registry.registerBeanDefinition(beanName, beanDefinitionHolder.getBeanDefinition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
