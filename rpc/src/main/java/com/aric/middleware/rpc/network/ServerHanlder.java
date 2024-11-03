package com.aric.middleware.rpc.network;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.ProviderConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.time.LocalTime;

public class ServerHanlder extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(ServerHanlder.class);

    private transient ApplicationContext applicationContext;

    public ServerHanlder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;
        logger.info("server 接收到信息: {}", JSON.toJSONString(request));

        // 调用方法并返回
        ProviderConfig providerConfig = request.getProviderConfig();

        Object bean = this.applicationContext.getBean(providerConfig.getRef());
        Class<?> clzz = Class.forName(providerConfig.getNozzle());
        Method method = clzz.getMethod(request.getMethodName(), request.getParameterTypes());
        Object result = method.invoke(bean, request.getArgs());

        Response response = new Response();
        response.setRequestId(request.getUuid());
        response.setResult(result);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
