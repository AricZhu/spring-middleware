package com.aric.middleware;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.mybatis.dao.IUserDao;
import com.aric.middleware.mybatis.po.User;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisStarterDemo {
    private Logger logger = LoggerFactory.getLogger(MybatisStarterDemo.class);

    @Resource
    IUserDao userDao;

    @Test
    public void test_mybatisStarter() {
        User user = userDao.queryUserInfoById(2L);
        logger.info("测试结果: {}", JSON.toJSONString(user));

    }
}
