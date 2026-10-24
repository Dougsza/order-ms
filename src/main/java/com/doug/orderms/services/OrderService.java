package com.doug.orderms.services;

import com.doug.orderms.controller.dto.OrderResponse;
import com.doug.orderms.entity.OrderEntity;
import com.doug.orderms.entity.OrderItem;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.repository.OrderRepository;
import org.bson.Document;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;
    public OrderService(OrderRepository orderRepository,
                        MongoTemplate mongoTemplate) {

        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(OrderCreatedEvent event){

        var entity = new OrderEntity();
        entity.setOrderId(event.orderCode());
        entity.setCustomerId(event.clientCode());
        entity.setItems(gerOrderItems(event));
        entity.setTotal(getTotal(event));

        orderRepository.save(entity);
    }

    public Page<OrderResponse> findAllByCustomerId(Long customerId, PageRequest pageRequest){
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        return orders.map(OrderResponse::fromEntity);
    }

    public BigDecimal findTotalOnOrdersByCustomerId(Long customerId){
        var aggregations = newAggregation(
                match(Criteria.where("customerId").is(customerId)),
                group().sum("total").as("total")
        );

        var response = mongoTemplate.aggregate(aggregations, "tb_orders", Document.class);
        var result = response.getUniqueMappedResult();

        if (result == null || result.get("total") == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(result.get("total").toString());
    }

    private BigDecimal getTotal(OrderCreatedEvent event) {
        return event.items()
                .stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static @NonNull List<OrderItem> gerOrderItems(OrderCreatedEvent event) {
        return event.items().stream()
                .map(i -> new OrderItem(i.product(), i.quantity(), i.price()))
                .toList();
    }
}
