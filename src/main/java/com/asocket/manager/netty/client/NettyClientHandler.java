package com.asocket.manager.netty.client;

import com.asocket.manager.system.Const;
import com.asocket.manager.util.ByteUtils;
import com.asocket.manager.util.MsgCreator;
import com.asocket.manager.vo.SzHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 启动业务消息发送
        Thread sender = new Thread(new ClientSender(ctx.channel()));
        sender.start();

        // 启动心跳消息发送
        Thread heartBeat = new Thread(new HeartBeat(ctx.channel()));
        heartBeat.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf msgBuf = (ByteBuf) msg;
        byte[] msgBytes = new byte[msgBuf.readableBytes()];
        msgBuf.readBytes(msgBytes);
        int msgLen = msgBytes.length;

        if (msgLen >= Const.HEAD_LEN) {
            // 截取0-20位消息头
            byte[] header = ByteUtils.subBytes(msgBytes, 0, Const.HEAD_LEN);
            SzHeader szHeader = MsgCreator.createRecvHeader(header);
            processRecv(szHeader);

            // 截取剩余数据
            byte[] restHeader = ByteUtils.subBytes(msgBytes, Const.HEAD_LEN, msgLen - Const.HEAD_LEN);
            while (restHeader.length > 0) {
                header = ByteUtils.subBytes(restHeader, 0, Const.HEAD_LEN);
                szHeader = MsgCreator.createRecvHeader(header);
                processRecv(szHeader);

                // 继续截取剩余数据
                restHeader = ByteUtils.subBytes(restHeader, Const.HEAD_LEN, msgLen - Const.HEAD_LEN);
            }
        }

        // 释放缓冲区
        msgBuf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // 记录错误信息
        LOGGER.error("客户端出现异常,捕捉结果:", cause);

        // 关闭连接
        ctx.close();
    }

    private void processRecv(SzHeader szHeader){
        LOGGER.info("客户端收到消息头:{}", szHeader.toString());
        // 1.获取消息头类型,只取正常响应的

        // 2.处理数据更新
    }
}
