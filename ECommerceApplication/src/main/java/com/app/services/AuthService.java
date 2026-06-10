package com.app.services;

import com.app.payloads.AuthResponse;
import com.app.payloads.LoginRequest;
import com.app.payloads.UserRegistrationRequest;

public interface AuthService {

    AuthResponse register(
            UserRegistrationRequest request
    );

    AuthResponse login(
            LoginRequest request
    );

    AuthResponse refreshToken(
            String refreshToken
    );

    void verifyEmail(String token);

    void logout(
            String refreshToken
    );

    void forgotPassword(
            String email
    );

    void resetPassword(
            String token,
            String newPassword
    );

}