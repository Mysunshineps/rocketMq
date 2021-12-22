package com.psq.code.jms;

import com.psq.code.config.RocketMqConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 消费者
 * @Author psq
 * @Date 2021/10/26 16:44
 */
@Component
public class PayConsumer {

    private DefaultMQPushConsumer consumer;

    public PayConsumer() throws MQClientException {
        /**
         * enableMsgTrace：订阅消息时是否开启消息轨迹
         */
        consumer = new DefaultMQPushConsumer(RocketMqConfig.CONSUMER_GROUP,true);
        consumer.setNamesrvAddr(RocketMqConfig.NAME_SERVER_ADDR);
        //CONSUME_FROM_LAST_OFFSET:默认策略，初次从该队列最尾开始消费，即跳过历史消息，后续再启动接着上次消费的进度开始消费
        //CONSUME_FROM_FIRST_OFFSET:初次从消息队列头部开始消费，即历史消息（还储存在broker的）全部消费一遍，后续再启动接着上次消费的进度开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(RocketMqConfig.PAY_TOPIC, "*");
        //消费模式，默认为CLUSTERING集群方式，也可指定为BROADCASTING广播模式，
        //但消费重试只针对集群消费方式生效；广播方式不提供失败重试特性，
        //即消费失败后，失败消息不再重试，继续消费新的消息
//        consumer.setMessageModel(MessageModel.BROADCASTING);
        //最大的重试次数：0-16次，如果超过设置的最大重试次数，该消息会被指向一个等待删除的队列中
        // consumer.setMaxReconsumeTimes(3);

        //TODO 一条消息无论被重试几次，其中Message ID 和keys是不变的
        //TODO 消费 记得在消费逻辑里去重，可以将对应的信息加入数据库，以Message ID 和keys为唯一标识
        //MessageListenerConcurrentl：这里可以并发的消费
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                int times = msg.getReconsumeTimes();
                System.out.println("重试次数：" + times);
                try {
                    System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), new String(msgs.get(0).getBody()));

                    String topic = msg.getTopic();
                    String body = new String(msg.getBody(), "utf-8");
                    String tags = msg.getTags();
                    String keys = msg.getKeys();

                    /**
                     * 以下进行模拟消费失败，进行消费重试
                     */
//                    if (StringUtils.isNotBlank(keys) && keys.equalsIgnoreCase("17778386756")){
//                        throw new Exception();
//                    }
                    System.out.println("topic=" + topic + ", tags=" + tags + ", keys=" + keys + ", msg=" + body);
                    //业务逻辑处理，处理成功后记录消息在数据库，消息去重
                    //表示消费成功，会删除队列的消息
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    System.out.println("消费异常");

                    //如果重试2次不成功，则记录数据库后，使其消费成功返回broker，但需人工介入处理
                    if (times >= 2){
                        System.out.println("重试次数大于2，记录数据库，发送短信给开发人员或运营人员，来人工处理");
                        //TODO 记录数据库，发送短信给开发人员或运营人员，来人工处理
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    e.printStackTrace();
                    //表示消费失败，后面会重复消费
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        consumer.start();
        System.out.println("PayConsumer start ...");
    }

}
