package com.doug.orderms.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderSummaryResponse(
        @Schema(
                description = "Valor total acumulado dos pedidos do cliente.",
                example = "125.30"
        )
        BigDecimal totalOnOrders
) {
}