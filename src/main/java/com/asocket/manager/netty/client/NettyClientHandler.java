/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.netty.client;

import com.asocket.manager.util.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private NettyClientThread nettyClientThread;

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        while (true){
//            byte[][] b = new byte[8][];
//            b[0] = ByteUtils.toHH(16);
//            b[1] = ByteUtils.toHH(10);
//            b[2] = ByteUtils.toHH(1);
//            b[3] ="cgx1".getBytes();
//            b[4] = ByteUtils.toHH(17);
//            b[5] = ByteUtils.toHH(20);
//            b[6] = ByteUtils.toHH(2);
//            b[7] ="cgx22".getBytes();
//            LOGGER.info("start");
//            ByteBuf req = ctx.alloc().buffer(33);
//            req.writeBytes(ByteUtils.bbToBytes(b,33));
//            ctx.writeAndFlush(req);
//            Thread.sleep(2500);
//            LOGGER.info("end");
//        }
        // 启动发送
        Thread sender = new Thread(new ClientSender(ctx.channel()));
        sender.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        byte[] msgBytes = new byte[m.readableBytes()];
        m.readBytes(msgBytes);
        System.out.println("长度:" + msgBytes.length);
        System.out.println("收到服务器响应长度: " + ByteUtils.hBytesToInt(ByteUtils.subBytes(msgBytes, 0, 4)));
        System.out.println("收到服务器报文ID为: " + ByteUtils.hBytesToInt(ByteUtils.subBytes(msgBytes, 4, 4)));
        System.out.println("收到服务器消息序号: " + ByteUtils.hBytesToInt(ByteUtils.subBytes(msgBytes, 8, 4)));
        m.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // 记录错误信息
        LOGGER.error("客户端出现异常,捕捉结果:", cause);

        // 关闭连接
        ctx.close();
    }
}
