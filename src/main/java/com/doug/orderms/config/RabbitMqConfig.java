package com.doug.orderms.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_CREATE_QUEUE = "btg-pactual-order-created";
    public static final String ORDER_CREATE_DLQ = ORDER_CREATE_QUEUE + ".dlq";
    public static final String ORDER_DEAD_LETTER_EXCHANGE = "orders.dlx";

    @Bean
    public JacksonJsonMessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",ORDER_CREATE_DLQ)
                .build();
    }

    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return new DirectExchange(ORDER_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_CREATE_DLQ).build();
    }


    @Bean
    public Binding orderCreatedDeadLetterBinding(
            @Qualifier("orderCreatedDeadLetterQueue") Queue deadLetterQueue,
            @Qualifier("orderDeadLetterExchange") DirectExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(ORDER_CREATE_DLQ);
    }
}
