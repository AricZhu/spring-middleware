package com.aric.middleware.xredis.config;

import com.aric.middleware.xredis.proxy.XRedisProxyBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.beans.Introspector;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableConfigurationProperties(XRedisProperties.class)
public class AutoConfiguration implements InitializingBean {
    @Autowired
    private XRedisProperties properties;

    @Bean
    public Jedis jedis() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(5);
        poolConfig.setTestOnBorrow(true);

        return new JedisPool(poolConfig, properties.getHost(), properties.getPort()).getResource();
    }

    public static class XRegister implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

        private BeanFactory beanFactory;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            String packageSearchPath = "classpath*:com/aric/middleware/xredis/**/*.class";
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            try {
                Resource[] resources = patternResolver.getResources(packageSearchPath);
                SimpleMetadataReaderFactory simpleMetadataReaderFactory = new SimpleMetadataReaderFactory();

                for (Resource resource : resources) {
                    MetadataReader metadataReader = simpleMetadataReaderFactory.getMetadataReader(resource);
                    AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                    if (!annotationMetadata.hasAnnotation("com.aric.middleware.xredis.annotation.XRedisAnnotation")) {
                        continue;
                    }
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    Class<?> aClass = Class.forName(classMetadata.getClassName());
                    ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadataReader);
                    String beanName = Introspector.decapitalize(ClassUtils.getShortName(beanDefinition.getBeanClassName()));

                    beanDefinition.setSource(resource);
                    beanDefinition.setResource(resource);
                    beanDefinition.setScope("singleton");
                    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());

                    beanDefinition.setBeanClass(XRedisProxyBean.class);

                    BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                    registry.registerBeanDefinition(beanName, beanDefinitionHolder.getBeanDefinition());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Configuration
    @Import(XRegister.class)
    public static class StartRegisterConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() throws Exception {

        }
    }
}
