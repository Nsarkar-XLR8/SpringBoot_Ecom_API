package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.CheckoutSession;
import com.ecommerce.shop.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {

    Optional<CheckoutSession> findByStripeSessionId(String stripeSessionId);

    Optional<CheckoutSession> findByStripePaymentIntent(String stripePaymentIntent);

    Optional<CheckoutSession> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    @Query("SELECT cs.id FROM CheckoutSession cs " +
            "WHERE cs.paymentStatus = :status AND cs.processed = false")
    List<Long> findPendingIdsForValidation(@Param("status") PaymentStatus status);

    @Query("SELECT DISTINCT cs FROM CheckoutSession cs " +
            "LEFT JOIN FETCH cs.items i " +
            "LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH cs.user " +
            "WHERE cs.id = :id")
    Optional<CheckoutSession> findByIdForProcessing(@Param("id") Long id);
}
