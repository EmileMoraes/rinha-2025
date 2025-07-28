package com.rinha.payment_gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByProcessedAtBetween(Instant from, Instant to);
    List<Payment> findByProcessedAtAfter(Instant from);
    List<Payment> findByProcessedAtBefore(Instant to);
}
