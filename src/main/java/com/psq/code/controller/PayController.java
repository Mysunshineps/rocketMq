package com.psq.code.controller;

import com.psq.code.config.RocketMqConfig;
import com.psq.code.jms.PayProducer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description 控制层
 * @Author psq
 * @Date 2021/10/26 16:44
 */
@RestController
public class PayController {

    @Autowired
    private PayProducer payProducer;

    /**
     * rocketmq采用同步双写、异步刷盘策略：broker主从间数据同步双写，内存写到磁盘里异步
     * @param text
     * @return
     */
    @RequestMapping("/api/v1/pay_cb")
    public Object callback(String text) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {

        /**
         * 指定消息keys，是唯一的作为标识
         */
        Message message = new Message(RocketMqConfig.TOPIC,"tagA", "17778386756", ("hello rocketmq = "+text).getBytes() );

        /**
         * 该参数setDelayTimeLevel(xxx)设置延迟消息：
         * 目前有以下级别"1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"；
         * xxx对应第几个，比如消息发送到broker后,消费者要延迟10s消费，则设为setDelayTimeLevel(3)即可
         * 使用场景：
         * a.通过消息触发一些定时任务，比如生日送礼物，前一天23点执行定时器，将消息发送给broker,延迟1h后凌晨0点发送生日短信
         * b.消息生产和消费有时间窗口要求：比如在电商交易中超时未支付关闭订单的场景，在订单创建时会发送一条延时消息；
         * 这条消息将会在 30 分钟以后投递给消费者，消费者收到此消息后需要判断对应的订单是否已完成支付。如支付未完成，则关闭订单。如已完成支付则忽略
         */
        message.setDelayTimeLevel(3);

        SendResult sendResult = null;

        //todo:消息发送方式三种，以下对比
        /**
         * todo:1.同步方式发送消息，有返回值，消息不丢失
         * 应用场景：重要通知邮件、报名短信通知、营销短信系统等
         */
        sendResult = payProducer.getProducer().send(message);

        // /**
        //  * todo:2.异常方式发送消息，有返回值，消息不丢失
        //  * 应用场景：对时间敏感,可以支持更高的并发，回调成功触发相对应的业务，比如注册成功后通知积分系统发放优惠券
        //  */
        // payProducer.getProducer().send(message, new SendCallback() {
        //     //消息异步发送后消费成功返回
        //     @Override
        //     public void onSuccess(SendResult sendResult) {
        //         System.out.printf("返回结果状态=%s，msg=%s", sendResult.getSendStatus(), sendResult);
        //     }
        //     //消息异步发送后消费失败异常返回
        //     @Override
        //     public void onException(Throwable e) {
        //         System.out.printf("返回结果状态失败，异常信息=%s", e);
        //         e.printStackTrace();
        //         //补偿机制，根据业务情况进行使用，看是否重试
        //     }
        // });
        //
        // /**
        //  * todo:3.oneway方式发送消息，没有返回值，消息可能丢失
        //  * 使用场景：主要是日志收集，适用于某些耗时非常短，但对可靠性要求并不高的场景, 也就是LogServer, 只负责发送消息，不等待服务器回应且没有回调函数触发，即只发送请求不等待应答
        //  */
        // payProducer.getProducer().sendOneway(message);
        //
        // /**
        //  * todo:生产消息使用MessageQueueSelector投递到Topic下指定的queue
        //  * 应用场景：顺序消息，分摊负载
        //  * 注意：
        //  * 1.支持同步，异步发送指定的MessageQueue
        //  * 2.选择的queue数量(也就是下面的arg参数)必须小于配置的，否则会出错：越界报错
        //  */
        // //todo：1.同步方式的MessageQueueSelector投递
        // sendResult = payProducer.getProducer().send(message, new MessageQueueSelector() {
        //     @Override
        //     public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        //         //这里的arg就是send(message,new MessageQueueSelector(){}, arg)中第三个传入的参数
        //         Integer queueNum = 0;
        //         if (null != arg) {
        //             Integer argNum = Integer.parseInt(arg.toString());
        //             //判断参数是否在队列范围内
        //             if (argNum > 0 && mqs.size() > argNum) {
        //                 queueNum = argNum;
        //             }
        //         }
        //         //mqs代表的是配置的队列集合，默认队列长度为4，指针从0，1，2，3开始，所以传入的参数不能大于3，否则会报越界异常
        //         //消息投递到Topic下指定的queue
        //         return mqs.get(queueNum);
        //     }
        // }, 0);
        //
        // //todo：2.异步方式的MessageQueueSelector投递，没有返回值
        // payProducer.getProducer().send(message, new MessageQueueSelector() {
        //     @Override
        //     public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        //         //这里的arg就是send(message,new MessageQueueSelector(){}, arg)中第三个传入的参数
        //         Integer queueNum = 0;
        //         if (null != arg) {
        //             Integer argNum = Integer.parseInt(arg.toString());
        //             //判断参数是否在队列范围内
        //             if (argNum > 0 && mqs.size() > argNum) {
        //                 queueNum = argNum;
        //             }
        //         }
        //         //mqs代表的是配置的队列集合，默认队列长度为4，指针从0，1，2，3开始，所以传入的参数不能大于3，否则会报越界异常
        //         //消息投递到Topic下指定的queue
        //         return mqs.get(queueNum);
        //     }
        // }, 0, new SendCallback() {
        //     @Override
        //     public void onSuccess(SendResult sendResult) {
        //         System.out.printf("返回结果状态=%s，msg=%s", sendResult.getSendStatus(), sendResult);
        //     }
        //
        //     @Override
        //     public void onException(Throwable e) {
        //         System.out.printf("返回结果状态失败，异常信息=%s", e);
        //         e.printStackTrace();
        //         //补偿机制，根据业务情况进行使用，看是否重试
        //     }
        //
        // });


        System.out.println(sendResult);

        return sendResult.toString();
    }

}
