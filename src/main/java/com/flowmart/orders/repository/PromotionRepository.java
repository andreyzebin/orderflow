package com.flowmart.orders.repository;

import com.flowmart.orders.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.category = :category " +
           "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActiveByCategory(@Param("category") String category,
                                         @Param("today") LocalDate today);

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findAllActive(@Param("today") LocalDate today);
}
