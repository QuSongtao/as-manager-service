package com.asocket.manager.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Netty服务端
 *
 * @author qust
 * @version 1.0 20180918
 */
@Component
public class NettyServer {

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    // 端口号
    @Value("${netty.port}")
    private int port;

    // 启动服务器方法
    public void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup);
            server.channel(NioServerSocketChannel.class);
            server.option(ChannelOption.SO_BACKLOG, 1024);
            server.childOption(ChannelOption.TCP_NODELAY, true);
            server.childOption(ChannelOption.SO_KEEPALIVE, true);
            server.childHandler(new NettyServerInitializer());

            // 绑定端口,开始接收进来的连接
            ChannelFuture channelFuture = server.bind(port).sync();
            LOGGER.info("通信服务端启动成功,端口:[" + port + "]等待客户端连接 ");
            // 等待服务器socket关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error("通信服务端异常:", e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            LOGGER.warn("通信服务端将在10秒后重启!");
            try {
                Thread.sleep(10 * 1000);
                Thread thread = new Thread(new NettyServerThread());
                // 启动netty服务
                thread.start();
            } catch (InterruptedException e) {
                LOGGER.error("线程SLEEP出现异常!");
            }
        }
    }
}
