package com.psq.code.jms;

import com.psq.code.config.RocketMqConfig;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * @Description 生产者
 * @Author psq
 * @Date 2021/10/26 16:44
 */
@Component
public class PayProducer {

    private DefaultMQProducer producer;

    public PayProducer(){
        /**
         * enableMsgTrace：发送消息时是否开启消息轨迹
         */
        producer = new DefaultMQProducer(RocketMqConfig.PRODUCER_GROUP,true);
        /**
         * 指定NameServer地址，多个地址以 ; 隔开
         * 如 producer.setNamesrvAddr("192.168.100.141:9876;192.168.100.142:9876;192.168.100.149:9876");
         */
        producer.setNamesrvAddr(RocketMqConfig.NAME_SERVER_ADDR);

        /**
         * 同步投递时，生产者投递到broker失败时的重试次数，默认为2次，可配置setRetryTimesWhenSendFailed参数设定指定的重试次数
         */
        producer.setRetryTimesWhenSendFailed(3);
        start();
    }

    public DefaultMQProducer getProducer(){
        return this.producer;
    }

    /**
     * 对象在使用之前必须要调用一次，只能初始化一次
     */
    public void start(){
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * 一般在应用上下文，使用上下文监听器，进行关闭
     */
    public void shutdown(){
        this.producer.shutdown();
    }
}
