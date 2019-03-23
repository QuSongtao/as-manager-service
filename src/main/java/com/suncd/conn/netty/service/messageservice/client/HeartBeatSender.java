package com.suncd.conn.netty.service.messageservice.client;

import com.suncd.conn.netty.utils.MsgCreator;
import com.suncd.conn.netty.system.constants.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送心跳线程,每隔30秒钟发送一次心跳
 */
public class HeartBeatSender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private Channel channel;

    public HeartBeatSender(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            if (!this.channel.isActive()) {
                running = false;
                continue;
            }
            ByteBuf bf = this.channel.alloc().buffer(Constant.HEAD_LEN);
            bf.writeBytes(MsgCreator.createHeartBeatData());
            this.channel.writeAndFlush(bf);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.warn("【客户端】客户端连接通道:{}异常,已关闭心跳发送线程!",this.channel.hashCode());
    }
}
