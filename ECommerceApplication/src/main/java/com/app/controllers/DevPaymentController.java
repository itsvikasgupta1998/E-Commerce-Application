package com.app.controllers;

import com.app.services.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
@Profile("dev")
public class DevPaymentController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping("/checkout-expire/{orderId}")
    public ResponseEntity<String> expireCheckout(@PathVariable Long orderId) {
        paymentWebhookService.markCheckoutExpired(orderId);
        return ResponseEntity.ok("Checkout expired successfully.");
    }
}