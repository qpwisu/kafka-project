package org.example.consumer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;
}