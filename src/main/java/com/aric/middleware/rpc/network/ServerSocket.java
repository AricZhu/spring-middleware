package com.aric.middleware.rpc.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerSocket implements Runnable {
    private ChannelFuture channelFuture;

    @Override
    public void run() {
        //创建两个线程组 boosGroup、workerGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建服务端的启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //给pipeline管道设置处理器
                            socketChannel.pipeline().addLast(
                                    new Encoder(Response.class),
                                    new Decoder(Request.class),
                                    new ServerHanlder()
                            );
                        }
                    });

            //绑定端口号，启动服务端
            this.channelFuture = bootstrap.bind(6666).sync();
            //对关闭通道进行监听
            this.channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public boolean isReady() {
        if (null != channelFuture) {
            return channelFuture.channel().isActive();
        }

        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        ServerSocket serverSocket = new ServerSocket();
        Thread thread = new Thread(serverSocket);
        thread.start();
        while (!serverSocket.isReady()) {
            Thread.sleep(500);
        }
        System.out.println("server 启动完成");
    }
}
