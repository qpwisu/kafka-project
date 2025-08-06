package org.example.consumer.kafka;

import lombok.RequiredArgsConstructor;
import org.example.consumer.dto.OrderRequestDto;
import org.example.consumer.service.OrderConsumerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderConsumerService orderConsumerService;

    @KafkaListener(topics = "order-topic", groupId = "log-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderRequestDto dto) {
        orderConsumerService.save(dto);
    }
}