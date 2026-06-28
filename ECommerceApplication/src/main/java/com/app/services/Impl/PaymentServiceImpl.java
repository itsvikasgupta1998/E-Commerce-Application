package com.app.services.Impl;

import com.app.entites.Order;
import com.app.entites.Payment;
import com.app.enums.OrderStatus;
import com.app.enums.PaymentStatus;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.PaymentMapper;
import com.app.payloads.CheckoutSessionResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.app.payloads.PaymentResponse;
import com.app.repositories.OrderRepository;
import com.app.repositories.PaymentRepository;
import com.app.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Value("${app.payment.success-url}")
    private String successUrl;

    @Value("${app.payment.cancel-url}")
    private String cancelUrl;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(
            Long paymentId
    ) {

        log.debug(
                "Fetching payment. paymentId={}",
                paymentId
        );

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Payment not found. paymentId={}",
                                    paymentId
                            );

                            return new ResourceNotFoundException(
                                    "Payment",
                                    "paymentId",
                                    paymentId
                            );
                        });

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(
            Long orderId
    ) {

        log.debug(
                "Fetching payment by order. orderId={}",
                orderId
        );

        Payment payment =
                paymentRepository.findByOrder_OrderId(orderId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Payment not found for order. orderId={}",
                                    orderId
                            );

                            return new ResourceNotFoundException(
                                    "Payment",
                                    "orderId",
                                    orderId
                            );
                        });

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Order", "orderId", orderId));


        // Cannot create payment for canceled orders
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new APIException("This order has been cancelled. Please place a new order.");
        }

        Payment payment = order.getPayment();

        if (payment == null) {

            throw new APIException(
                    "Payment record not found"
            );
        }

        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            throw new APIException(
                    "Payment already completed"
            );
        }

        try {

            SessionCreateParams params =
                    SessionCreateParams.builder()

                            .setMode(
                                    SessionCreateParams.Mode.PAYMENT
                            )

                            .setSuccessUrl(
                                    successUrl +
                                            "?orderId=" +
                                            order.getOrderId()
                            )

                            .setCancelUrl(
                                    cancelUrl +
                                            "?orderId=" +
                                            order.getOrderId()
                            )
                            .setExpiresAt(
                                    Instant.now()
                                            .plus(30, ChronoUnit.MINUTES)
                                            .getEpochSecond()
                            )
                            .addLineItem(
                                    SessionCreateParams.LineItem
                                            .builder()

                                            .setQuantity(1L)

                                            .setPriceData(
                                                    SessionCreateParams
                                                            .LineItem
                                                            .PriceData
                                                            .builder()

                                                            .setCurrency("inr")

                                                            .setUnitAmount(
                                                                    order.getTotalAmount()
                                                                            .multiply(
                                                                                    BigDecimal.valueOf(
                                                                                            100
                                                                                    )
                                                                            )
                                                                            .longValue()
                                                            )

                                                            .setProductData(
                                                                    SessionCreateParams
                                                                            .LineItem
                                                                            .PriceData
                                                                            .ProductData
                                                                            .builder()

                                                                            .setName(
                                                                                    "Order #"
                                                                                            + order.getOrderId()
                                                                            )

                                                                            .build()
                                                            )

                                                            .build()
                                            )

                                            .build()
                            )

                            .putMetadata(
                                    "paymentId",
                                    payment.getPaymentId()
                                            .toString()
                            )

                            .putMetadata(
                                    "orderId",
                                    order.getOrderId()
                                            .toString()
                            )

                            .build();

            Session session =
                    Session.create(params);

            payment.setStripeCheckoutSessionId(
                    session.getId()
            );

            paymentRepository.save(
                    payment
            );

            return CheckoutSessionResponse
                    .builder()
                    .sessionId(
                            session.getId()
                    )
                    .checkoutUrl(
                            session.getUrl()
                    )
                    .build();

        }
        catch (StripeException ex) {

            throw new APIException("Stripe checkout creation failed");
        }
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {

        log.info("Refund requested. paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", paymentId));

        // Already refunded
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new APIException("Payment already refunded");
        }

        // Only successful payments can be refunded
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new APIException("Only successful payments can be refunded");
        }

        // Stripe payment required
        if (payment.getStripePaymentIntentId() == null) {
            throw new APIException("Stripe Payment Intent not found");
        }

        Order order = payment.getOrder();

        // Important: Validate BEFORE calling Stripe API
        if (order != null && order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new APIException("Delivered orders cannot be refunded");
        }

        try {

            RefundCreateParams params = RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .build();

            Refund refund = Refund.create(params);
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            payment.setStripeRefundId(refund.getId());
            payment.setRefundedAt(LocalDateTime.now());
            Payment updatedPayment = paymentRepository.save(payment);

            if (order != null && order.getOrderStatus() != OrderStatus.CANCELLED) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }

            log.info("Refund successful. paymentId={}, refundId={}", paymentId, refund.getId());
            return paymentMapper.toResponse(updatedPayment);


        } catch (StripeException ex) {

            log.error("Refund failed. paymentId={}", paymentId, ex);
            throw new APIException("Refund processing failed");
        }
    }
}