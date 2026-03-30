package com.flowmart.orders.controller;

import com.flowmart.orders.model.Order;
import com.flowmart.orders.model.StoreCredit;
import com.flowmart.orders.service.CustomerService;
import com.flowmart.orders.service.StoreCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final StoreCreditService storeCreditService;

    @GetMapping("/me/store-credits")
    public ResponseEntity<List<StoreCredit>> getMyCredits(
            @AuthenticationPrincipal UserDetails currentUser) {

        Long customerId = customerService.findByEmail(currentUser.getUsername()).getId();
        return ResponseEntity.ok(storeCreditService.getAvailableCredits(customerId));
    }

    @GetMapping("/me/store-credits/total")
    public ResponseEntity<BigDecimal> getMyTotalCredit(
            @AuthenticationPrincipal UserDetails currentUser) {

        Long customerId = customerService.findByEmail(currentUser.getUsername()).getId();
        return ResponseEntity.ok(storeCreditService.getTotalAvailable(customerId));
    }

    // BUG-1 (mirrors StoreCreditService): controller passes creditId directly
    // to the service without first checking that the credit belongs to currentUser.
    // The service also lacks this check, so the IDOR is end-to-end.
    @PostMapping("/me/store-credits/{creditId}/redeem")
    public ResponseEntity<Order> redeemCredit(
            @PathVariable Long creditId,
            @RequestParam Long orderId,
            @AuthenticationPrincipal UserDetails currentUser) {

        return ResponseEntity.ok(storeCreditService.redeemCredit(creditId, orderId));
    }
}
