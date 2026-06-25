package com.app.controllers;

import com.app.payloads.CheckoutSessionResponse;
import com.app.payloads.PaymentResponse;
import com.app.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(
                        paymentId
                )
        );
    }

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                paymentService.createCheckoutSession(
                        orderId
                )
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(
                        orderId
                )
        );
    }
}