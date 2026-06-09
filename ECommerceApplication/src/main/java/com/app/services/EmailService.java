package com.app.services;

public interface EmailService {

    void sendVerificationEmail(String email, String token);
}
