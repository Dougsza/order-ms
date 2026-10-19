package com.doug.orderms.controller.dto;

import com.doug.orderms.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderResponse(
                            @Schema(description = "Identificador do pedido.", example = "123")
                            Long orderId,

                            @Schema(description = "Identificador do cliente.", example = "42")
                            Long customerId,

                            @Schema(description = "Valor total do pedido", example = "125.30")
                            BigDecimal total) {

    public static OrderResponse fromEntity(OrderEntity orderEntity){
        return new OrderResponse(orderEntity.getOrderId(),
                                 orderEntity.getCustomerId(),
                                 orderEntity.getTotal()
        );
    }
}
