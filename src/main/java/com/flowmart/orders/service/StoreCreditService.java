package com.flowmart.orders.service;

import com.flowmart.orders.exception.ResourceNotFoundException;
import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.StoreCredit;
import com.flowmart.orders.repository.OrderRepository;
import com.flowmart.orders.repository.StoreCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreCreditService {

    private final StoreCreditRepository storeCreditRepository;
    private final OrderRepository orderRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");

    @Transactional
    public Order redeemCredit(Long creditId, Long orderId) {
        StoreCredit credit = storeCreditRepository.findById(creditId)
                .orElseThrow(() -> new ResourceNotFoundException("Store credit not found"));

        if (credit.isRedeemed()) {
            throw new IllegalStateException("Store credit already redeemed");
        }
        if (credit.getExpiresAt().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Store credit has expired");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        BigDecimal credited = credit.getAmount().min(order.getTotal());
        order.setDiscountAmount(order.getDiscountAmount().add(credited));
        order.setTotal(order.getTotal().subtract(credited));

        credit.setRedeemed(true);
        storeCreditRepository.save(credit);
        return orderRepository.save(order);
    }

    public List<StoreCredit> getAvailableCredits(Long customerId) {
        return storeCreditRepository.findAvailableByCustomerId(customerId, LocalDate.now());
    }

    public BigDecimal getTotalAvailable(Long customerId) {
        return getAvailableCredits(customerId).stream()
                .map(StoreCredit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
