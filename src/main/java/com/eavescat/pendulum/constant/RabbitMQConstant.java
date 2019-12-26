package com.eavescat.pendulum.constant;

/**
 * rabbitmq常量类
 * Created by wendrewshay on 2019/7/15 12:44
 */
public class RabbitMQConstant {

    /**
     * 队列名称
     */
    public static final String QUEUE_NAME = "pendulum_queue";
    /**
     * 交换器名称
     */
    public static final String EXCHANGE_NAME = "pendulum_topic";
    /**
     * 路由键
     */
    public static final String ROUTING_KEY = "scheduled.#";
}
