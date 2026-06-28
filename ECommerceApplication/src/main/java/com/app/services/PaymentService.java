package com.app.services;

import com.app.payloads.CheckoutSessionResponse;
import com.app.payloads.PaymentResponse;

public interface PaymentService {

    PaymentResponse getPaymentById(
            Long paymentId
    );

    PaymentResponse getPaymentByOrderId(
            Long orderId
    );

    CheckoutSessionResponse createCheckoutSession(
            Long orderId
    );

    PaymentResponse refundPayment(Long paymentId);

}