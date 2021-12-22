package com.psq.code.config;

/**
 * @Description rocketmq配置文件
 * @Author psq
 * @Date 2021/10/26 16:28
 */
public class RocketMqConfig {

    /**
     * rocketmq-broker访问地址
     */

    public static final String NAME_SERVER_ADDR = "IPA:9876";

    /**
     * 生产者组：相当于区分微商城普通、批发大宗
     */
    public static final String PRODUCER_GROUP = "pay_group";

    /**
     * 消费者组：与上面对应；注意每个消费者组名不能一样
     */
    public static final String CONSUMER_GROUP = "pay_consumer_group";

    /**
     * 支付话题：相当于商城的：商品、活动、下单、支付、物流等等
     */
    public static final String PAY_TOPIC = "pay_topic";



    /**
     * 生产者组
     */
    public static final String ORDER_GROUP = "order_group";

    /**
     * 消费者组：每个消费者组名不能一样
     */
    public static final String ORDER_CONSUMER_GROUP = "order_consumer_group";

    /**
     * 订单话题
     */
    public static final String ORDER_TOPIC = "order_topic";

}
