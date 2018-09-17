package com.asocket.manager.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Qu SongTao@低调火药 on 2015/8/4.
 */
public class SocketBus extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketBus.class);

    private Socket client;
    private String clientIP;
    private boolean running = true;
    public static long lTime = new Date().getTime();

    public SocketBus(Socket socket) {
        setClient(socket);
    }

    /**
     * 单例synchronized,关闭Socket客户端连接
     */
    public synchronized void disconnect() {
        LOGGER.info("[X]Client: {} is disconnect!", getClientIP());
        toStop();
        try {
            getClient().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //SocketClient.RECONNECT = true;
    }

    public void toStop() {
        setRunning(false);
    }

    public void run() {
        InputStream inputStream = null;
        PrintStream out = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean ret;
        try {
            inputStream = getClient().getInputStream();
//          PrintWriter out = new PrintWriter(getClient().getOutputStream());
            out = new PrintStream(getClient().getOutputStream());
            System.out.println(SocketConfig.getNowDate() + " Client: " + getClient().getInetAddress() + " have connected!");
            LOGGER.info("[OK]Client: {} have connected!", getClient().getInetAddress());

            setClientIP(getClient().getInetAddress().toString());
            while (isRunning()) {
                byte[] tempBytes = new byte[20 + 4096];
                int byteRead;
                while ((byteRead = inputStream.read(tempBytes)) != -1) {
                    //设置最后接收时间
                    lTime = new Date().getTime();
                    baos.write(tempBytes, 0, byteRead);
                    ret = SocketConfig.dealData(baos.toByteArray());
                    if (ret) {
                        out.write(SocketConfig.responseData());
                        out.flush();
                    }
                    baos.reset();
                }
            }
            try {
                if (null != inputStream) inputStream.close();
                if (null != out) out.close();
                if (null != baos) baos.close();
            } catch (Exception e) {

            }

        } catch (Exception ex) {
            System.out.println(new Date() + ":程序异常!!");

            ex.printStackTrace();
            disconnect();
            try {
                if (null != inputStream) inputStream.close();
                if (null != out) out.close();
                if (null != baos) baos.close();
            } catch (Exception e) {

            }
        } finally {

        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(PropertiesUtil.getValue("socket.properties", "server.local.port"));
        ServerSocket server = new ServerSocket(port);
        System.out.println("socket server have started,port:" + port + " ready to accept client!");
        Object obj = null;
        while (true) {
            SocketBus mu = new SocketBus(server.accept());
            mu.start();
            if (null == obj) {
                obj = mu;
            } else {
                SocketBus socketBus = (SocketBus) obj;
                socketBus.disconnect();
                obj = mu;
            }
        }
    }


    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
