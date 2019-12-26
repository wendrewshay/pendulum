package com.eavescat.pendulum.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 自定义与RabbitMQ整合的Job
 * Created by wendrewshay on 2019/7/13 11:27
 */
@Slf4j
public class RabbitJob implements Job {

    private RabbitTemplate rabbitTemplate;
    public RabbitJob(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        // 路由键
        JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
        String routingKey = String.format("%s.%s", jobKey.getGroup(), jobKey.getName().substring(0, jobKey.getName().lastIndexOf("_")));
        // 消息属性和内容
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding("UTF-8");
        messageProperties.setContentType("application/json");
        messageProperties.setHeader("pendulum_timestamp", System.currentTimeMillis());
        Message message = new Message("1".getBytes(), messageProperties);

        // 发送消息
        rabbitTemplate.convertAndSend(routingKey, message);
        log.info("发送消息 >>> 路由键：{}，消息结构：{}", routingKey, message);
    }

}
