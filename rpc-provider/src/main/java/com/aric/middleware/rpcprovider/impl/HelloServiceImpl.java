package com.aric.middleware.rpcprovider.impl;

import com.aric.middleware.rpcprovider.export.HelloService;
import com.aric.middleware.rpcprovider.export.Hi;
import org.springframework.stereotype.Component;

@Component("helloService")
public class HelloServiceImpl implements HelloService {

    @Override
    public String hi() {
        return "hi bugstack rpc";
    }

    @Override
    public String say(String str) {
        return str;
    }

    @Override
    public String sayHi(Hi hi) {
        return hi.getUserName() + " say：" + hi.getSayMsg();
    }

}
