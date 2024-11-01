package com.aric.middleware.rpc.network;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.ProviderConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientSocket implements Runnable {
    private ChannelFuture channelFuture;

    @Override
    public void run() {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            //创建bootstrap对象，配置参数
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(eventExecutors)
                    //设置客户端的通道实现类型
                    .channel(NioSocketChannel.class)
                    //使用匿名内部类初始化通道
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //添加客户端通道的处理器
                            ch.pipeline().addLast(
                                    new Encoder(Request.class),
                                    new Decoder(Response.class),
                                    new ClientHandler()
                            );
                        }
                    });

            //连接服务端
            this.channelFuture = bootstrap.connect("127.0.0.1", 6666).sync();
            //对通道关闭进行监听
            this.channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //关闭线程组
            eventExecutors.shutdownGracefully();
        }
    }

    public boolean isReady() {
        if (null != this.channelFuture) {
            return this.channelFuture.channel().isActive();
        }

        return false;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public Response writeMessage(Request request) throws InterruptedException, ExecutionException, TimeoutException {
        WriteFuture writeFuture = WriteFutureMap.getWriteFuture(request.getUuid());

        // 发送数据
        channelFuture.channel().writeAndFlush(request);

        // 设置超时时间等待返回数据
        Response response = writeFuture.get(10000, TimeUnit.MILLISECONDS);

        WriteFutureMap.removeWriteFuture(request.getUuid());

        return response;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        ClientSocket client = new ClientSocket();
        Thread thread = new Thread(client);
        thread.start();
        while (!client.isReady()) {
            Thread.sleep(500);
        }

        System.out.println("client 启动完成: " + LocalTime.now());

        Thread.sleep(2000);

        System.out.println("client 开始发送数据: " + LocalTime.now());

        Request request = new Request();
        request.setUuid(String.valueOf(UUID.randomUUID()));
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setPort(6666);
        providerConfig.setHost("127.0.0.1");
        providerConfig.setNozzle("com.aric.demo");
        request.setProviderConfig(providerConfig);

        Response response = client.writeMessage(request);
        System.out.println("接收到服务端响应: " + LocalTime.now() + " -> " + JSON.toJSONString(response));
    }
}
