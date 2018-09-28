package com.suncd.conn.netty.service.client;

import com.suncd.conn.netty.utils.ByteUtils;
import com.suncd.conn.netty.utils.MsgCreator;
import com.suncd.conn.netty.vo.SzHeader;
import com.suncd.conn.netty.system.constants.Constant;
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
        Thread sender = new Thread(new MessageSender(ctx.channel()));
        sender.start();

        // 启动心跳消息发送
        Thread heartBeat = new Thread(new HeartBeatSender(ctx.channel()));
        heartBeat.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf msgBuf = (ByteBuf) msg;
        byte[] msgBytes = new byte[msgBuf.readableBytes()];
        msgBuf.readBytes(msgBytes);
        int msgLen = msgBytes.length;

        if (msgLen >= Constant.HEAD_LEN) {
            // 截取0-20位消息头
            byte[] header = ByteUtils.subBytes(msgBytes, 0, Constant.HEAD_LEN);
            SzHeader szHeader = MsgCreator.createRecvHeader(header);
            processRecv(szHeader);

            // 截取剩余数据
            byte[] restHeader = ByteUtils.subBytes(msgBytes, Constant.HEAD_LEN, msgLen - Constant.HEAD_LEN);
            while (restHeader.length > 0) {
                header = ByteUtils.subBytes(restHeader, 0, Constant.HEAD_LEN);
                szHeader = MsgCreator.createRecvHeader(header);
                processRecv(szHeader);

                // 继续截取剩余数据
                restHeader = ByteUtils.subBytes(restHeader, Constant.HEAD_LEN, msgLen - Constant.HEAD_LEN);
            }
        }

        // 释放缓冲区
        msgBuf.release();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn("【客户端】客户端连接通道:{}被移除!",ctx.channel().hashCode());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // 记录错误信息
        LOGGER.error("【客户端】客户端出现异常,捕捉结果:", cause);

        // 关闭连接
        ctx.close();
    }

    private void processRecv(SzHeader szHeader){
        LOGGER.info("【客户端】客户端收到消息头:{}", szHeader.toString());
        // 1.获取消息sendTime和seqNo

        // 2.处理数据更新 delete from conn_send_main where pushTime = '' and seqNo = ''
        //               insert into conn_send_main_his
    }
}
