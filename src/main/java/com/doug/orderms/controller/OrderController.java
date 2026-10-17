package com.doug.orderms.controller;

import com.doug.orderms.controller.dto.ApiResponse;
import com.doug.orderms.controller.dto.OrderResponse;
import com.doug.orderms.controller.dto.PaginationResponse;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.services.OrderPublisher;
import com.doug.orderms.services.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderPublisher orderPublisher;

    public OrderController(OrderService orderService, OrderPublisher orderPublisher){
        this.orderService = orderService;
        this.orderPublisher = orderPublisher;
    }

    @PostMapping("/orders")
    public ResponseEntity<Void> createOrder(@RequestBody OrderCreatedEvent event){
        orderPublisher.publish(event);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> listOrders(@PathVariable("customerId") Long customerId,
                                                                 @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){

    var pageResponse = orderService.findAllByCustomerId(customerId, PageRequest.of(page, pageSize));
    var totalOnOrders = orderService.findTotalOnOrdersByCustomerId(customerId);
    return ResponseEntity.ok(new ApiResponse<>(
                Map.of("totalOnOrders", totalOnOrders),
                pageResponse.getContent(),
                PaginationResponse.fromPage(pageResponse)
            ));
    }
}
