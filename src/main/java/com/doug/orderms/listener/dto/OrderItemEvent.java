package com.doug.orderms.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemEvent(
        @NotBlank(message = "product não pode ser vazio")
        @Schema(description = "Nome do produto.", example = "Teclado")
        String product,

        @NotNull(message = "quantity não pode ser nulo")
        @Positive(message = "quantity deve ser maior que zero")
        @Schema(description = "Quantidade do produtos no pedido", example = "2")
        Integer quantity,

        @NotNull(message = "price não pode ser nulo")
        @Positive(message = "Preço deve ser positivo")
        @Schema(description = "Preço unitário do produto.", example = "49.90")
        BigDecimal price
) {
}
