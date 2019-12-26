package com.eavescat.pendulum.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * 消费者消费消息示例代码
 * # 1.消费者的线程数量范围为最小3，最大5，根据项目需要进行调整
 * # 2.示例路由键为：scheduled.appName1.className_methodName1，即scheduled.{groupName}.{jobName}，groupName和jobName从数据库中获取
 * Created by wendrewshay on 2019/7/15 20:57
 */
@Slf4j
@Service
public class ConsumerDemo {

    /**
     * 测试方法1
     * @author wendrewshay
     * @date 2019/7/17 11:54
     * @param message 消息体
     */
    @RabbitListener(queuesToDeclare = {@Queue("appName1.className.methodName1")})
    public void test1(Message message, Channel channel) throws IOException {
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("消费消息test1 >>> 消息结构：{}", message);
        } catch (Exception e) {
            log.error("消费异常：{}", e.getMessage() ,e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    /**
     * 测试方法2
     * @author wendrewshay
     * @date 2019/7/17 11:54
     * @param message 消息体
     */
    @RabbitListener(queuesToDeclare = {@Queue("appName1.className.methodName2")})
    public void test2(Message message, Channel channel) throws IOException {
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("消费消息test2 >>> 消息结构：{}", message);
        } catch (IOException e) {
            log.error("消费异常：{}", e.getMessage() ,e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
