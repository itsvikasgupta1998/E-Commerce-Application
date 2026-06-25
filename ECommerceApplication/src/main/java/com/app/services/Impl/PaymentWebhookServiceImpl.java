package com.app.services.Impl;

import com.app.entites.Order;
import com.app.entites.Payment;
import com.app.enums.OrderStatus;
import com.app.enums.PaymentStatus;
import com.app.repositories.OrderRepository;
import com.app.repositories.PaymentRepository;
import com.app.services.PaymentWebhookService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentWebhookServiceImpl
        implements PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public void processWebhookEvent(Event event)
    {
        log.info("Webhook Event Type Received = {}", event.getType());
        switch (event.getType()) {

            case "checkout.session.completed" ->
                    handleCheckoutCompleted(event);

            case "checkout.session.expired" ->
                    handleCheckoutExpired(event);

            default ->
                    log.info(
                            "Unhandled Stripe event type={}",
                            event.getType()
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

                log.error(
                        "paymentId missing in metadata. eventId={}",
                        event.getId()
                );

                return;
            }

            Long paymentId =
                    Long.valueOf(paymentIdStr);

            Payment payment =
                    paymentRepository.findById(paymentId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Payment not found: "
                                                    + paymentId
                                    ));

            if (payment.getPaymentStatus()
                    == PaymentStatus.SUCCESS) {

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

                order.setOrderStatus(
                        OrderStatus.PLACED
                );

                orderRepository.save(order);
            }

            if (order != null) {
                log.info(
                        "Payment marked SUCCESS. paymentId={}, orderId={}",
                        payment.getPaymentId(),
                        order.getOrderId()
                );
            }

        } catch (EventDataObjectDeserializationException ex) {

            log.error(
                    "Stripe deserialization failed",
                    ex
            );
        }
    }


    private void handleCheckoutExpired(
            Event event
    ) {

        var stripeObjectOptional =
                event.getDataObjectDeserializer()
                        .getObject();

        if (stripeObjectOptional.isEmpty()) {

            log.error(
                    "Failed to deserialize expired checkout session. eventId={}",
                    event.getId()
            );

            return;
        }

        Session session =
                (Session) stripeObjectOptional.get();

        Optional<Payment> paymentOptional =
                paymentRepository
                        .findByStripeCheckoutSessionId(
                                session.getId()
                        );

        if (paymentOptional.isEmpty()) {

            log.warn(
                    "Payment not found for expired sessionId={}",
                    session.getId()
            );

            return;
        }

        Payment payment =
                paymentOptional.get();

        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            return;
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        payment.setFailureReason(
                "Stripe checkout session expired"
        );

        paymentRepository.save(
                payment
        );

        log.info(
                "Payment marked FAILED. paymentId={}, sessionId={}",
                payment.getPaymentId(),
                session.getId()
        );
    }
}