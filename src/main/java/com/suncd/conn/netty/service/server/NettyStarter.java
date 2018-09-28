package com.suncd.conn.netty.service.server;

import com.suncd.conn.netty.service.client.NettyClientThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * NettyServer随应用启动
 *
 * @author qust
 * @version 1.0 2080918
 */
@Component
@Order(value = 10)
public class NettyStarter implements ApplicationRunner {
    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private NettyServerThread nettyServerThread;

    @Autowired
    private NettyClientThread nettyClientThread;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        LOGGER.info("通信组件启动中...");
        Thread server = new Thread(nettyServerThread);
        // 启动netty服务端
        server.start();

        Thread client = new Thread(nettyClientThread);
        // 启动netty客户端
        client.start();


    }
}
