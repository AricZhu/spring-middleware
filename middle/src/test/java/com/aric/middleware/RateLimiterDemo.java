package com.aric.middleware;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterDemo {
    //每秒只发出 1 个令牌
    private final RateLimiter rateLimiter = RateLimiter.create(1);

    public void testLimit() {
        int count = 0;
        for (;;) {
            if (rateLimiter.tryAcquire()) {
                System.out.println(System.currentTimeMillis() / 1000);
                count++;
                if (count > 10) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        RateLimiterDemo rateLimiterDemo = new RateLimiterDemo();
        rateLimiterDemo.testLimit();
    }
}
