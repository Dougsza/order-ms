package com.doug.orderms.controller;

import com.doug.orderms.controller.handler.GlobalExceptionHandler;
import com.doug.orderms.listener.dto.OrderCreatedEvent;
import com.doug.orderms.listener.dto.OrderItemEvent;
import com.doug.orderms.services.OrderPublisher;
import com.doug.orderms.services.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderPublisher orderPublisher;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderWithInvalidQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderCode": 123,
                                  "clientCode": 42,
                                  "items": [
                                    {
                                      "product": "Teclado",
                                      "quantity": 0,
                                      "price": 49.90
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0].field")
                        .value("items[0].quantity"));

        verifyNoInteractions(orderPublisher);
    }

    @Test
    void createOrderWithAValidRequestThenShouldReturnAcceptedAndPublishedEvent() throws Exception{
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderCode":123,
                        "clientCode":42,
                        "items":[
                            {
                                "product": "Teclado",
                                "quantity": 2,
                                "price": 49.90
                            }
                        ]
                    }
                    """))
                .andExpect(status().isAccepted());

        Mockito.verify(orderPublisher).publish(new OrderCreatedEvent(
                123L,
                42L,
                List.of(new OrderItemEvent(
                        "Teclado",
                        2,
                        new BigDecimal("49.90")
                ))
        ));

    }

    @Test
    void listOrdersWithInvalidPageSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/customers/42/orders")
                        .param("page", "0")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0].field").value("pageSize"))
                .andExpect(jsonPath("$.details[0].message")
                        .value("pageSize deve ser pelo menos 1"));

        verifyNoInteractions(orderService);
    }
}