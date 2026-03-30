package com.flowmart.orders.controller;

import com.flowmart.orders.exception.ResourceNotFoundException;
import com.flowmart.orders.model.Order;
import com.flowmart.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Order order = orderService.findById(orderId);
        if (!order.getCustomer().getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("Not your order");
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Order order = orderService.findById(orderId);
        if (!order.getCustomer().getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("Not your order");
        }
        return ResponseEntity.ok(orderService.confirmOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Order order = orderService.findById(orderId);
        if (!order.getCustomer().getEmail().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("Not your order");
        }
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
