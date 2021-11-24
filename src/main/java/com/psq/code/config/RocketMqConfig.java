package com.psq.code.config;

/**
 * @Description ocketmq配置文件
 * @Author psq
 * @Date 2021/10/26 16:28
 */
public class RocketMqConfig {

    /**
     * rocketmq-broker访问地址
     */

    public static final String NAME_SERVER_ADDR = "IPA:9876;IPB:9876";

    /**
     * 话题
     */
    public static final String TOPIC = "pay_topic_test";

    /**
     * 生产者组
     */
    public static final String PRODUCER_GROUP = "pay_group";

    /**
     * 消费者组
     */
    public static final String CONSUMER_GROUP = "pay_consumer_group";

}
