
package com.asocket.manager.netty.client;

import com.asocket.manager.system.constants.Constant;
import com.asocket.manager.util.MsgCreator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MessageSender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    private Channel channel;

    public MessageSender() {

    }

    public MessageSender(Channel channel) {
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
            int pushTime = (int) (new Date().getTime() / 1000);
            short seqNo = Constant.getSeqNo();
            byte[] data = MsgCreator.createAppData("cgx", pushTime, seqNo);
            ByteBuf bf = this.channel.alloc().buffer(23);
            bf.writeBytes(data);
            this.channel.writeAndFlush(bf);

            // 处理被放入的消息 update conn_sen_main set pushTime='' and seqNo=''

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.warn("【客户端】客户端连接通道:{}异常,已关闭消息发送线程!", this.channel.hashCode());
    }
}
