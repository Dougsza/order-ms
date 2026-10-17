package com.doug.orderms.services;

import com.doug.orderms.config.RabbitMqConfig;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish (OrderCreatedEvent event){
        rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_CREATE_QUEUE, event);
    }
}
