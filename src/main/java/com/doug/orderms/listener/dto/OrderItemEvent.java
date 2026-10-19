package com.doug.orderms.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderItemEvent(
        @Schema(description = "Nome do produto.", example = "Teclado")
        String product,

        @Schema(description = "Quantidade do produtos no pedido", example = "2")
        Integer quantity,

        @Schema(description = "Preço unitário do produto.", example = "49.90")
        BigDecimal price
) {
}
