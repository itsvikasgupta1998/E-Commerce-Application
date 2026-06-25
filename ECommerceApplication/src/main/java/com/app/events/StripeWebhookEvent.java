package com.app.events;

import com.stripe.model.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StripeWebhookEvent {

    private final Event stripeEvent;
}