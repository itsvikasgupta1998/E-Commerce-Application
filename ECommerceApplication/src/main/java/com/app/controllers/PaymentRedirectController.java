package com.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentRedirectController {

    @GetMapping("/payment-success")
    public Map<String, Object> paymentSuccess(
            @RequestParam Long orderId
    ) {

        return Map.of(
                "status", "SUCCESS",
                "message", "Payment completed successfully",
                "orderId", orderId
        );
    }

    @GetMapping("/payment-cancel")
    public Map<String, Object> paymentCancel(
            @RequestParam Long orderId
    ) {

        return Map.of(
                "status", "CANCELLED",
                "message", "Payment was cancelled",
                "orderId", orderId
        );
    }
}