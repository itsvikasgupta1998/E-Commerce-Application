package com.app.services.Impl;

import com.app.entites.Order;
import com.app.entites.Payment;
import com.app.entites.ProcessedWebhook;
import com.app.enums.OrderStatus;
import com.app.enums.PaymentStatus;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.OrderRepository;
import com.app.repositories.PaymentRepository;
import com.app.repositories.ProcessedWebhookRepository;
import com.app.services.PaymentWebhookService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentWebhookServiceImpl
        implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ProcessedWebhookRepository processedWebhookRepository;

    @Override
    public void processWebhookEvent(Event event) {

        log.info(
                "Webhook Event Type Received = {}",
                event.getType()
        );

        // Ignore duplicate webhooks
        if (processedWebhookRepository.existsByEventId(
                event.getId())) {

            log.info(
                    "Duplicate webhook ignored. eventId={}",
                    event.getId()
            );

            return;
        }

        try {

            switch (event.getType()) {

                case "checkout.session.completed" ->
                        handleCheckoutCompleted(event);

                case "checkout.session.expired" ->
                        handleCheckoutExpired(event);

                case "refund.created",
                     "refund.updated" ->
                        handleRefundEvent(event);

                case "charge.refunded" ->
                        handleChargeRefunded(event);

                default ->
                        log.info(
                                "Unhandled Stripe event type={}",
                                event.getType()
                        );
            }

            // Save only AFTER successful processing
            processedWebhookRepository.save(

                    ProcessedWebhook.builder()

                            .eventId(
                                    event.getId()
                            )

                            .eventType(
                                    event.getType()
                            )

                            .build()
            );

            log.info(
                    "Webhook processed successfully. eventId={}, eventType={}",
                    event.getId(),
                    event.getType()
            );

        } catch (Exception ex) {

            log.error(
                    "Webhook processing failed. eventId={}, eventType={}",
                    event.getId(),
                    event.getType(),
                    ex
            );

            // Don't save processed event.
            // Stripe will retry automatically.
            throw ex;
        }
    }

    private void handleRefundEvent(Event event) {

        try
        {
        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        StripeObject stripeObject =
                deserializer.deserializeUnsafe();

        if (!(stripeObject instanceof Refund refund)) {

            log.warn("Refund object not found");
            return;
        }

        Payment payment =
                paymentRepository
                        .findByStripePaymentIntentId(
                                refund.getPaymentIntent()
                        )
                        .orElse(null);

        if (payment == null) {

            log.warn(
                    "Payment not found. paymentIntent={}",
                    refund.getPaymentIntent()
            );

            return;
        }

        payment.setStripeRefundId(
                refund.getId()
        );

        payment.setRefundedAt(
                LocalDateTime.now()
        );

        payment.setGatewayResponse(
                refund.getStatus()
        );

        if ("succeeded".equals(refund.getStatus())) {

            payment.setPaymentStatus(
                    PaymentStatus.REFUNDED
            );

        } else if ("failed".equals(refund.getStatus())) {

            payment.setFailureReason(
                    "Refund failed"
            );
        }

        paymentRepository.save(payment);

        Order order = payment.getOrder();

        if (order != null
                && payment.getPaymentStatus()
                == PaymentStatus.REFUNDED) {

            order.setOrderStatus(
                    OrderStatus.CANCELLED
            );

            orderRepository.save(order);
        }

        log.info(
                "Refund webhook processed. paymentId={}, refundId={}",
                payment.getPaymentId(),
                refund.getId()
        );
    }
        catch (EventDataObjectDeserializationException ex) {

            log.error(
                    "Stripe refund deserialization failed. eventId={}",
                    event.getId(),
                    ex
            );
        }
    }

    private void handleChargeRefunded(Event event) {

        try
        {
        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        StripeObject stripeObject = deserializer.deserializeUnsafe();

        if (!(stripeObject instanceof Charge charge)) {

            log.warn(
                    "Charge object not found. eventId={}",
                    event.getId()
            );
            return;
        }

        Payment payment =
                paymentRepository
                        .findByStripePaymentIntentId(
                                charge.getPaymentIntent()
                        )
                        .orElse(null);

        if (payment == null) {

            return;
        }

        payment.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        paymentRepository.save(payment);

        log.info(
                "Charge refunded. paymentId={}",
                payment.getPaymentId()
        );
    }

        catch (EventDataObjectDeserializationException ex) {

            log.error(
                    "Stripe charge deserialization failed. eventId={}",
                    event.getId(),
                    ex
            );
        }
    }


    private void handleCheckoutCompleted(Event event) {

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = deserializer.deserializeUnsafe();
            Session session = (Session) stripeObject;
            String paymentIdStr = session.getMetadata().get("paymentId");

            if (paymentIdStr == null) {
                log.error("paymentId missing in metadata. eventId={}", event.getId());
                return;
            }

            Long paymentId = Long.valueOf(paymentIdStr);
            Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                                    new RuntimeException("Payment not found: " + paymentId));

            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return;
            }

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setGatewayName("STRIPE");
            payment.setGatewayTransactionId(session.getPaymentIntent());
            payment.setStripeCheckoutSessionId(session.getId());
            payment.setStripePaymentIntentId(session.getPaymentIntent());

            paymentRepository.save(payment);
            Order order = payment.getOrder();

            if (order != null) {
                order.setOrderStatus(OrderStatus.PLACED);
                orderRepository.save(order);
            }

            if (order != null) {
                log.info("Payment marked SUCCESS. paymentId={}, orderId={}",
                        payment.getPaymentId(),
                        order.getOrderId()
                );
            }

        } catch (EventDataObjectDeserializationException ex) {

            log.error("Stripe deserialization failed", ex);
        }
    }


    private void handleCheckoutExpired(Event event) {

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = deserializer.deserializeUnsafe();
            if (!(stripeObject instanceof Session session)) {
                log.error("Invalid Stripe session object");
                return;
            }

            log.info("Expired Session Id={}", session.getId());

            Payment payment = paymentRepository
                    .findByStripeCheckoutSessionId(session.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found. sessionId=" + session.getId()));

            markCheckoutExpired(payment.getOrder().getOrderId());

        } catch (EventDataObjectDeserializationException ex) {

            log.error("Stripe deserialization failed", ex);
        }
    }

    @Transactional
    public void markCheckoutExpired(Long orderId) {

        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment", "orderId", orderId
                        ));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new APIException("Payment already completed.");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Stripe checkout session expired");

        paymentRepository.save(payment);

        Order order = payment.getOrder();

        if (order != null
                && order.getOrderStatus() != OrderStatus.DELIVERED
                && order.getOrderStatus() != OrderStatus.CANCELLED
                && order.getOrderStatus() != OrderStatus.PAYMENT_EXPIRED) {

            order.setOrderStatus(OrderStatus.PAYMENT_EXPIRED);

            orderRepository.save(order);
        }

        log.info(
                "Checkout expired manually. paymentId={}, orderId={}",
                payment.getPaymentId(),
                orderId
        );
    }
}