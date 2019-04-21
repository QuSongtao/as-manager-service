/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.suncd.conn.netty.service.messageservice.server;

import com.suncd.conn.netty.system.constants.Constant;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 每隔2秒检测一次心跳时间是否在允许范围
 */
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
                Constant.LAST_RECV_TIME.remove(this.channel.hashCode());
                // 超过n秒无响应则断开客户端
                this.channel.close();
                LOGGER.warn("【服务端】服务端超过{}秒未收到客户端,已断开客户端,通道编号:{}",Constant.TIMEOUT_MS/1000,this.channel.hashCode());
                running = false;
            }
        }
        Constant.LAST_RECV_TIME.remove(this.channel.hashCode());
        LOGGER.warn("【服务端】服务端连接通道:{}异常,已关闭心跳侦听线程!",this.channel.hashCode());
    }
}
