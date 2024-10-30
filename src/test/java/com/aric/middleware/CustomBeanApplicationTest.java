package com.aric.middleware;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CustomBeanApplicationTest {
    @Test
    public void test_customBean() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config.xml");
        CustomBean customBean = applicationContext.getBean("customBean1", CustomBean.class);
        CustomBean customBean2 = applicationContext.getBean("customBean2", CustomBean.class);

        System.out.println(JSON.toJSONString(customBean));
        System.out.println(JSON.toJSONString(customBean2));
    }
}
