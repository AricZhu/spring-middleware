package com.aric.middleware.xredis;

import com.aric.middleware.xredis.config.AutoConfiguration;
import com.aric.middleware.xredis.service.IRedisService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = Application.class)
public class TestApplication {

    private final Logger logger = LoggerFactory.getLogger(TestApplication.class);

    @Autowired
    private IRedisService redisService;

    @Test
    public void test_RedisService() {
        redisService.set("b_info_user", "小傅哥，一个并不简单的男人！");

        String result = redisService.get("b_info_user");
        logger.info("获取 Redis key：{} 信息：{}", "b_info_user", result);
    }

    @Test
    public void test() {
        String s = redisService.get("hello");
        System.out.println(s);
    }
}
