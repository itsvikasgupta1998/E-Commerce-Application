package com.app.controllers;

import com.app.services.PaymentWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/webhook")
public class StripeWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(

            @RequestBody String payload,

            @RequestHeader("Stripe-Signature")
            @NotBlank
            String stripeSignature
    ) {

        try {

            Event event =
                    Webhook.constructEvent(
                            payload,
                            stripeSignature,
                            webhookSecret
                    );

            log.info(
                    "Stripe webhook received. eventId={}, eventType={}",
                    event.getId(),
                    event.getType()
            );

            paymentWebhookService.processWebhookEvent(
                    event
            );

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException ex) {

            log.error(
                    "Invalid Stripe webhook signature",
                    ex
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Stripe webhook processing failed",
                    ex
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}