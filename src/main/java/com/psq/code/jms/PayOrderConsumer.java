package com.psq.code.jms;

import com.psq.code.config.RocketMqConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 消费者--顺序消息消费
 * @Author psq
 * @Date 2021/10/26 16:44
 */
@Component
public class PayOrderConsumer {

    private DefaultMQPushConsumer consumer;

    public PayOrderConsumer() throws MQClientException {
        /**
         * enableMsgTrace：订阅消息时是否开启消息轨迹
         */
        consumer = new DefaultMQPushConsumer(RocketMqConfig.ORDER_CONSUMER_GROUP,true);
        consumer.setNamesrvAddr(RocketMqConfig.NAME_SERVER_ADDR);
        consumer.setVipChannelEnabled(false);
        //CONSUME_FROM_LAST_OFFSET:默认策略，初次从该队列最尾开始消费，即跳过历史消息，后续再启动接着上次消费的进度开始消费
        //CONSUME_FROM_FIRST_OFFSET:初次从消息队列头部开始消费，即历史消息（还储存在broker的）全部消费一遍，后续再启动接着上次消费的进度开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //subscribe(A,B)前者参数订阅话题；后者更细致的划分，对消息发送做过滤，不指定可以写*，也可以订阅指定该话题下的哪个tag,多个tag可以用|连接
        consumer.subscribe(RocketMqConfig.ORDER_TOPIC, "orderTagA || orderTagB");
        //消费模式，默认为CLUSTERING集群方式，也可指定为BROADCASTING广播模式，
        //但消费重试只针对集群消费方式生效；广播方式不提供失败重试特性，
        //即消费失败后，失败消息不再重试，继续消费新的消息
//        consumer.setMessageModel(MessageModel.BROADCASTING);


        //TODO 一条消息无论被重试几次，其中Message ID 和keys是不变的
        //TODO 消费 记得在消费逻辑里去重，可以将对应的信息加入数据库，以Message ID 和keys为唯一标识
        //MessageListenerOrderly：顺序的消费
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                MessageExt msg = msgs.get(0);
                //重试次数
                int times = msg.getReconsumeTimes();
                try {
                    System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), new String(msgs.get(0).getBody()));
                    //业务逻辑处理，处理成功后记录消息在数据库，消息去重
                    //表示消费成功，会删除队列的消息
                    return ConsumeOrderlyStatus.SUCCESS;
                } catch (Exception e) {
                    System.out.println("消费异常");
                    //过一会再发送消息重试
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
        });
        consumer.start();
        System.out.println("OrderConsumer start ...");
    }

}
