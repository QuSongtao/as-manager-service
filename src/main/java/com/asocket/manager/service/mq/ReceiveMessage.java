/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.service.mq;

import com.ibm.jms.JMSBytesMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.annotation.JmsListeners;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@Component
public class ReceiveMessage extends MessageListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveMessage.class);

    @Override
    @JmsListeners(value = {@JmsListener(destination = "${ibm.queue.q1}"),@JmsListener(destination = "Q2")})
    public void onMessage(Message message) {
        String recvStrMsg = "";
        if (message instanceof JMSBytesMessage) {
            LOGGER.info("字节类型的消息");
            JMSBytesMessage bm = (JMSBytesMessage) message;
            byte[] bys;
            try {
                bys = new byte[(int) bm.getBodyLength()];
                bm.readBytes(bys);
                recvStrMsg = new String(bys);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.info("文本类型的消息");
            TextMessage bm = (TextMessage) message;
            try {
                recvStrMsg = bm.getText();
            } catch (JMSException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("收到消息:{}", recvStrMsg);
    }

    @Override
    protected void handleListenerException(Throwable e) {
        LOGGER.error(e.getMessage(), e);
    }
}
