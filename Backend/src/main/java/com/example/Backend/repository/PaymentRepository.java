package com.example.Backend.repository;

import com.example.Backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByUserId(UUID userId);

    List<Payment> findByOrderId(UUID orderId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    Optional<Payment> findByOrderIdAndStatus(UUID orderId, Payment.PaymentStatus status);

    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);
}
