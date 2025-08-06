package org.example.order.processingconsumer.kafka;

import lombok.RequiredArgsConstructor;
import org.example.order.processingconsumer.dto.OrderRequestDto;
import org.example.order.processingconsumer.service.OrderProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProcessingConsumer {

    private final OrderProcessingService orderProcessingService;

    @KafkaListener(topics = "order-topic", groupId = "process-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderRequestDto dto) {
        orderProcessingService.process(dto);
    }
}