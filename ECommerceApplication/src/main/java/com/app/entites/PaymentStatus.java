package com.app.entites;

public enum PaymentStatus {

    PENDING,        // Payment initiate hui hai
    PROCESSING,     // Gateway processing kar raha hai
    SUCCESS,        // Payment successful
    FAILED,         // Payment fail ho gayi
    CANCELLED,      // User ne cancel ki
    REFUNDED,       // Full refund
    PARTIALLY_REFUNDED,
    EXPIRED         // Payment window expire
}