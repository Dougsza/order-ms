package com.doug.orderms.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreatedEvent (
        @NotNull(message = "orderCode não pode ser nulo")
        @Positive(message = "orderCode deve ser positivo")
        @Schema(description = "Identificador do pedido.", example = "123")
        Long orderCode,

        @NotNull(message = "clientCode não pode ser nulo")
        @Positive(message = "clientCode deve ser positivo")
        @Schema(description = "Identificador do cliente.", example = "42")
        Long clientCode,

        @NotEmpty(message = "items não pode ser vazio")
        @Schema(description = "Lista de items do pedido")
        List<@Valid OrderItemEvent> items
) {
}
