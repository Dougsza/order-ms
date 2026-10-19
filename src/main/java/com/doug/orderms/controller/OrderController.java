package com.doug.orderms.controller;

import com.doug.orderms.controller.dto.ApiResponse;
import com.doug.orderms.controller.dto.OrderResponse;
import com.doug.orderms.controller.dto.PaginationResponse;
import com.doug.orderms.controller.dto.OrderSummaryResponse;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.services.OrderPublisher;
import com.doug.orderms.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pedidos", description = "Operações para recebimento e consulta de pedidos.")
@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderPublisher orderPublisher;

    public OrderController(OrderService orderService, OrderPublisher orderPublisher){
        this.orderService = orderService;
        this.orderPublisher = orderPublisher;
    }

    @Operation(
            summary = "Receber um pedido",
            description = "Aceita o pedido e o publica para processamento assíncrono. "
                    + "A resposta 202 confirma o recebimento, mas não que o pedido já foi salvo."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Pedido aceito para processamento."
    )
    @PostMapping("/orders")
    public ResponseEntity<Void> createOrder(
                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                    description = "Pedido a ser aceito para processamento assíncrono.",
                                                    content = @Content(examples = @ExampleObject(
                                                            value = "{\"orderCode\": 123, \"clientCode\": 42, \"items\": [{\"product\": \"Teclado\", \"quantity\": 2, \"price\": 49.90}]}"
                                                    ))
                                            )
                                            @RequestBody OrderCreatedEvent event
    ){
        orderPublisher.publish(event);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Listar pedidos de um cliente",
            description = "Retorna os pedidos do cliente em uma página, junto com o total acumulado."

    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedidos do cliente retornados com sucesso."
    )
    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> listOrders(@Parameter(description = "Identificador do cliente.", example = "42")
                                                                 @PathVariable("customerId") Long customerId,

                                                                 @Parameter(description = "Número da página, começando em 0.", example = "0")
                                                                 @RequestParam(name = "page", defaultValue = "0") Integer page,

                                                                 @Parameter(description = "Quantidade máxima de pedidos por página.", example = "10")
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){

    var pageResponse = orderService.findAllByCustomerId(customerId, PageRequest.of(page, pageSize));
    var totalOnOrders = orderService.findTotalOnOrdersByCustomerId(customerId);
    return ResponseEntity.ok(new ApiResponse<>(
                new OrderSummaryResponse(totalOnOrders),
                pageResponse.getContent(),
                PaginationResponse.fromPage(pageResponse)
            ));
    }
}
