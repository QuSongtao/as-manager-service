/*
成都太阳高科技有限责任公司
http://www.suncd.com
*/
package com.asocket.manager.system.configuration;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IbmMqJmsConfiguration {
    /*@Bean
    public MQQueueConnectionFactory mqQueueConnectionFactory() {
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();

        try {
//            mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            mqQueueConnectionFactory.setCCSID(1208);
//            mqQueueConnectionFactory.setChannel(channel);
//            mqQueueConnectionFactory.setPort(port);
//            mqQueueConnectionFactory.setQueueManager(queueManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mqQueueConnectionFactory;
    }*/
//    @Bean(name = "UserCredentialsConnectionFactoryAdapter1")
//    public UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter() {
//        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
//        userCredentialsConnectionFactoryAdapter.setUsername("MUSR_MQADMIN");
//        userCredentialsConnectionFactoryAdapter.setPassword("123456");
//        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqConnectionFactory);
//        return new UserCredentialsConnectionFactoryAdapter();
//    }

    /*@Bean
    @Primary
    public CachingConnectionFactory cachingConnectionFactory(MQConnectionFactory mqConnectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(mqConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(CachingConnectionFactory cachingConnectionFactory){
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
        return jmsTransactionManager;
    }*/

    @Bean
    public JmsOperations jmsOperations(MQConnectionFactory cachingConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
        jmsTemplate.setReceiveTimeout(20000);
        return jmsTemplate;
    }
}
