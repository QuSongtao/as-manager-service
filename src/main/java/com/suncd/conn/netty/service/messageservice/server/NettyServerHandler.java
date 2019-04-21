package com.suncd.conn.netty.service.messageservice.server;

import com.suncd.conn.netty.dao.ConnConfSyscodeDao;
import com.suncd.conn.netty.dao.ConnRecvMainDao;
import com.suncd.conn.netty.dao.ConnRecvMsgDao;
import com.suncd.conn.netty.dao.ConnTotalNumDao;
import com.suncd.conn.netty.entity.ConnConfSyscode;
import com.suncd.conn.netty.entity.ConnRecvMain;
import com.suncd.conn.netty.entity.ConnRecvMsg;
import com.suncd.conn.netty.utils.ByteUtils;
import com.suncd.conn.netty.utils.CommonUtil;
import com.suncd.conn.netty.utils.MsgCreator;
import com.suncd.conn.netty.utils.SpringUtil;
import com.suncd.conn.netty.vo.SzHeader;
import com.suncd.conn.netty.system.constants.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

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
    private static final Logger LOGGER_WARN = LoggerFactory.getLogger("warnAndErrorLogger");
    private ConnTotalNumDao connTotalNumDao = SpringUtil.getBean(ConnTotalNumDao.class);
    private ConnRecvMainDao connRecvMainDao = SpringUtil.getBean(ConnRecvMainDao.class);
    private ConnRecvMsgDao connRecvMsgDao = SpringUtil.getBean(ConnRecvMsgDao.class);
    private ConnConfSyscodeDao connConfSyscodeDao = SpringUtil.getBean(ConnConfSyscodeDao.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 更新心跳时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());

        ByteBuf buf = (ByteBuf) msg;

        // 读取所有字节到字节数组
        int msgLength = buf.readableBytes();
        byte[] msgBytes = new byte[msgLength];
        buf.readBytes(msgBytes);
        byte[] recordBytes;

        // 获取第一条消息
        int dataLen = ByteUtils.hBytesToShort(ByteUtils.subBytes(msgBytes, 2, 2));  // 数据包长度(不包括消息头)

        // 判断消息长度是否合法
        if (dataLen > msgLength) {
            LOGGER_WARN.info("非法消息: 报文头中业务数据长度{}超过了消息总长度{},放弃处理!", dataLen, msgLength);
            String errorData = new String(ByteUtils.subBytes(msgBytes, Constant.HEAD_LEN, msgBytes.length - Constant.HEAD_LEN));
            LOGGER_WARN.info("非法消息内容: {}", errorData);
            return;
        }

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
        LOGGER_WARN.info("【服务端】{}已连接到服务端,通道编号:{}", clientIp, ctx.channel().hashCode());
        // 设置最后心跳接收时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());
        // 开启心跳监测线程
        Thread heartCheck = new Thread(new HeartBeatChecker(ctx.channel()));
        heartCheck.start();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = client.getAddress().getHostAddress();
        LOGGER_WARN.info("【服务端】{}已从服务端断开,通道编号:{}", clientIp, ctx.channel().hashCode());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录错误信息
        LOGGER_WARN.error("【服务端】连接异常捕捉:", cause);

        // 内部出错不关闭与客户端建立的连接
        //ctx.close();
    }

    /**
     * 1.检测心跳
     * 2.回执客户端
     * 3.保存业务消息
     *
     * @param ctx         通道上下文
     * @param recordBytes 单条消息(包括消息头和消息体)
     */
    private void ackAndSave(ChannelHandlerContext ctx, byte[] recordBytes) {
        // 1.判断数据长度是否足够
        if (recordBytes.length >= Constant.HEAD_LEN) {
            SzHeader recvHeader = MsgCreator.createRecvHeader(recordBytes);
            if (null != recvHeader) {
                // 2.判断消息类型
                if (recvHeader.getMsgType() == (short) 0x8080) {
                    // 收到的消息为心跳,记录最后心跳接收时间
                    Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());
                    LOGGER_WARN.info("【服务端】收到酸轧二级心跳报文,丢弃处理!");
                } else {
                    // 3.回执客户端
                    SzHeader szHeader = MsgCreator.createAckHeader(recordBytes);
                    ByteBuf ack = ctx.alloc().buffer(Constant.HEAD_LEN);
                    ack.writeBytes(szHeader.toByte());
                    ctx.writeAndFlush(ack);

                    // 4.插入接收消息表
                    // 取消息内容字符串
                    String msg = new String(ByteUtils.subBytes(recordBytes, Constant.HEAD_LEN, recordBytes.length - Constant.HEAD_LEN));
                    String msgId = UUID.randomUUID().toString();
                    ConnRecvMsg connRecvMsg = new ConnRecvMsg();
                    connRecvMsg.setId(msgId);
                    connRecvMsg.setCreateTime(new Date());
                    connRecvMsg.setMsgTxt(msg);
                    connRecvMsgDao.insertSelective(connRecvMsg);

                    // 5.插入接收总表
                    String telId = msg.substring(0, 4);
                    ConnRecvMain connRecvMain = new ConnRecvMain();
                    connRecvMain.setId(UUID.randomUUID().toString());
                    connRecvMain.setMsgId(msgId);
                    connRecvMain.setRecvTime(new Date());
                    connRecvMain.setTelId(telId);
                    connRecvMain.setSender(Constant.SOCKET_SZ);
                    connRecvMain.setSenderName(getSysNameByCode(Constant.SOCKET_SZ));
                    connRecvMain.setReceiver(Constant.MES_CR);
                    connRecvMain.setReceiverName(getSysNameByCode(Constant.MES_CR));
                    connRecvMainDao.insertSelective(connRecvMain);

                    // 6.更新统计表
                    connTotalNumDao.updateTotalNum("RR");

                    // 7.记录接收日志
                    LOGGER.info(msg);
                }
            }
        } else {
            LOGGER_WARN.warn("收到消息长度的长度不够!");
            // 更新统计表
            connTotalNumDao.updateTotalNum("RE");
        }
    }

    private String getSysNameByCode(String sysCode) {
        ConnConfSyscode connConfSyscode = connConfSyscodeDao.selectBySysCode(sysCode);
        if (null == connConfSyscode) {
            CommonUtil.SYSLOGGER.warn("【警告】通信系统编码:{} 没有在CONN_CONF_SYSCODE表中定义！", sysCode);
            return null;
        } else {
            return connConfSyscode.getSysName();
        }
    }

}
