package com.doug.orderms.services;

import com.doug.orderms.controller.dto.OrderResponse;
import com.doug.orderms.entity.OrderEntity;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.listener.dto.OrderItemEvent;
import com.doug.orderms.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveCalculatesOrderTotalAndMapsItems() {
        var event = new OrderCreatedEvent(
                123L,
                456L,
                List.of(
                        new OrderItemEvent("Keyboard", 2, new BigDecimal("49.90")),
                        new OrderItemEvent("Mouse", 1, new BigDecimal("25.50"))
                )
        );

        orderService.save(event);

        var entityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(entityCaptor.capture());

        var savedOrder = entityCaptor.getValue();
        assertEquals(123L, savedOrder.getOrderId());
        assertEquals(456L, savedOrder.getCustomerId());
        assertEquals(0, new BigDecimal("125.30").compareTo(savedOrder.getTotal()));
        assertEquals(2, savedOrder.getItems().size());
        assertEquals("Keyboard", savedOrder.getItems().get(0).getProduct());
        assertEquals(2, savedOrder.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("49.90"), savedOrder.getItems().get(0).getPrice());
        assertEquals("Mouse", savedOrder.getItems().get(1).getProduct());
    }

    @Test
    void saveUsesZeroTotalWhenOrderHasNoItems() {
        var event = new OrderCreatedEvent(123L, 456L, List.of());

        orderService.save(event);

        var entityCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(entityCaptor.capture());
        assertEquals(BigDecimal.ZERO, entityCaptor.getValue().getTotal());
        assertEquals(List.of(), entityCaptor.getValue().getItems());
    }

    @Test
    void findAllByCustomerIdMapsOrdersToResponses() {
        var customerId = 456L;
        var pageRequest = PageRequest.of(1, 5);
        var order = new OrderEntity();
        order.setOrderId(123L);
        order.setCustomerId(customerId);
        order.setTotal(new BigDecimal("125.30"));
        Mockito.when(orderRepository.findAllByCustomerId(customerId, pageRequest))
                .thenReturn(new PageImpl<>(List.of(order), pageRequest, 6));

        var result = orderService.findAllByCustomerId(customerId, pageRequest);

        assertEquals(1, result.getContent().size());
        assertEquals(
                new OrderResponse(123L, customerId, new BigDecimal("125.30")),
                result.getContent().get(0)
        );
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getTotalPages());
        verify(orderRepository).findAllByCustomerId(customerId, pageRequest);
    }

    @Test
    void findTotalOnOrdersByCustomerIdThenReturnsZeroWhenNoOrdersExists(){
        Mockito.when(mongoTemplate.aggregate(
                Mockito.any(Aggregation.class),
                Mockito.eq("tb_orders"),
                Mockito.eq(Document.class)
        )).thenReturn(new AggregationResults<>(List.of(), new Document()));

        var result = orderService.findTotalOnOrdersByCustomerId(456L);

        assertEquals(BigDecimal.ZERO, result);
    }
}
