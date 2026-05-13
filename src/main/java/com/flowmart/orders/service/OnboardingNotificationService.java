package com.flowmart.orders.service;

import com.flowmart.orders.model.Customer;
import com.flowmart.orders.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends welcome emails when customers complete onboarding.
 *
 * Wired into the onboarding webhook handler; runs synchronously after a
 * customer's first order so the welcome message can reference that order.
 */
@Service
public class OnboardingNotificationService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Notify every customer in the supplied id list. Used by the nightly
     * cohort job and the manual re-send admin endpoint.
     */
    public void notifyAll(List<Long> customerIds) {
        for (Long customerId : customerIds) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "customer not found: " + customerId));
            sendWelcomeEmail(customer);
        }
    }

    private void sendWelcomeEmail(Customer customer) {
        // Delegated to com.flowmart.notifications.EmailGateway in production;
        // stubbed here pending the EmailGateway interface PR.
    }
}
