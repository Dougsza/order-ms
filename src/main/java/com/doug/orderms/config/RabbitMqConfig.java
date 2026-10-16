package com.doug.orderms.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_CREATE_QUEUE = "btg-pactual-order-created";
}
