package com.aric.middleware.rpc.config;

import com.aric.middleware.rpc.network.ServerSocket;
import com.aric.middleware.rpc.register.RedisRegistryCenter;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServerAutoConfiguration implements ApplicationContextAware {
    @Resource
    private ServerProperties serverProperties;

    private final Logger logger = LoggerFactory.getLogger(ServerAutoConfiguration.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.info("初始化注册中心...");
        RedisRegistryCenter.init("127.0.0.1", 6379);
        logger.info("注册中心初始化完成");

        logger.info("启动服务端...");
        ServerSocket serverSocket = new ServerSocket(applicationContext, serverProperties.getPort());
        Thread thread = new Thread(serverSocket);
        thread.start();
        while (!serverSocket.isReady()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        logger.info("服务端启动完成, 地址: {}:{}", LocalServer.getHost(), LocalServer.getPort());
    }
}
