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

    void logout(
            String refreshToken
    );
}