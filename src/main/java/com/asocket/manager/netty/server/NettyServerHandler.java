package com.asocket.manager.netty.server;

import com.asocket.manager.system.constants.Constant;
import com.asocket.manager.util.ByteUtils;
import com.asocket.manager.util.MsgCreator;
import com.asocket.manager.vo.SzHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;

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

        // 获取第一条消息
        int dataLen = ByteUtils.hBytesToShort(ByteUtils.subBytes(msgBytes, 2, 2));  // 数据包长度(不包括消息头)
//        int msgTime = ByteUtils.hBytesToInt(ByteUtils.subBytes(msgBytes, 12, 4));  // 消息发送时间
//        int seqNo = ByteUtils.hBytesToShort(ByteUtils.subBytes(msgBytes, 16, 2));  // 消息序号(1-32768)循环

        // 处理第一条消息
        recordBytes = ByteUtils.subBytes(msgBytes, 0, dataLen + Constant.HEAD_LEN);
        ackAndSave(ctx, recordBytes);

        // 截取剩余字节(此处理方式防止数据粘包)
        byte[] restBytes = ByteUtils.subBytes(msgBytes, dataLen + Constant.HEAD_LEN, (msgLength - dataLen - Constant.HEAD_LEN));
        while (restBytes.length > 0) {
            dataLen = ByteUtils.hBytesToShort(ByteUtils.subBytes(restBytes, 2, 2));
            recordBytes = ByteUtils.subBytes(restBytes, 0, dataLen + Constant.HEAD_LEN);
            ackAndSave(ctx, recordBytes);

            // 继续截取
            restBytes = ByteUtils.subBytes(restBytes, dataLen + Constant.HEAD_LEN, (restBytes.length - dataLen - Constant.HEAD_LEN));
        }

        // 释放消息
        buf.release();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = client.getAddress().getHostAddress();
        LOGGER.info("【服务端】客户端:{}已连接到服务端,通道编号:{}",clientIp,ctx.channel().hashCode());
        // 设置最后心跳接收时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(),new Date());
        // 开启心跳监测线程
        Thread heartCheck = new Thread(new HeartBeatChecker(ctx.channel()));
        heartCheck.start();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = client.getAddress().getHostAddress();
        LOGGER.info("【服务端】客户端:{}已从服务端断开,通道编号:{}",clientIp,ctx.channel().hashCode());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录错误信息
        LOGGER.error("【服务端】连接异常捕捉:", cause);

        // 内部出错不关闭与客户端建立的连接
        //ctx.close();
    }

    /**
     * 应答客户端ACK
     *
     * @param ctx         通道上下文
     * @param recordBytes 单条消息(包括消息头和消息体)
     */
    private void ackAndSave(ChannelHandlerContext ctx, byte[] recordBytes) {
        // 1.判断数据长度是否足够
        if (recordBytes.length >= Constant.HEAD_LEN) {
            SzHeader recvHeader = MsgCreator.createRecvHeader(recordBytes);
            if (null != recvHeader){
                // 2.判断消息类型
                if(recvHeader.getMsgType() == (short)0x8080){
                    // 收到的消息为心跳,记录最新心跳时间
                    Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(),new Date());
                }else {
                    // 3.建立通道消息缓冲区,用于写入响应头
                    SzHeader szHeader = MsgCreator.createAckHeader(recordBytes);
                    ByteBuf ack = ctx.alloc().buffer(Constant.HEAD_LEN);
                    ack.writeBytes(szHeader.toByte());
                    ctx.writeAndFlush(ack);

                    // 4.保存数据
                }
            }
        }else {
            LOGGER.warn("收到消息长度的长度不够!");
        }
    }
}
