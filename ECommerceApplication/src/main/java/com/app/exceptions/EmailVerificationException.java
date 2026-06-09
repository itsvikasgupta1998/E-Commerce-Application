package com.app.exceptions;

public class EmailVerificationException
        extends RuntimeException {

    public EmailVerificationException(String message) {
        super(message);
    }
}
