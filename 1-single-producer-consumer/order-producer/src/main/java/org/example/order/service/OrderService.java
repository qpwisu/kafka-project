package org.example.order.service;

import lombok.RequiredArgsConstructor;
import org.example.order.dto.OrderRequestDto;
import org.example.order.entity.OrderRequest;
import org.example.order.entity.Product;
import org.example.order.kafka.KafkaProducer;
import org.example.order.repository.OrderRequestRepository;
import org.example.order.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRequestRepository orderRequestRepository;
    private final KafkaProducer kafkaProducer;

    public void createOrder(OrderRequestDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        OrderRequest order = OrderRequest.of(dto.getProductId(), dto.getQuantity());
        orderRequestRepository.save(order);
        kafkaProducer.send(dto);
    }
}