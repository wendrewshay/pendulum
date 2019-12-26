package com.eavescat.pendulum.config;

import com.eavescat.pendulum.constant.RabbitMQConstant;
import com.eavescat.pendulum.domain.JobInfo;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.SendRetryContextAccessor;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;
import java.util.UUID;

/**
 * 自定义rabbitmq配置
 * Created by wendrewshay on 2019/7/15 10:47
 */
@Slf4j
@EnableRabbit
@Configuration
public class MyRabbitMQConfig {

    private AmqpAdmin amqpAdmin;
    public MyRabbitMQConfig(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    /**
     * 声明交换器
     * @author wendrewshay
     * @date 2019/7/15 14:04
     * @return TopicExchange
     */
    @Bean
    @Qualifier(RabbitMQConstant.EXCHANGE_NAME)
    public TopicExchange exchange() {
        return new TopicExchange(RabbitMQConstant.EXCHANGE_NAME, true, false);
    }

    /**
     * 消息重试机制
     * @author wendrewshay
     * @date 2019/7/18 11:32
     * @return RetryTemplate
     */
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // 重试次数-3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, Collections.singletonMap(Exception.class, true));
        retryTemplate.setRetryPolicy(retryPolicy);
        // 重试间隔-5秒
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(5000);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(300000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

//    @Bean
//    public StatefulRetryOperationsInterceptor statefulRetryOperationsInterceptor() {
//        return RetryInterceptorBuilder.stateful()
//                .backOffOptions(5000, 10.0, 300000)
//                .maxAttempts(3)
//                .messageKeyGenerator(message -> UUID.randomUUID().toString())
//                .build();
//    }

    /**
     * 配置rabbitTemplate
     * @author wendrewshay
     * @date 2019/7/15 20:14
     * @param connectionFactory 连接工厂
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setExchange(RabbitMQConstant.EXCHANGE_NAME);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("ConfirmCallback >>> Succeed: correlationData={}, cause={}：", correlationData, cause);
            } else {
                log.error("ConfirmCallback >>> Failed: correlationData={}, cause={}：", correlationData, cause);
            }
        });
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) ->
                log.info("ReturnCallback >>> message:{}, replyCode:{}, replyText:{}, exchange:{}, routingKey:{}", message, replyCode, replyText, exchange, routingKey));
        return rabbitTemplate;
    }

    /**
     * 简单消息监听容器
     * @author wendrewshay
     * @date 2019/7/16 18:01
     * @param connectionFactory 连接工厂
     * @return SimpleRabbitListenerContainerFactory
     */
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        messageListenerContainer.setConsumerTagStrategy(queue -> queue + "_" + UUID.randomUUID().toString());
        messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        messageListenerContainer.setConcurrentConsumers(3);
        messageListenerContainer.setMaxConcurrentConsumers(5);
        messageListenerContainer.setDefaultRequeueRejected(false);
        messageListenerContainer.setExclusive(false);
//        messageListenerContainer.setQueueNames("appName1.className.methodName1");
//        messageListenerContainer.setMessageListener(message -> {
//            log.info(">>> message:{}", message);
//        });
//        messageListenerContainer.setAdviceChain(statefulRetryOperationsInterceptor());
        return messageListenerContainer;
    }

    /**
     * 动态声明队列和绑定关系
     * @author wendrewshay
     * @date 2019/7/16 22:07
     * @param jobInfo 定时任务对象
     */
    public void declare(JobInfo jobInfo) {
        String routingKey = String.format("%s.%s", jobInfo.getGroupName(), jobInfo.getJobName());
        Queue queue = new Queue(routingKey, true, false, false);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange()).with(routingKey));
    }
}
