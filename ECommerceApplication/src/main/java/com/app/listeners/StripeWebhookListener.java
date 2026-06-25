package com.app.listeners;

import com.app.events.StripeWebhookEvent;
import com.app.services.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookListener {

    private final PaymentWebhookService paymentWebhookService;

    @Async
    @EventListener
    public void handleStripeWebhookEvent(
            StripeWebhookEvent event
    ) {

        log.info(
                "Processing Stripe webhook asynchronously"
        );

        paymentWebhookService.processWebhookEvent(
                event.getStripeEvent()
        );
    }
}
