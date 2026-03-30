package com.flowmart.orders.service;

import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.OrderItem;
import com.flowmart.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final OrderRepository orderRepository;

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
}
