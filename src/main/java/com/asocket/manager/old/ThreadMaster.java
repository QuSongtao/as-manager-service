package com.asocket.manager.old;


import java.io.*;
import java.net.ServerSocket;
import java.util.Date;
/**
 * Created by Administrator on 2015/8/18.
 */
public class ThreadMaster extends Thread {

public void run(){
	while(true){
		//System.out.println("last recv time long = " + SocketBus.lTime);
		if( new Date().getTime() - SocketBus.lTime > 70*1000 ){
			System.out.println(new Date() + "life is dead !");
			SocketClient.RECONNECT = true;
  	}else{
  		SocketClient.RECONNECT = false;
  	}
  	try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
	}
}

	public static void main(String[] args) throws IOException {
		//start client
		SocketClient socketClient = new SocketClient();
    socketClient.start();
    
    //start check
		ThreadMaster tm = new ThreadMaster();
		tm.start();

		//start server
    int port = Integer.parseInt(PropertiesUtil.getValue("socket.properties","server.local.port"));
    ServerSocket server = new ServerSocket(port);
    System.out.println("socket server have started,port:"+port+" ready to accept client!");
    SocketBus obj = null;
    while (true) {
        SocketBus mu = new SocketBus(server.accept());
        mu.start();
        if(null == obj){
            obj = mu;
        }else {
            obj.disconnect();
            obj = mu;
        }
    }
  }
}