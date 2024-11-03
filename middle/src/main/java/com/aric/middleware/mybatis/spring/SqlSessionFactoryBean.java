package com.aric.middleware.mybatis.spring;

import com.aric.middleware.mybatis.Resource;
import com.aric.middleware.mybatis.SqlSessionFactory;
import com.aric.middleware.mybatis.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.Reader;

public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {
    private String resource;
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        Reader reader = Resource.getResourceAsReader(resource);
        // 参数修改
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(null, "");
    }

    @Override
    public SqlSessionFactory getObject() throws Exception {
        return this.sqlSessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
