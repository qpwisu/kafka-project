package org.example.order.processingconsumer.repository;

import org.example.order.processingconsumer.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}