/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.netty.server;

import com.asocket.manager.system.constants.Constant;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class HeartBeatChecker implements Runnable {
    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatChecker.class);
    // 连接通道
    private Channel channel;

    public HeartBeatChecker(Channel channel){
        this.channel = channel;
    }

    public HeartBeatChecker(){

    }

    @Override
    public void run() {
        boolean running = true;
        while (running){
            if(!this.channel.isActive()){
                running = false;
                continue;
            }

            try {
                // 每2秒判断一次
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error("线程SLEEP出现异常!");
            }

            if(new Date().getTime() - Constant.LAST_RECV_TIME.get(this.channel.hashCode()).getTime() > Constant.TIMEOUT_MS){
                LOGGER.warn("客户端无心跳,主动断开客户端!");
                Constant.LAST_RECV_TIME.remove(this.channel.hashCode());
                // 超过60秒无响应则断开客户端
                this.channel.close();
                running = false;
            }
        }
        LOGGER.warn("通道关闭,心跳侦听结束!");
    }
}
