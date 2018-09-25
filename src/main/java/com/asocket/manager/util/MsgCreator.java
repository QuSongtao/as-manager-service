package com.asocket.manager.util;

import com.asocket.manager.system.Const;
import com.asocket.manager.vo.SzHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MsgCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgCreator.class);

    private MsgCreator(){

    }
    /**
     * 基础响应数据
     */
    private static byte[] baseData(short msgType) {
        SzHeader mh = new SzHeader();
        mh.setMsgType(msgType);
        mh.setDataLen((short) 0x0000);
        mh.setGroupIdDestination((short) 10);
        mh.setMenberIdDestination((short) 11);
        mh.setGroupIdSource((short) 20);
        mh.setMenberIdSource((short) 21);
        mh.setMsgTime((int) (new Date().getTime() / 1000));
        mh.setSeqNo((short) 1);
        mh.setReserved((short) 0x0000);
        return mh.toByte();
    }
    /**
     * 创建心跳消息
     */
    public static byte[] createHeartBeatData() {
        return baseData((short) 0x8080);
    }

    /**
     * 创建业务请求数据
     */
    public static byte[] createAppData(String busiData, short seqNo) {
        short dataLen = (short) busiData.getBytes().length;
        LOGGER.info("数据长度:{}",dataLen );
        byte[][] bytes = new byte[2][];
        SzHeader mh = new SzHeader();
        mh.setMsgType((short) 0x8000);
        mh.setDataLen(dataLen);
        mh.setGroupIdDestination((short) 10);
        mh.setMenberIdDestination((short) 11);
        mh.setGroupIdSource((short) 20);
        mh.setMenberIdSource((short) 21);
        mh.setMsgTime((int) (new Date().getTime() / 1000));
        mh.setSeqNo(seqNo);
        mh.setReserved((short) 0x0000);
        bytes[0] = mh.toByte();
        bytes[1] = busiData.getBytes();
        return ByteUtils.bbToBytes(bytes, Const.HEAD_LEN + dataLen);
    }

    /**
     * 根据请求消息创建应答消息
     */
    public static SzHeader createAckHeader(byte[] bytes) {
        SzHeader mh = null;
        try {
            mh = new SzHeader();
            mh.setMsgType((short) 0xE000); // 正常应答
            mh.setDataLen((short) 0x0000); // 应用数据包长度0
            mh.setGroupIdDestination(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 4, 2)));
            mh.setMenberIdDestination(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 6, 2)));
            mh.setGroupIdSource(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 8, 2)));
            mh.setMenberIdSource(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 10, 2)));
            mh.setMsgTime(ByteUtils.hBytesToInt(ByteUtils.subBytes(bytes, 12, 4)));
            mh.setSeqNo(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 16, 2)));
            mh.setReserved(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 18, 2)));
        } catch (Exception e) {
            LOGGER.error("生成响应数据头出现异常",e);
        }
        return mh;
    }

    /**
     * 获取请求头
     */
    public static SzHeader createRecvHeader(byte[] bytes) {
        SzHeader mh = null;
        try {
            mh = new SzHeader();
            mh.setMsgType(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 0, 2)));
            mh.setDataLen(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 2, 2)));
            mh.setGroupIdDestination(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 4, 2)));
            mh.setMenberIdDestination(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 6, 2)));
            mh.setGroupIdSource(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 8, 2)));
            mh.setMenberIdSource(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 10, 2)));
            mh.setMsgTime(ByteUtils.hBytesToInt(ByteUtils.subBytes(bytes, 12, 4)));
            mh.setSeqNo(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 16, 2)));
            mh.setReserved(ByteUtils.hBytesToShort(ByteUtils.subBytes(bytes, 18, 2)));
        } catch (Exception e) {
            LOGGER.error("从接收电文中获取数据头出现异常",e);
        }
        return mh;
    }
}
