package com.aric.middleware.mybatis.springbootstarter;

import com.aric.middleware.mybatis.SqlSessionFactory;
import com.aric.middleware.mybatis.SqlSessionFactoryBuilder;
import com.aric.middleware.mybatis.spring.MapperScannerConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
public class MybatisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(MybatisProperties mybatisProperties, Connection connection) throws Exception {
        return new SqlSessionFactoryBuilder().build(connection, mybatisProperties.getMapperLocations());
    }

    @Bean
    @ConditionalOnMissingBean
    public Connection connection(MybatisProperties mybatisProperties) throws SQLException {
        return DriverManager.getConnection(mybatisProperties.getUrl(), mybatisProperties.getUsername(), mybatisProperties.getPassword());
    }

    public static class AutoConfiguredMapperScannerRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {
        private String basePackage;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            beanDefinitionBuilder.addPropertyValue("basePackage", this.basePackage);
            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), beanDefinitionBuilder.getBeanDefinition());
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.basePackage = environment.getProperty("mybatis.base-package");
        }
    }

    @Configuration
    @Import({AutoConfiguredMapperScannerRegistrar.class})
    public static class MapperScannerRegistrarNotFoundConfiguration {

    }
}
