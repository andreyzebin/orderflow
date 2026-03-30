package com.flowmart.orders.repository;

import com.flowmart.orders.model.StoreCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StoreCreditRepository extends JpaRepository<StoreCredit, Long> {

    @Query("SELECT sc FROM StoreCredit sc WHERE sc.customer.id = :customerId " +
           "AND sc.redeemed = false AND sc.expiresAt >= :today")
    List<StoreCredit> findAvailableByCustomerId(@Param("customerId") Long customerId,
                                                @Param("today") LocalDate today);
}
