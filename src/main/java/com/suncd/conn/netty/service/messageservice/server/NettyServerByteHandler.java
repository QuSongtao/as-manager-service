package com.suncd.conn.netty.service.messageservice.server;

import com.suncd.conn.netty.dao.*;
import com.suncd.conn.netty.entity.ConnConfSyscode;
import com.suncd.conn.netty.entity.ConnRecvMain;
import com.suncd.conn.netty.entity.ConnRecvMainHis;
import com.suncd.conn.netty.entity.ConnRecvMsg;
import com.suncd.conn.netty.system.constants.Constant;
import com.suncd.conn.netty.utils.ByteUtils;
import com.suncd.conn.netty.utils.CommonUtil;
import com.suncd.conn.netty.utils.MsgCreator;
import com.suncd.conn.netty.utils.SpringUtil;
import com.suncd.conn.netty.vo.SzHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.UncategorizedSQLException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 服务端消息解码器
 * 1.接收客户端发送的消息
 * 2.应答客户端
 *
 * @author qust
 * @version 1.0 20190425
 */
public class NettyServerByteHandler extends ByteToMessageDecoder {
    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerByteHandler.class);
    private static final Logger LOGGER_WARN = LoggerFactory.getLogger("warnAndErrorLogger");
    private ConnTotalNumDao connTotalNumDao = SpringUtil.getBean(ConnTotalNumDao.class);
    private ConnRecvMainDao connRecvMainDao = SpringUtil.getBean(ConnRecvMainDao.class);
    private ConnRecvMsgDao connRecvMsgDao = SpringUtil.getBean(ConnRecvMsgDao.class);
    private ConnConfSyscodeDao connConfSyscodeDao = SpringUtil.getBean(ConnConfSyscodeDao.class);
    private ConnRecvMainHisDao connRecvMainHisDao = SpringUtil.getBean(ConnRecvMainHisDao.class);
    private Environment environment = SpringUtil.getBean(Environment.class);

    // 完整消息包括报文头和报文体
    private byte[] msgBuf;
    // 报文体被截断时的偏移量
    private int offset = 0;
    // 消息报文头
    private byte[] headerBuf;
    // 报文头被截断时的偏移量
    private int headerOffset = 0;
    // 缓存电文ID
    private String tempTelId = "";
    // 缓存电文数据长度
    private short tempDataLen = 0;
    // 缓存消息序号
    private short tempSeqNo = 0;
    // 缓存消息时间戳
    private int tempMsgTime = 0;

    /**
     * 消息解码器
     *
     * @param ctx 通道上下文
     * @param in  消息缓冲数据
     * @param out 输出
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 更新心跳时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());
        // 读取所有字节到字节数组
        int availableLength = in.readableBytes();
        LOGGER_WARN.info("字节数据长度: {}", availableLength);
        if (headerOffset > 0 && headerOffset < 20) {
            byte[] header = new byte[headerOffset];
            in.readBytes(header);
            // 拼接头部
            headerBuf = ByteUtils.addBytes(headerBuf, header);
        }

        // offset为0表示刚处理完上次的业务数据,或者首次接收数据
        if (offset == 0) {
            // 读取消息头
            if (null == headerBuf) {
                // header在消息末尾被截断的情况
                if (availableLength < Constant.HEAD_LEN) {
                    // 把所有的字节读到headerBuf中
                    headerBuf = new byte[availableLength];
                    in.readBytes(headerBuf);
                    // 记录header的剩余字节数,供下次读取
                    headerOffset = Constant.HEAD_LEN - availableLength;
                    if (checkHeader(in, availableLength)) {
                        return;
                    }
                    return;
                } else {
                    headerBuf = new byte[Constant.HEAD_LEN];
                    in.readBytes(headerBuf);
                    headerOffset = Constant.HEAD_LEN;
                    if (checkHeader(in, availableLength)) {
                        return;
                    }
                }
            }
            SzHeader szHeader = MsgCreator.createRecvHeader(headerBuf);

            // 获取业务数据body长度
            int dataLength = szHeader.getDataLen();
            // 计算本次是否读取完毕
            offset = dataLength + headerOffset - availableLength;
            if (offset == 0 || offset < 0) {
                // 本次已读取完毕
                msgBuf = new byte[dataLength];
                in.readBytes(msgBuf);
                msgBuf = ByteUtils.addBytes(headerBuf, msgBuf);
                in.discardReadBytes();
                ackAndSave(ctx, msgBuf);
            } else {
                // 本次未读取完整,装入本次读取的所有字节
                msgBuf = new byte[availableLength - Constant.HEAD_LEN];
                in.readBytes(msgBuf);
                msgBuf = ByteUtils.addBytes(headerBuf, msgBuf);
            }
        } else if (offset > 0) {
            // 未读取完整,待下次读取
            if (availableLength < offset) {
                // 还未读够,则全量读
                byte[] restData = new byte[availableLength];
                in.readBytes(restData);
                msgBuf = ByteUtils.addBytes(msgBuf, restData);
                offset = offset - availableLength;
            } else {
                // 已读够,则只读取offset偏移量的长度
                byte[] restData = new byte[offset];
                in.readBytes(restData);
                msgBuf = ByteUtils.addBytes(msgBuf, restData);
                // 交业务处理
                in.discardReadBytes();
                ackAndSave(ctx, msgBuf);
            }
        }
    }

    private boolean checkHeader(ByteBuf in, int availableLength) {
        if (-128 != (int) headerBuf[0]) {
            // 头部截取有问题,进行偏移
            // 重置read索引
            in.resetReaderIndex();
            // 查找是否有符合条件的字节,header字节数据是以-128打头
            int skip = in.bytesBefore((byte) -128);
            if (skip < 0) {
                in.skipBytes(availableLength);
            } else {
                in.skipBytes(skip);
            }
            headerBuf = null;
            headerOffset = 0;
            return true;
        }
        return false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
//        String clientIp = client.getAddress().getHostAddress();
        LOGGER_WARN.info("【服务端】{}已连接到服务端,通道编号:{}", "二级系统", ctx.channel().hashCode());
        // 设置最后心跳接收时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());
        if (!environment.getProperty("netty.checkHeartBeat").equals("0")) {
            // 开启心跳监测线程
            Thread heartCheck = new Thread(new HeartBeatChecker(ctx.channel()));
            heartCheck.start();
        }
    }

    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) {
//        InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
//        String clientIp = client.getAddress().getHostAddress();
        LOGGER_WARN.info("【服务端】{}已从服务端断开,通道编号:{}", "二级系统", ctx.channel().hashCode());
        ctx.channel().close();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录错误信息
        LOGGER_WARN.error("【服务端】连接异常捕捉:", cause);
        // 内部出错不关闭与客户端建立的连接
        //ctx.close();
        ctx.channel().close();
        ctx.close();
        offset = 0;
        msgBuf = null;
        headerBuf = null;
        headerOffset = 0;
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

                    String msgId = UUID.randomUUID().toString();
                    String mainId = UUID.randomUUID().toString();
                    // 截取消息内容(字符串格式)
                    String msg = new String(ByteUtils.subBytes(recordBytes, Constant.HEAD_LEN, recordBytes.length - Constant.HEAD_LEN));
                    String telId = msg.substring(0, 4);

                    // 3.1 判断前后两条消息是否为重复消息,重复则直接丢弃
                    // 判断条件4个：电文ID(telId)、数据长度(dataLen)、消息序号(seqNo)、时间戳(msgTime)
                    if (this.tempTelId.equals(telId) && this.tempDataLen == recvHeader.getDataLen() && this.tempSeqNo == recvHeader.getSeqNo() && this.tempMsgTime == recvHeader.getMsgTime()) {
                        LOGGER_WARN.warn("重复消息丢弃!");
                        // 更新缓存数据
                        this.tempTelId = telId;
                        this.tempDataLen = recvHeader.getDataLen();
                        this.tempSeqNo = recvHeader.getSeqNo();
                        this.tempMsgTime = recvHeader.getMsgTime();
                        // 返回,等待处理下一条数据
                        return;
                    }
                    // 3.2 更新缓存数据
                    this.tempTelId = telId;
                    this.tempDataLen = recvHeader.getDataLen();
                    this.tempSeqNo = recvHeader.getSeqNo();
                    this.tempMsgTime = recvHeader.getMsgTime();

                    try {
                        // 4.插入接收消息表
                        ConnRecvMsg connRecvMsg = new ConnRecvMsg();
                        connRecvMsg.setId(msgId);
                        connRecvMsg.setCreateTime(new Date());
                        connRecvMsg.setMsgTxt(msg);
                        connRecvMsgDao.insertSelective(connRecvMsg);

                        // 5.插入接收总表
                        ConnRecvMain connRecvMain = new ConnRecvMain();
                        connRecvMain.setId(mainId);
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
                    } catch (UncategorizedSQLException e) {
                        CommonUtil.SYSLOGGER.error(e.getMessage(), e);
                        // 触发器处理异常的消息,记录到接收历史表
                        ConnRecvMainHis connRecvMainHis = new ConnRecvMainHis();
                        connRecvMainHis.setTelId(telId);
                        connRecvMainHis.setId(mainId);
                        connRecvMainHis.setDealTime(new Date());
                        connRecvMainHis.setRecvTime(new Date());
                        connRecvMainHis.setMsgId(msgId);
                        connRecvMainHis.setDes(e.getMessage());
                        connRecvMainHis.setSender(Constant.SOCKET_SZ);
                        connRecvMainHis.setSenderName(getSysNameByCode(Constant.SOCKET_SZ));
                        connRecvMainHis.setReceiver(Constant.MES_CR);
                        connRecvMainHis.setReceiverName(getSysNameByCode(Constant.MES_CR));
                        connRecvMainHis.setDealFlag("9"); // 触发器处理异常
                        connRecvMainHisDao.insertSelective(connRecvMainHis);
                    }
                }
            }
        } else {
            LOGGER_WARN.warn("收到消息长度的长度不够!");
            // 更新统计表
            connTotalNumDao.updateTotalNum("RE");
        }
        offset = 0;
        msgBuf = null;
        headerBuf = null;
        headerOffset = 0;
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
