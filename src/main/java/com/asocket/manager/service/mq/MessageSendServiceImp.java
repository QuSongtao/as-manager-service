/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.service.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MessageSendServiceImp implements MessageSendService {

    @Autowired
    private JmsOperations jmsOperations;

    @Override
    public void sendMessage(String destinationName, String message) {
        // 转为字节发送
        byte[] msgBuf = message.getBytes();
        jmsOperations.convertAndSend(destinationName, msgBuf);
    }

    @PostConstruct
    public void testSend() {
        sendMessage("Q1", "cgx中文123");
    }
}
