package com.doug.orderms;


import com.doug.orderms.config.RabbitMqConfig;
import com.doug.orderms.entity.OrderEntity;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.listener.dto.OrderItemEvent;
import com.doug.orderms.repository.OrderRepository;
import com.doug.orderms.services.OrderPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.awaitility.Awaitility.await;

import com.doug.orderms.services.OrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@SpringBootTest
public class OrderProcessingIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private OrderPublisher orderPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishedOrderIsConsumedAndSavedInMongo(){
        long orderId = System.currentTimeMillis();

        var event = new OrderCreatedEvent(
                orderId,
                456L,
                List.of(new OrderItemEvent(
                        "Teclado",
                        2,
                        new BigDecimal("49.90")

                ))
        );

        orderPublisher.publish(event);
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(()->{
                    OrderEntity saveOrder = orderRepository.findById(orderId)
                            .orElseThrow();

                    assertEquals(456L, saveOrder.getCustomerId());
                    assertEquals(0, new BigDecimal("99.80")
                            .compareTo(saveOrder.getTotal()));
                });
    }

    @Test
    void failedOrderIsRetriedAndSentToDeadLetterQueue(){
        long orderId = System.currentTimeMillis();

        var event = new OrderCreatedEvent(
                orderId,
                456L,
                List.of(
                        new OrderItemEvent(
                                "Teclado",
                                2,
                                new BigDecimal("49.90")
                        ))

        );

        Mockito.doThrow(new IllegalStateException("Simulated processing failure"))
                .when(orderService)
                .save(event);

        orderPublisher.publish(event);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                            Mockito.verify(orderService, Mockito.times(3)).save(event)
                        );
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertNotNull(rabbitTemplate.receive(RabbitMqConfig.ORDER_CREATE_DLQ))
                );
    }

}
