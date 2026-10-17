package com.doug.orderms.services;


import com.doug.orderms.config.RabbitMqConfig;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderPublisher orderPublisher;

    @Test
    void publishSendsEventToOrderQueue(){
        var event = new OrderCreatedEvent(1L, 42L, List.of());

        orderPublisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfig.ORDER_CREATE_QUEUE,
                event
        );
    }

}
