package com.aric.middleware.controller;

import com.aric.middleware.annotation.HystrixAnnotation;
import com.aric.middleware.annotation.MethodExtAnnotation;
import com.aric.middleware.annotation.RateLimiterAnnotation;
import com.aric.middleware.annotation.WhiteListAnnotation;
import com.aric.middleware.common.Result;
import com.aric.middleware.configuration.WhiteListConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MainController {
    @Autowired
    private WhiteListConfiguration whiteListConfiguration;

    @GetMapping("/hello")
    @WhiteListAnnotation(key = "userId")
    public Result hello(@RequestParam("userId") String userId) {
        return Result.success("userId=" + userId + ":" + whiteListConfiguration.getUsers() + ":" + whiteListConfiguration.getIsOpen());
    }

    @GetMapping("/world")
    public Result world() {
        return Result.success("world");
    }

    // 超时熔断-正常返回
    @GetMapping("/hystrix/demo1")
    @HystrixAnnotation(timeoutMs = 500)
    public Result hystrixDemo1() throws InterruptedException {
        Thread.sleep(400);
        return Result.success("超时熔断正常返回");
    }

    // 超时熔断-超时
    @GetMapping("/hystrix/demo2")
    @HystrixAnnotation(timeoutMs = 500)
    public Result hystrixDemo2() throws InterruptedException {
        Thread.sleep(600);
        return Result.success("超时熔断超时返回");
    }

    // 超时熔断-默认值
    @GetMapping("/hystrix/demo3")
    @HystrixAnnotation
    public Result hystrixDemo3() throws InterruptedException {
        Thread.sleep(600);
        return Result.success("超时熔断默认值");
    }

    // 限流返回
    @GetMapping("/ratelimit")
    @RateLimiterAnnotation(permitPerSecond = 1)
    public Result ratelimit() {
        return Result.success("限流正常返回!");
    }

    @GetMapping("/queryInfo")
    @MethodExtAnnotation(method = "doFilter")
    public Result queryInfo(@RequestParam("userId") String userId) {
        return Result.success("查询用户: " + userId);
    }

    public boolean doFilter(String userId) {
        if ("xiaohong".equals(userId) || "xiaoming".equals(userId)) {
            return false;
        }

        return true;
    }
}
