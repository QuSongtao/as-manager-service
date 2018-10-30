/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.suncd.conn.netty.service.messageservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyClientThread implements Runnable {
    @Autowired
    private NettyClient nettyClient;

    @Override
    public void run() {
        nettyClient.startClient();
    }
}
