package org.example.order.processingconsumer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {
    private String productId;
    private int quantity;
}