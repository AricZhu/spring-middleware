package com.aric.middleware.rpc.proxy;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.ProviderConfig;
import com.aric.middleware.rpc.network.ClientSocket;
import com.aric.middleware.rpc.network.Request;
import com.aric.middleware.rpc.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ConsumerBeanProxy implements InvocationHandler {
    private final Logger logger = LoggerFactory.getLogger(ConsumerBeanProxy.class);

    private final Request request;

    private ClientSocket clientSocket;

    public ConsumerBeanProxy(Request request) {
        this.request = request;
    }

    public ClientSocket getClient() throws InterruptedException {
        if (null != this.clientSocket) {
            return this.clientSocket;
        }
        ProviderConfig providerConfig = request.getProviderConfig();

        ClientSocket clientSocket = new ClientSocket(providerConfig.getHost(), providerConfig.getPort());
        this.clientSocket = clientSocket;
        new Thread(clientSocket).start();
        while (!clientSocket.isReady()) {
            Thread.sleep(500);
        }
        logger.info("代理类 client 启动完成");

        return this.clientSocket;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("调用代理类方法: {}", method.getName());
        ClientSocket client = getClient();
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setArgs(args);
        logger.info("向服务端发送消息：{}", JSON.toJSONString(request));
        Response response = client.writeMessage(request);

        return response.getResult();
    }
}
