package com.asocket.manager.vo;

/**
 * 消息头定义
 *
 */
public class DxHeader {
    private int msgLen;      // 消息长度
    private int msgId;       // 消息ID
    private int sequenceNum; // 发送序列号

    public int getMsgLen() {
        return msgLen;
    }

    public void setMsgLen(int msgLen) {
        this.msgLen = msgLen;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }
}
