package com.asocket.manager.vo;

import com.asocket.manager.util.ByteUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SzHeader {
    private short msgType;             // 消息类型: 2个字节,正常请求-0x8000 正常响应-0xE000 心跳请求-0x8080
    private short dataLen;             // 数据包字节数: 2个字节,不包括消息头长度
    private short groupIdDestination;  // 目的组ID  默认:10
    private short menberIdDestination; // 目的成员ID  默认:11
    private short groupIdSource;       // 源组ID  默认:20
    private short menberIdSource;      // 源成员ID  默认:21
    private short seqNo;               // 序号,循环取值
    private short reserved;            // 预留2个字节
    private int msgTime;               // 时间戳 1970-01-01至今的秒数(s)

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }

    public short getDataLen() {
        return dataLen;
    }

    public void setDataLen(short dataLen) {
        this.dataLen = dataLen;
    }

    public short getGroupIdDestination() {
        return groupIdDestination;
    }

    public void setGroupIdDestination(short groupIdDestination) {
        this.groupIdDestination = groupIdDestination;
    }

    public short getMenberIdDestination() {
        return menberIdDestination;
    }

    public void setMenberIdDestination(short menberIdDestination) {
        this.menberIdDestination = menberIdDestination;
    }

    public short getGroupIdSource() {
        return groupIdSource;
    }

    public void setGroupIdSource(short groupIdSource) {
        this.groupIdSource = groupIdSource;
    }

    public short getMenberIdSource() {
        return menberIdSource;
    }

    public void setMenberIdSource(short menberIdSource) {
        this.menberIdSource = menberIdSource;
    }

    public short getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(short seqNo) {
        this.seqNo = seqNo;
    }

    public short getReserved() {
        return reserved;
    }

    public void setReserved(short reserved) {
        this.reserved = reserved;
    }

    public int getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(int msgTime) {
        this.msgTime = msgTime;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date();
        long lTime = msgTime * 1000L;
        dt.setTime(lTime);
        return "{" +
                "消息类型=" + msgType +
                ", 消息长度=" + dataLen +
                ", 接收组ID=" + groupIdDestination +
                ", 接收者ID=" + menberIdDestination +
                ", 发送组ID=" + groupIdSource +
                ", 发送者ID=" + menberIdSource +
                ", 消息序号=" + seqNo +
                ", 预留=" + reserved +
                ", 时间=" + sdf.format(dt) +
                '}';
    }

    public byte[] toByte(){
        byte[][] b = new byte[9][];
        b[0] = ByteUtils.toHH(msgType);
        b[1] = ByteUtils.toHH(dataLen);
        b[2] = ByteUtils.toHH(groupIdDestination);
        b[3] = ByteUtils.toHH(menberIdDestination);
        b[4] = ByteUtils.toHH(groupIdSource);
        b[5] = ByteUtils.toHH(menberIdSource);
        b[6] = ByteUtils.toHH(msgTime);
        b[7] = ByteUtils.toHH(seqNo);
        b[8] = ByteUtils.toHH(reserved);
        return ByteUtils.bbToBytes(b,20);
    }
}
