package com.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.entites.Payment;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(
            String transactionId
    );

    Optional<Payment> findByGatewayTransactionId(
            String gatewayTransactionId
    );

    Optional<Payment> findByOrder_OrderId(
            Long orderId
    );

    Optional<Payment> findByStripePaymentIntentId(
            String paymentIntentId
    );

    Optional<Payment> findByStripeCheckoutSessionId(
            String stripeCheckoutSessionId
    );

    boolean existsByIdempotencyKey(
            String idempotencyKey
    );

}