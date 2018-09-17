/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 10)
public class NettyStarter implements ApplicationRunner {

    // 日志
    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
    @Autowired
    private NettyServer nettyServer;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        LOGGER.info("socket server starting...");
        Thread thread = new Thread(new NettyServerThread());
        // 启动netty服务
        thread.start();
    }

    private class NettyServerThread implements Runnable {
        @Override
        public void run() {
            nettyServer.run();
        }
    }
}
