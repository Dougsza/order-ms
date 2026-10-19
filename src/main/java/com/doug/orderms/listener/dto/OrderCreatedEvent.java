package com.doug.orderms.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OrderCreatedEvent (
        @Schema(description = "Identificador do pedido.", example = "123")
        Long orderCode,

        @Schema(description = "Identificador do cliente.", example = "42")
        long clientCode,

        @Schema(description = "Lista de items do pedido")
        List<OrderItemEvent> items
) {
}
