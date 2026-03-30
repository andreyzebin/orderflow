package com.flowmart.orders.service;

import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.OrderItem;
import com.flowmart.orders.model.Promotion;
import com.flowmart.orders.repository.OrderItemRepository;
import com.flowmart.orders.repository.OrderRepository;
import com.flowmart.orders.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PromotionRepository promotionRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");

    @Transactional
    public Order calculateTotal(Order order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setTaxAmount(tax);
        order.setTotal(subtotal.add(tax));
        return orderRepository.save(order);
    }

    @Transactional
    public Order applyFlatDiscount(Order order, BigDecimal discountAmount) {
        BigDecimal discounted = order.getSubtotal().subtract(discountAmount);
        BigDecimal tax = discounted.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(tax);
        order.setTotal(discounted.add(tax));
        return orderRepository.save(order);
    }

    // BUG-3: missing @Transactional — two writes without atomicity.
    // If orderRepository.save() throws after orderItemRepository.saveAll()
    // succeeds, items will be discounted but the order total unchanged.
    public Order applyBulkDiscount(Order order, Promotion promotion) {
        List<OrderItem> eligible = findEligibleItems(order, promotion);
        if (eligible.isEmpty()) {
            return order;
        }

        List<List<OrderItem>> groups = partitionGroups(eligible, promotion.getBuyQuantity());
        List<OrderItem> toUpdate = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (List<OrderItem> group : groups) {
            OrderItem freeItem = selectFreeItem(group);
            freeItem.setDiscountAmount(freeItem.getUnitPrice());
            toUpdate.add(freeItem);
            totalDiscount = totalDiscount.add(freeItem.getUnitPrice());
        }

        orderItemRepository.saveAll(toUpdate);
        order.setDiscountAmount(totalDiscount);
        order.setTotal(order.getSubtotal().subtract(totalDiscount).add(order.getTaxAmount()));
        return orderRepository.save(order);
    }

    // BUG-4: N+1 — one SQL query per category instead of a single IN query.
    // Should use promotionRepository.findActiveByCategoryIn(categories, today).
    public List<Promotion> findApplicablePromotions(Order order) {
        Set<String> categories = order.getItems().stream()
                .map(item -> item.getProduct().getCategory())
                .collect(Collectors.toSet());

        List<Promotion> result = new ArrayList<>();
        for (String category : categories) {
            result.addAll(
                promotionRepository.findActiveByCategory(category, LocalDate.now())
            );
        }
        return result;
    }

    // ── private helpers ─────────────────────────────────────────────────

    // BUG-1: returns the first item in the group, not the cheapest.
    // Business rule (AGENTS.md + ticket AC): free item must be the one
    // with the lowest unitPrice in the qualifying group.
    private OrderItem selectFreeItem(List<OrderItem> group) {
        return group.get(0);
    }

    private List<OrderItem> findEligibleItems(Order order, Promotion promotion) {
        return order.getItems().stream()
                .filter(item -> promotion.getCategory()
                        .equals(item.getProduct().getCategory()))
                .collect(Collectors.toList());
    }

    private List<List<OrderItem>> partitionGroups(List<OrderItem> items, int buyQty) {
        List<List<OrderItem>> groups = new ArrayList<>();
        int step = buyQty + 1;
        for (int i = 0; i + step <= items.size(); i += step) {
            groups.add(new ArrayList<>(items.subList(i, i + step)));
        }
        return groups;
    }
}
