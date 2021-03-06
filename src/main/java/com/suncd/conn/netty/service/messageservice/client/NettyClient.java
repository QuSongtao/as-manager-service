package com.suncd.conn.netty.service.messageservice.client;

import com.suncd.conn.netty.system.constants.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Netty客户端
 *
 * @author qust
 * @version 1.0 20180926
 */
@Component
public class NettyClient {

    @Autowired
    private NettyClientThread nettyClientThread;

    @Value("${netty.remote.addr}")
    private String host;

    @Value("${netty.remote.port}")
    private int port;

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);
    private static final Logger LOGGER_WARN = LoggerFactory.getLogger("warnAndErrorLogger");

    public void startClient() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap client = new Bootstrap();
            client.group(workerGroup);
            client.channel(NioSocketChannel.class);
            client.option(ChannelOption.SO_KEEPALIVE, true);
            client.handler(new NettyClientInitializer());

            // 启动客户端连接
            ChannelFuture f = client.connect(host, port).sync();
            LOGGER.info("【客户端】连接远程主机{}:{}成功,通道编号:{}", host, port, f.channel().hashCode());

            // 设置客户端状态为1-运行
            Constant.CLIENT_STATUS = 1;

            // 等待连接被关闭,执行如下
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER_WARN.error("【客户端】通信客户端组件异常:", e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();

            // 设置客户端状态为1-运行
            Constant.CLIENT_STATUS = 0;
            // LOGGER_WARN.warn("【客户端】通信客户端组件将在10秒后重启");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage());
            }
            // 重启客户端
            Thread client = new Thread(nettyClientThread);
            client.start();
        }
    }
}
