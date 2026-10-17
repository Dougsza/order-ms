package com.doug.orderms.listener.dto;

import java.util.List;

public record OrderCreatedEvent (
        Long orderCode,
        long clientCode,
        List<OrderItemEvent> items
) {
}
