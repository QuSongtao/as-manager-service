/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.suncd.conn.netty.system.constants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Constant {

    private Constant(){

    }
    // -------------------------------- SOCKET CONSTANTS ------------------------------------
    // 消息序号(循环取)
    private static short SEQ_NO = 1;

    // 记录每个通道最后收到的心跳时间
    public static Map<Integer,Date> LAST_RECV_TIME= new HashMap<>();

    // 消息头长度
    public static final int HEAD_LEN = 20;

    // 心跳超时时间(ms) - 调整为70秒
    public static final int TIMEOUT_MS = 70000;

    // 获取循环序号
    public static short getSeqNo(){
        if(SEQ_NO > 30000){
            SEQ_NO = 1;
        }
        return ++SEQ_NO;
    }

    // 客户端和服务端状态 0-停止,1-运行
    public static int CLIENT_STATUS = 0;
    public static int SERVER_STATUS = 0;

    // 各SOCKET通信系统编码
    public static String SOCKET_SZ = "SZ";    // 酸轧
    public static String MES_CR = "CR";       // 冷轧1#MES

}
