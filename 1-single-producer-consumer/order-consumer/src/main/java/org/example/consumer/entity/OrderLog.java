package org.example.consumer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class OrderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;
    private LocalDateTime createdAt;

    public OrderLog(Long productId, int quantity, LocalDateTime createdAt) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }
}