package com.aric.middleware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MiddlewareApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testHystrix() {
		HystrixDemo hystrixDemo = new HystrixDemo("Bob", 500);
		String ret = hystrixDemo.execute();
		System.out.println(ret);
	}
}
