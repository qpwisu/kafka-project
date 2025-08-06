package org.example.consumer.service;

import lombok.RequiredArgsConstructor;
import org.example.consumer.dto.OrderRequestDto;
import org.example.consumer.entity.OrderLog;
import org.example.consumer.repository.OrderLogRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderConsumerService {

    private final OrderLogRepository orderLogRepository;

    public void save(OrderRequestDto dto) {
        OrderLog log = new OrderLog(dto.getProductId(), dto.getQuantity(), dto.getCreatedAt());
        orderLogRepository.save(log);
    }
}