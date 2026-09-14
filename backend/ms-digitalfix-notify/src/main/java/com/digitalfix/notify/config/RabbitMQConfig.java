package com.digitalfix.notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "digitalfix.workorder.exchange";
    public static final String DLX = "digitalfix.workorder.dlx";

    public static final String CREATED_QUEUE = "workorder.created.queue";
    public static final String STATUS_QUEUE = "workorder.status.queue";
    public static final String COMPLETED_QUEUE = "workorder.completed.queue";

    public static final String CREATED_DLQ = "workorder.created.queue.dlq";
    public static final String STATUS_DLQ = "workorder.status.queue.dlq";
    public static final String COMPLETED_DLQ = "workorder.completed.queue.dlq";

    public static final String CREATED_ROUTING_KEY = "workorder.created";
    public static final String STATUS_ROUTING_KEY = "workorder.status.#";
    public static final String COMPLETED_ROUTING_KEY = "workorder.completed";

    @Bean
    public TopicExchange workOrderExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange workOrderDlx() {
        return ExchangeBuilder.topicExchange(DLX).durable(true).build();
    }

    @Bean
    public Queue createdQueue() {
        return QueueBuilder.durable(CREATED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(CREATED_DLQ)
                .build();
    }

    @Bean
    public Queue statusQueue() {
        return QueueBuilder.durable(STATUS_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(STATUS_DLQ)
                .build();
    }

    @Bean
    public Queue completedQueue() {
        return QueueBuilder.durable(COMPLETED_QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(COMPLETED_DLQ)
                .build();
    }

    @Bean
    public Queue createdDlq() {
        return QueueBuilder.durable(CREATED_DLQ).build();
    }

    @Bean
    public Queue statusDlq() {
        return QueueBuilder.durable(STATUS_DLQ).build();
    }

    @Bean
    public Queue completedDlq() {
        return QueueBuilder.durable(COMPLETED_DLQ).build();
    }

    @Bean
    public Binding createdBinding(Queue createdQueue, TopicExchange workOrderExchange) {
        return BindingBuilder.bind(createdQueue).to(workOrderExchange).with(CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding statusBinding(Queue statusQueue, TopicExchange workOrderExchange) {
        return BindingBuilder.bind(statusQueue).to(workOrderExchange).with(STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding completedBinding(Queue completedQueue, TopicExchange workOrderExchange) {
        return BindingBuilder.bind(completedQueue).to(workOrderExchange).with(COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding createdDlqBinding(Queue createdDlq, TopicExchange workOrderDlx) {
        return BindingBuilder.bind(createdDlq).to(workOrderDlx).with(CREATED_DLQ);
    }

    @Bean
    public Binding statusDlqBinding(Queue statusDlq, TopicExchange workOrderDlx) {
        return BindingBuilder.bind(statusDlq).to(workOrderDlx).with(STATUS_DLQ);
    }

    @Bean
    public Binding completedDlqBinding(Queue completedDlq, TopicExchange workOrderDlx) {
        return BindingBuilder.bind(completedDlq).to(workOrderDlx).with(COMPLETED_DLQ);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JsonMessageConverter.TypePrecedence.INFERRED);
        return converter;
    }
}
