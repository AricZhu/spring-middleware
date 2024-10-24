package com.aric.middleware.mybatisSpring;

import com.aric.middleware.mybatis.SqlSession;
import com.aric.middleware.mybatis.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

public class MapperFactoryBean<T> implements FactoryBean<T> {
    private Class<T> mapperInterface;
    private SqlSessionFactory sqlSessionFactory;

    public MapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
        this.mapperInterface = mapperInterface;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public T getObject() throws Exception {
        InvocationHandler handler = (proxy, method, args) -> {
            System.out.println("你被代理了，执行SQL操作: " + method.getName());
            // 获取全限定名
            String statement = mapperInterface.getName() + "." + method.getName();

            // 开启 ORM 操作
            SqlSession sqlSession = sqlSessionFactory.openSession();

            // 根据返回值和参数类型选择调用的 ORM 的 API
            Class<?> returnType = method.getReturnType();
            try {
                // 无参数
                if (null == args || args.length == 0) {
                    // 返回列表
                    if (List.class.isAssignableFrom(returnType)) {
                        return sqlSession.selectList(statement);
                    } else { // 返回单个对象
                        return sqlSession.selectOne(statement);
                    }
                } else { // 有参
                    // 返回列表
                    if (List.class.isAssignableFrom(returnType)) {
                        return sqlSession.selectList(statement, args[0]);
                    } else { // 返回单个对象
                        return sqlSession.selectOne(statement, args[0]);
                    }
                }
            } finally {
                sqlSession.close();
            }
        };

        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{mapperInterface}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
