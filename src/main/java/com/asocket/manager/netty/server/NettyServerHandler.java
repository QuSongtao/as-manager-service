package com.asocket.manager.netty.server;

import com.asocket.manager.util.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 服务端消息处理器
 * 1.接收客户端发送的消息
 * 2.应答客户端
 *
 * @author qust
 * @version 1.0 20180918
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;

        // 读取所有字节到字节数组
        int msgLength = buf.readableBytes();
        byte[] msgBytes = new byte[msgLength];
        buf.readBytes(msgBytes);
        byte[] recordBytes;

        // 获取消息长度
        int recordLength = ByteUtils.hBytesToInt(ByteUtils.subBytes(msgBytes, 0, 4));

        // 处理第一条消息
        recordBytes = ByteUtils.subBytes(msgBytes, 0, recordLength);
        response(ctx, recordBytes);
        // todo-
        byte[] restBytes = ByteUtils.subBytes(msgBytes, recordLength, (msgLength - recordLength));
        while (restBytes.length > 0) {
            recordLength = ByteUtils.hBytesToInt(ByteUtils.subBytes(restBytes, 0, 4));
            recordBytes = ByteUtils.subBytes(restBytes, 0, recordLength);
            response(ctx, recordBytes);
            // todo-
            restBytes = ByteUtils.subBytes(restBytes, recordLength, (restBytes.length - recordLength));
        }
        buf.release();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = client.getAddress().getHostAddress();
        LOGGER.info("客户端:" + clientIp + "已连接!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = client.getAddress().getHostAddress();
        LOGGER.info("客户端:" + clientIp + "已断开连接!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录错误信息
        LOGGER.error("连接异常捕捉:", cause);

        // 内部出错不关闭与客户端建立的连接
        //ctx.close();

    }

    /**
     * 应答客户端ACK
     *
     * @param ctx         通道上下文
     * @param recordBytes 单条消息(包括消息头和消息体)
     */
    private void response(ChannelHandlerContext ctx, byte[] recordBytes) {
        ByteBuf ack = ctx.alloc().buffer(12); // 消息头长度12字节
        ack.writeBytes(ByteUtils.subBytes(recordBytes, 0, 12));
        ctx.writeAndFlush(ack);
        // 处理具体业务

    }

}
