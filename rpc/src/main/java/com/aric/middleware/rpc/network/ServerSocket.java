package com.aric.middleware.rpc.network;

import com.aric.middleware.rpc.config.LocalServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerSocket implements Runnable {
    private ChannelFuture channelFuture;

    private int port;

    private transient ApplicationContext applicationContext;

    public ServerSocket(ApplicationContext applicationContext, int port) {
        this.applicationContext = applicationContext;
        this.port = port;
    }

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
                                    new ServerHanlder(applicationContext)
                            );
                        }
                    });

            //绑定端口号，启动服务端
            int port = getIdlePort();
            System.out.println("port: " + port);
            this.channelFuture = bootstrap.bind(port).sync();
            LocalServer.setHost("127.0.0.1");
            LocalServer.setPort(port);
            //对关闭通道进行监听
            this.channelFuture.channel().closeFuture().sync();
            System.out.println("服务端关闭");
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
            System.out.println("finally 关闭");
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

    public int getIdlePort() throws UnknownHostException {
        return port;
//        for (int port = 25673; port <= 65535; port++) {
//            try (Socket serverSocket = new Socket("127.0.0.1", port)) {
//                return port; // 找到未被占用的端口
//            } catch (IOException e) {
//                // 端口被占用，继续下一个端口尝试
//            }
//        }
//        throw new RuntimeException("No available ports found.");
    }

    public static void main(String[] args) throws InterruptedException {
        ServerSocket serverSocket = new ServerSocket(null, 6666);
        Thread thread = new Thread(serverSocket);
        thread.start();
        while (!serverSocket.isReady()) {
            Thread.sleep(500);
        }
        System.out.println("server 启动完成");
    }
}
