package org.example.order.kafka;

import lombok.RequiredArgsConstructor;
import org.example.order.dto.OrderRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, OrderRequestDto> kafkaTemplate;

    @Value("${kafka.topic}")
    private String topic;

    public void send(OrderRequestDto dto) {
        kafkaTemplate.send(topic, dto);
    }
}