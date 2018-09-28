/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.suncd.conn.netty.service.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerThread implements Runnable {
    @Autowired
    private NettyServer nettyServer;

    @Override
    public void run() {
        nettyServer.startServer();
    }
}
