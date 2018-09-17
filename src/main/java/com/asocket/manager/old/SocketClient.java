package com.asocket.manager.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import java.util.Date;

/**
 * Created by Administrator on 2015/8/4.
 */
public class SocketClient extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class);

    PrintStream out;
    BufferedReader in;
    InputStreamReader inR;
    InputStream inputStream;
    Socket clientSocket;
    String address;
    int port;
    public static boolean RECONNECT = false;


    public SocketClient() {
        initClient();
    }

    /**
     * shutdownOutput()
     * Disables the output stream for this socket.
     * For a TCP socket, any previously written data will be sent
     * followed by TCP's normal connection termination sequence.
     * If you write to a socket output stream after invoking shutdownOutput() on the socket,
     * the stream will throw an IOException.
     *
     * @param request
     */
    public synchronized void sendRequest(String request) {
        if (null == getOut()) return;
        try {
            getOut().write((request + "\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client send: " + request);
//        try {
//            getClientSocket().shutdownOutput();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        getResponse();
    }

    public MsgHeader getResponse() {
        String str = null;
        MsgHeader mh = null;
        try {
            //TODO -- 需要测试下效率
//            int tempchar;
//            StringBuilder sb = new StringBuilder();
//            while ((tempchar = getInR().read()) != -1){
//                if (((char)tempchar) != '\r'){
//                    sb.append((char)tempchar);
//                }else {
//                    System.out.println("新记录:"+sb.toString());
//                }
//            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tempBytes = new byte[20 + 4096];
            int byteRead;
//            getInputStream().
            while ((byteRead = getInputStream().read(tempBytes)) != -1) {
                baos.write(tempBytes, 0, byteRead);
                mh = SocketConfig.dealResponse(baos.toByteArray());
                baos.reset();
                break;
            }
        } catch (IOException e) {
            //(1)服务器端不可用,服务器端中途停止
            System.out.println(SocketConfig.getNowDate() + "server is avialbe!");
            reconnect();
        }
        return mh;
    }

    public String getResponse0() {
        String str = null;
        try {
            //TODO -- 需要测试下效率
//            int tempchar;
//            StringBuilder sb = new StringBuilder();
//            while ((tempchar = getInR().read()) != -1){
//                if (((char)tempchar) != '\r'){
//                    sb.append((char)tempchar);
//                }else {
//                    System.out.println("新记录:"+sb.toString());
//                }
//            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tempBytes = new byte[20 + 4096];
            int byteRead = 0;
            if (null == getInputStream()) {
                System.out.println("InputStream is not avialbe!");
                return null;
            }
            System.out.println("8----->");
            while ((byteRead = getInputStream().read(tempBytes)) != -1) {
                System.out.println("9");
                System.out.println("byteread=" + byteRead);
                baos.write(tempBytes, 0, byteRead);
                SocketConfig.dealData(baos.toByteArray());
                baos.reset();
                break;
            }
            System.out.println("10 end<-----");
            str = new String(baos.toByteArray());
        } catch (IOException e) {

            //(1)服务器端不可用,服务器端中途停止
            System.out.println(SocketConfig.getNowDate() + "server is avialbe!");
            reconnect();
        }
        return str;
    }

    public void initClient() {
        LOGGER.info("客户端初始化...");
        setAddress(PropertiesUtil.getValue("socket.properties", "server.remote.ip"));
        setPort(Integer.parseInt(PropertiesUtil.getValue("socket.properties", "server.remote.port")));
        try {
            System.out.println(getAddress() + ":" + getPort());
            setClientSocket(new Socket(getAddress(), getPort()));
            setOut(new PrintStream(getClientSocket().getOutputStream()));
            setInputStream(getClientSocket().getInputStream());
            RECONNECT = false;
        } catch (Exception e) {
            //(1)此处出异常,则首次启动是未能连接上服务器
            //System.out.println("can not connect to server!");
            RECONNECT = true;
        } finally {

        }
    }

    public void closeClient() {
        try {
            getOut().close();
            getInputStream().close();
            getClientSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        closeClient();
        initClient();
    }

    public byte[] businessData(String busiData) {
        short len = (short) busiData.getBytes().length;
        byte[][] bytes = new byte[2][];
        MsgHeader mh = new MsgHeader();
        mh.setMsgType((short) 0x8000);
        mh.setDataLen(len);
        mh.setGroupIdDestination((short) 10);
        mh.setMenberIdDestination((short) 11);
        mh.setGroupIdSource((short) 20);
        mh.setMenberIdSource((short) 21);
        mh.setMsgTime((int) (new Date().getTime() / 1000));
        mh.setSeqNo((short) 1);
        mh.setReserved((short) 0x0000);
        bytes[0] = mh.toByte();
        bytes[1] = busiData.getBytes();
        return ByteTransfer.bytesToB(bytes, 20 + len);
    }

    public void run() {
        int i = 0;
        String sFlag = "", strText = "";
        while (true) {
            if (null == getOut() || RECONNECT) {
                try {
                    System.out.println("will init Client !");
                    reconnect();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            try {

                if (i % 6 == 0) {
                    //System.out.println(SocketConfig.getNowDate()+ " :[SEND LIFE MSG!] " + m + " times");
                    getOut().write(SocketConfig.lifeData());
                    getOut().flush();
            			/*try {
			                getClientSocket().sendUrgentData(0x0000);
			            }catch (Exception ec){
			                System.out.println("[x] server is not available ! " + ec.getMessage());
			                 Logger.log("[X] server is not available! ", LogWriter.RE);
			                reconnect();
			                try {
					                Thread.sleep(5000);
					            } catch (InterruptedException e) {
					                e.printStackTrace();
					            } 
					            i++;
			                continue;
			            }
			            m++;
			            */
                }
                if (i == 20000) i = 0;

                /*DBCtr dbCtr = new DBCtr();
                dbCtr.setConnection();
                String[][] strMain = dbCtr.getSendMain();*/
                // 查询发送总表待发送记录
                String[][] strMain = new String[2][];
                if (null != strMain) {
                    for (int j = 0; j < strMain.length; j++) {
                        /*String[][] strMsg = dbCtr.getSendMainMsg(strMain[j][3]);*/
                        // 根据序号取待发送消息
                        String[][] strMsg = new String[2][];
                        if (null == strMsg) {
                            sFlag = "0";
                            strText = "在发送中间表中找不到SEQ_MSG=" + strMain[j][3] + "对应的记录";
                        } else {
                            getOut().write(businessData(strMsg[0][4]));
                            sFlag = "1";
                            strText = strMsg[0][4];
                        }
                        MsgHeader mh = getResponse();
                        if (mh.getMsgType() == (short) 0xE000) {
                            // 1.删除发送总表对应记录
                            /*dbCtr.deleteMain(strMain[j][0]);*/
                            // 2.插入发送历史表
	                        /*String sqlStr="INSERT INTO T_PCSAR_SND_MAIN_BCK(SEQ_NUM,TBL_NAME,MSG_ID,SEQ_MSG,PROC_FLAG,TMSTP_CRT,TMSTP_SND) VALUES("+strMain[j][0]
	                        +",'"+strMain[j][1]
	                        +"','"+strMain[j][2]
	                        +"',"+strMain[j][3]
	                        +",'"+ sFlag +"','"+strMain[j][5]+"',TO_CHAR(SYSDATE,'YYYYMMDDHHMISS'))";
	                        dbCtr.updateDBSql(sqlStr);*/
                            // 3.记录发送日志
                            LOGGER.info("发送报文:{}", strText);
                        }
                    }
                }
            } catch (Exception e) {
                //System.out.println("Socket Exception!");
                //e.printStackTrace();
                RECONNECT = true;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public static void main(String[] strings) {
        SocketClient socketClient = new SocketClient();
        socketClient.start();
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public InputStreamReader getInR() {
        return inR;
    }

    public void setInR(InputStreamReader inR) {
        this.inR = inR;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
