package com.asocket.manager.old;

/**
 * Socket通信消息头
 */
public class MsgHeader {
    private short msgType; // 消息类型: 2个字节,固定为0X8000
    private short dataLen; // 数据包字节数: 2个字节,包括消息头长度
    private short groupIdDestination; // 目的组ID
    private short menberIdDestination; // 目的成员ID
    private short groupIdSource; // 源组ID
    private short menberIdSource; // 源成员ID
    private short seqNo; // 序号
    private short reserved; // 预留2个字节
    private int msgTime; // 时间戳


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

    public byte[] toByte(){
        byte[][] b = new byte[9][];
        b[0] = ByteTransfer.toHH(msgType);
        b[1] = ByteTransfer.toHH(dataLen);
        b[2] = ByteTransfer.toHH(groupIdDestination);
        b[3] = ByteTransfer.toHH(menberIdDestination);
        b[4] = ByteTransfer.toHH(groupIdSource);
        b[5] = ByteTransfer.toHH(menberIdSource);
        b[6] = ByteTransfer.toHH(msgTime);
        b[7] = ByteTransfer.toHH(seqNo);
        b[8] = ByteTransfer.toHH(reserved);
        return ByteTransfer.bytesToB(b,20);
    }

    public String toString() {
        return "MsgHeader{" +
                "msgType=" + msgType +
                ", dataLen=" + dataLen +
                ", groupIdDestination=" + groupIdDestination +
                ", menberIdDestination=" + menberIdDestination +
                ", groupIdSource=" + groupIdSource +
                ", menberIdSource=" + menberIdSource +
                ", seqNo=" + seqNo +
                ", reserved=" + reserved +
                ", msgTime=" + msgTime +
                '}';
    }
}
