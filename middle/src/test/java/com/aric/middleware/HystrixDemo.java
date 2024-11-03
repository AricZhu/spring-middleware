package com.aric.middleware;

import com.netflix.hystrix.*;

public class HystrixDemo extends HystrixCommand<String> {
    private String name;

    public HystrixDemo(String name, int timeoutMs) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(timeoutMs)));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        Thread.sleep(500);
        return "Hello " + name + " !";
    }

    @Override
    protected String getFallback() {
        return "fallback";
    }
}
