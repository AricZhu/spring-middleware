package com.aric.middleware;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.mybatis.SqlSession;
import com.aric.middleware.mybatis.SqlSessionFactory;
import com.aric.middleware.mybatis.dao.IUserDao;
import com.aric.middleware.mybatis.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MybatisSpringDemo {
    @Test
    public void test_SqlSessionFactoryBean() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");

        IUserDao userDao = applicationContext.getBean("IUserDao", IUserDao.class);
        User user = userDao.queryUserInfoById(2L);
        System.out.println(JSON.toJSONString(user));

//        SqlSessionFactory sqlSessionFactory = applicationContext.getBean("sqlSessionFactory", SqlSessionFactory.class);
//        SqlSession sqlSession = sqlSessionFactory.openSession();
//        Object o = sqlSession.selectOne("com.aric.middleware.mybatis.dao.IUserDao.queryUserInfoById", 1);
//        System.out.println(JSON.toJSONString(o));

//        IUserDao userDao = (IUserDao)applicationContext.getBean("mapperFactory");
//        User user = userDao.queryUserInfoById(2L);
//        System.out.println(JSON.toJSONString(user));
//
//        CustomBean customBean = applicationContext.getBean("customBean", CustomBean.class);
//        customBean.sayHello();
    }
}
