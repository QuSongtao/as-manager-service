package com.asocket.manager.vo;

public class SzHeader {
    private short msgType;             // 消息类型: 2个字节,正常请求-0x8000 正常响应-0xE000 心跳请求-0x8080
    private short dataLen;             // 数据包字节数: 2个字节,包括消息头长度
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
}
