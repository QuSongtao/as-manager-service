package com.asocket.manager.netty.client;

import com.asocket.manager.system.Const;
import com.asocket.manager.util.MsgCreator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送心跳线程
 */
public class HeartBeat implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSender.class);

    private Channel channel;

    public HeartBeat(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            if (!this.channel.isActive()) {
                running = false;
            }
            ByteBuf bf = this.channel.alloc().buffer(Const.HEAD_LEN);
            bf.writeBytes(MsgCreator.createHeartBeatData());
            this.channel.writeAndFlush(bf);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
