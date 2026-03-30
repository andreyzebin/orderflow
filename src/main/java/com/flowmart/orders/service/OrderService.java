package com.flowmart.orders.service;

import com.flowmart.orders.exception.ResourceNotFoundException;
import com.flowmart.orders.model.Customer;
import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.OrderItem;
import com.flowmart.orders.model.OrderStatus;
import com.flowmart.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PricingService pricingService;

    @Transactional
    public Order createOrder(Customer customer, List<OrderItem> items) {
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .build();
        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);
        Order saved = orderRepository.save(order);
        return pricingService.calculateTotal(saved);
    }

    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Order cannot be confirmed from status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = findById(orderId);
        if (order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            releaseInventory(item);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void releaseInventory(OrderItem item) {
        // Integration point with inventory-service (async event in production)
    }
}
