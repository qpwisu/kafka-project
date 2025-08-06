package org.example.order.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long productId;
    private int quantity;
    private LocalDateTime createdAt = LocalDateTime.now(); // 현재 시간 자동 설정
}