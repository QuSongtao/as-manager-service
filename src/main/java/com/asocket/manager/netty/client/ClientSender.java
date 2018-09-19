
package com.asocket.manager.netty.client;

import com.asocket.manager.util.ByteUtils;
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
            byte[][] bb = new byte[8][];
            bb[0] = ByteUtils.toHH(16);
            bb[1] = ByteUtils.toHH(10);
            bb[2] = ByteUtils.toHH(1);
            bb[3] = "cgx1".getBytes();
            bb[4] = ByteUtils.toHH(17);
            bb[5] = ByteUtils.toHH(20);
            bb[6] = ByteUtils.toHH(2);
            bb[7] = "cgx22".getBytes();
            ByteBuf bf = this.channel.alloc().buffer(33);
            bf.writeBytes(ByteUtils.bbToBytes(bb, 33));
            this.channel.writeAndFlush(bf);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
