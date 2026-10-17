package com.doug.orderms.listener;

import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static com.doug.orderms.config.RabbitMqConfig.ORDER_CREATE_QUEUE;

@Component
public class OrderCreatedListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final OrderService orderService;

    public OrderCreatedListener(OrderService orderService) {
        this.orderService = orderService;
    }


    @RabbitListener(queues = ORDER_CREATE_QUEUE)
    public void listen(Message<OrderCreatedEvent> message){
        logger.info("Message consumed: {}",message);

        orderService.save(message.getPayload());
    }
}
