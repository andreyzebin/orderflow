package com.flowmart.orders.controller;

import com.flowmart.orders.exception.ResourceNotFoundException;
import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.Promotion;
import com.flowmart.orders.repository.OrderRepository;
import com.flowmart.orders.repository.PromotionRepository;
import com.flowmart.orders.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PromotionController {

    private final PricingService pricingService;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;

    @GetMapping("/promotions/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        return ResponseEntity.ok(promotionRepository.findAllActive(LocalDate.now()));
    }

    // BUG-2: IDOR — no ownership check before applying the promotion.
    // Any authenticated customer can apply a promotion to any other customer's order.
    // Compare with OrderController where every mutating endpoint verifies:
    //   order.getCustomer().getEmail().equals(currentUser.getUsername())
    @PostMapping("/orders/{orderId}/promotions/{promotionId}/apply")
    public ResponseEntity<Order> applyPromotion(
            @PathVariable Long orderId,
            @PathVariable Long promotionId,
            @AuthenticationPrincipal UserDetails currentUser) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        return ResponseEntity.ok(pricingService.applyBulkDiscount(order, promotion));
    }
}
