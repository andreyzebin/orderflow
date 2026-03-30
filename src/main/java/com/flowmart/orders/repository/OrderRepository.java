package com.flowmart.orders.repository;

import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerEmailOrderByCreatedAtDesc(String email, Pageable pageable);
    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
}
