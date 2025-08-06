package org.example.order.processingconsumer.service;

import lombok.RequiredArgsConstructor;
import org.example.order.processingconsumer.dto.OrderRequestDto;
import org.example.order.processingconsumer.entity.Order;
import org.example.order.processingconsumer.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;

    public void process(OrderRequestDto dto) {
        Order order = new Order();
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity());
        orderRepository.save(order);
    }
}