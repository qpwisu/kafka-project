package org.example.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderRequest {

    @Id @GeneratedValue
    private Long id;

    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;

    private OrderRequest(Long productId, int quantity, LocalDateTime createdAt) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public static OrderRequest of(Long productId, int quantity) {
        return new OrderRequest(productId, quantity, LocalDateTime.now());
    }
}