
package com.asocket.manager.netty.client;

import com.asocket.manager.system.constants.Constant;
import com.asocket.manager.util.MsgCreator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSender implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSender.class);

    private Channel channel;

    public ClientSender(){

    }

    public ClientSender(Channel channel){
        this.channel = channel;
    }

    @Override
    public void run() {
        while (true) {
            if(!this.channel.isActive()){
                LOGGER.warn("客户端连接通道异常,关闭发送!");
                break;
            }
            byte[] data = MsgCreator.createAppData("cgx",Constant.getSeqNo());
            ByteBuf bf = this.channel.alloc().buffer(23);
            bf.writeBytes(data);
            this.channel.writeAndFlush(bf);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
