package com.asocket.manager.netty.server;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class NettyServerListener implements ServletContextListener {

    @Autowired
    private NettyServer nettyServer;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("starting...");
        Thread thread = new Thread(new NettyServerThread());
        // 启动netty服务
        thread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private class NettyServerThread implements Runnable {

        @Override
        public void run() {
            nettyServer.run();
        }
    }
}
