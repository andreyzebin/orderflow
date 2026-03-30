package com.flowmart.orders.service;

import com.flowmart.orders.exception.ResourceNotFoundException;
import com.flowmart.orders.model.Customer;
import com.flowmart.orders.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + email));
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    @Transactional
    public Customer updateProfile(Long id, String fullName, String phone, String shippingAddress) {
        Customer customer = findById(id);
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setShippingAddress(shippingAddress);
        return customerRepository.save(customer);
    }
}
