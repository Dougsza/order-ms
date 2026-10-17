package com.doug.orderms.services;

import com.doug.orderms.controller.OrderController;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.listener.dto.OrderItemEvent;
import com.doug.orderms.services.OrderPublisher;
import com.doug.orderms.services.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderPublisher orderPublisher;

    @InjectMocks
    private OrderController orderController;


    @Test
    void createOrderPublishesEventAndReturnsAccept(){
        var event = new OrderCreatedEvent(
                2L,
                42L,
                List.of( new OrderItemEvent(
                        "Iphone",
                        2,
                        new BigDecimal("1500.00")
                ))
        );

        var response = orderController.createOrder(event);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNull(response.getBody());
        verify(orderPublisher).publish(event);
        verifyNoInteractions(orderService);
    }


}
