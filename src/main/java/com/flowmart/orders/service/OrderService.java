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

/**
 * Application-level orchestration of an Order's lifecycle: create →
 * confirm → cancel. Persistence is delegated to {@link OrderRepository},
 * monetary maths to {@link PricingService}. Methods are transactional
 * so that the status change and any side effects (inventory release,
 * total recalculation) commit atomically with the save.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PricingService pricingService;

    /**
     * Persist a fresh order in PENDING status with the given items already
     * linked, then run the initial pricing calculation through
     * {@link PricingService}. Mutates the input items by setting their
     * back-reference to the new order.
     *
     * @return the saved Order with totals populated.
     */
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

    /**
     * Move an order from PENDING/OPEN to CONFIRMED. Throws if the order
     * is in any other state — confirmation is only meaningful at the
     * start of the lifecycle.
     */
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = findById(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Order cannot be confirmed from status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    /**
     * Cancel an order and release inventory for each of its items.
     * Permitted from any state except SHIPPED/DELIVERED — once goods
     * are out the door, cancellation is the warehouse's problem, not
     * this service's.
     */
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

    /**
     * Lookup helper. Throws {@link ResourceNotFoundException} when the
     * order is missing — controllers turn this into HTTP 404.
     */
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void releaseInventory(OrderItem item) {
        // Integration point with inventory-service (async event in production)
    }
}
