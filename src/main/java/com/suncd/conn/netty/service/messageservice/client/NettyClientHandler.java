package com.suncd.conn.netty.service.messageservice.client;

import com.suncd.conn.netty.dao.ConnSendMainDao;
import com.suncd.conn.netty.dao.ConnSendMainHisDao;
import com.suncd.conn.netty.dao.ConnSendMsgDao;
import com.suncd.conn.netty.dao.ConnTotalNumDao;
import com.suncd.conn.netty.entity.ConnSendMain;
import com.suncd.conn.netty.entity.ConnSendMainHis;
import com.suncd.conn.netty.entity.ConnSendMsg;
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

import java.util.Date;
import java.util.UUID;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);
    private static final Logger LOGGER_WARN = LoggerFactory.getLogger("warnAndErrorLogger");
    private ConnSendMainDao connSendMainDao = SpringUtil.getBean(ConnSendMainDao.class);
    private ConnSendMsgDao connSendMsgDao = SpringUtil.getBean(ConnSendMsgDao.class);
    private ConnSendMainHisDao connSendMainHisDao = SpringUtil.getBean(ConnSendMainHisDao.class);
    private ConnTotalNumDao connTotalNumDao = SpringUtil.getBean(ConnTotalNumDao.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 启动业务消息发送
        Thread sender = new Thread(new MessageSender(ctx.channel()));
        sender.start();

        // 启动心跳消息发送
        Thread heartBeat = new Thread(new HeartBeatSender(ctx.channel()));
        heartBeat.start();
    }

    /**
     * 侦听远程服务器的应答消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 更新心跳时间
        Constant.LAST_RECV_TIME.put(ctx.channel().hashCode(), new Date());

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
        LOGGER_WARN.warn("【客户端】连接被远程服务器断开,通道编号:{}", ctx.channel().hashCode());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        // 记录错误信息
        LOGGER_WARN.error("【客户端】客户端出现异常,捕捉结果:", cause);

        // 关闭连接
        ctx.close();
    }

    private void processRecv(SzHeader szHeader) {
        LOGGER.info("【客户端】收到回执消息头:{}", szHeader.toString());

        // 1.获取消息sendTime和seqNo
        int pushTime = szHeader.getMsgTime();
        short seqNo = szHeader.getSeqNo();
        try {
            // 2.删除发送总表数据
            ConnSendMain connSendMain = connSendMainDao.selectByTimeAndSeq(pushTime, seqNo);
            connSendMainDao.deleteByPrimaryKey(connSendMain.getId());

            // 3.插入发送历史表
            ConnSendMainHis connSendMainHis = new ConnSendMainHis();
            connSendMainHis.setId(UUID.randomUUID().toString());
            connSendMainHis.setCreateTime(connSendMain.getCreateTime());
            connSendMainHis.setMsgId(connSendMain.getMsgId());
            connSendMainHis.setPushLongTime(pushTime);
            connSendMainHis.setPushSeqNo((int) seqNo);
            connSendMainHis.setSendFlag("1");
            connSendMainHis.setSendResult("发送成功!");
            connSendMainHis.setTelId(connSendMain.getTelId());
            connSendMainHis.setSender(connSendMain.getSender());
            connSendMainHis.setSenderName(connSendMain.getSenderName());
            connSendMainHis.setReceiver(connSendMain.getReceiver());
            connSendMainHis.setReceiverName(connSendMain.getReceiverName());
            connSendMainHis.setSendTime(new Date());
            connSendMainHisDao.insertSelective(connSendMainHis);

            // 4.更新统计表
            connTotalNumDao.updateTotalNum("SS");

            // 5.记录发送日志
            ConnSendMsg connSendMsg = connSendMsgDao.selectByPrimaryKey(connSendMain.getMsgId());
            LOGGER.info(connSendMsg.getMsgTxt());

        } catch (Exception e) {
            CommonUtil.SYSLOGGER.error(e.getMessage(), e);
        }
    }
}
