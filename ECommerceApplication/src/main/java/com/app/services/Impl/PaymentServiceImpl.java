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
import com.stripe.model.checkout.Session;
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

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order",
                                        "orderId",
                                        orderId
                                ));

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

            throw new APIException(
                    "Stripe checkout creation failed"
            );
        }
    }

    @Override
    public PaymentResponse markPaymentSuccess(
            Long paymentId
    ) {

        log.info(
                "Mark payment success requested. paymentId={}",
                paymentId
        );

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment",
                                        "paymentId",
                                        paymentId
                                )
                        );

        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            throw new APIException(
                    "Payment already marked as SUCCESS"
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        payment.setUpdatedAt(
                LocalDateTime.now()
        );

        Order order = payment.getOrder();

        if (order != null
                && order.getOrderStatus() == OrderStatus.PLACED) {

            orderRepository.save(order);
        }

        Payment updatedPayment =
                paymentRepository.save(payment);

        log.info(
                "Payment marked SUCCESS. paymentId={}",
                paymentId
        );

        return paymentMapper.toResponse(
                updatedPayment
        );
    }

    @Override
    public PaymentResponse markPaymentFailure(
            Long paymentId,
            String failureReason
    ) {

        log.info(
                "Mark payment failure requested. paymentId={}",
                paymentId
        );

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment",
                                        "paymentId",
                                        paymentId
                                )
                        );

        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            throw new APIException(
                    "Successful payment cannot be marked failed"
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        payment.setGatewayResponse(
                failureReason
        );

        payment.setUpdatedAt(
                LocalDateTime.now()
        );

        Payment updatedPayment =
                paymentRepository.save(payment);

        log.info(
                "Payment marked FAILED. paymentId={}",
                paymentId
        );

        return paymentMapper.toResponse(
                updatedPayment
        );
    }
}