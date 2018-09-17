package com.asocket.manager.old;

import java.util.*;
import java.text.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qu SongTao@低调火药 on 2015/8/6.
 */
public class SocketConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketConfig.class);
    /**
     * Socket Server 是否可用
     */
    public static boolean SERVER_ENABLED = false;

    /**
     * 心跳连接测试是否正常
     */
    public static boolean LIFE_ENABLED = false;

    /**
     * 心跳频率
     */
    public static int LIFE_FREQUENCY = 300000;


    public static byte[] baseData(short msgType) {
        MsgHeader mh = new MsgHeader();
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
     * 心跳报文
     */
    public static byte[] lifeData() {
        return baseData((short) 0x8080);
    }

    /**
     * 正确应答
     */
    public static byte[] responseData() {
        return baseData((short) 0xE000);
    }

    /**
     * 处理服务端接收到的报文数据
     *
     * @param bytes 字节报文数据
     * @return
     */
    public static boolean dealData(byte[] bytes) {
        String msg;
        boolean ret = false;
        // 判断数据长度
        if (bytes.length < 20) {
            LOGGER.warn("报文长度不够!");
            return false;
        }
        try {
            MsgHeader mh = new MsgHeader();
            mh.setMsgType(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 0, 2)));
            mh.setDataLen(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 2, 2)));
            mh.setGroupIdDestination(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 4, 2)));
            mh.setMenberIdDestination(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 6, 2)));
            mh.setGroupIdSource(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 8, 2)));
            mh.setMenberIdSource(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 10, 2)));
            mh.setMsgTime(ByteTransfer.hBytesToInt(ByteTransfer.divByte(bytes, 12, 4)));
            mh.setSeqNo(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 16, 2)));
            mh.setReserved(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 18, 2)));

            //字节转字符串后去空格
            msg = new String(ByteTransfer.divByte(bytes, 20, bytes.length - 20));
            msg = msg.trim();
            if (msg.length() > 0) {
                LOGGER.info("接收报文:{}", msg);
                // 插入接收总表(触发器调用存储过程处理至业务表)
                /*DBCtr dbCtr = new DBCtr();
                dbCtr.setConnection();
                dbCtr.updateDBSql("INSERT INTO T_PCSAR_REV(SEQ_MSG,MSG_ID,TMSTP_CRT,PROC_FLAG,MSG_TEXT) VALUES(SEQ_PCSAR.NEXTVAL,'"+msg.substring(0,4)+"','"+ PropertiesUtil.getNowDate() +"','0','"+msg+"')");
                dbCtr.commit();
                dbCtr.closeConnection();*/
                ret = true;
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ret;
    }

    public static MsgHeader dealResponse(byte[] bytes) {
        String msg;
        MsgHeader mh = null;
        if (bytes.length < 20) {
            LOGGER.warn("报文长度不够!");
            return null;
        }
        try {
            mh = new MsgHeader();
            mh.setMsgType(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 0, 2)));
            mh.setDataLen(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 2, 2)));
            mh.setGroupIdDestination(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 4, 2)));
            mh.setMenberIdDestination(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 6, 2)));
            mh.setGroupIdSource(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 8, 2)));
            mh.setMenberIdSource(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 10, 2)));
            mh.setMsgTime(ByteTransfer.hBytesToInt(ByteTransfer.divByte(bytes, 12, 4)));
            mh.setSeqNo(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 16, 2)));
            mh.setReserved(ByteTransfer.hBytesToShort(ByteTransfer.divByte(bytes, 18, 2)));
            //System.out.println(getNowDate() + " :[response data] bytes.length="+bytes.length);
            //System.out.println(getNowDate() + " :[response data] header:" +mh.getMsgType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mh;
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public static String getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }

    /**
     * 获取现在时间
     *
     * @return返回短时间格式 yyyy-MM-dd
     */
    public static String getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(currentTime);
    }

}
