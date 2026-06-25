package com.app.services;

import com.stripe.model.Event;

public interface PaymentWebhookService {

    void processWebhookEvent(
            Event event
    );
}