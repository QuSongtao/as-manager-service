/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.service.mq.sender;

public interface MessageSendService {
    void sendMessage(String destinationName,String message);
}
