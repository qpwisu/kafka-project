package org.example.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.order.dto.OrderRequestDto;
import org.example.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto dto) {
        orderService.createOrder(dto);
        return ResponseEntity.ok("Order Created");
    }
}