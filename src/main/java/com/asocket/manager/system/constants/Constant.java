/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.system.constants;

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

    // 心跳超时时间(ms)
    public static final int TIMEOUT_MS = 30000;

    // 获取循环序号
    public static short getSeqNo(){
        if(SEQ_NO > 30000){
            SEQ_NO = 1;
        }
        return ++SEQ_NO;
    }

    // ------------------------------------ MQ CONSTANTS ------------------------------------

}
