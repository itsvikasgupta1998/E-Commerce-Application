package com.app.services;

import com.app.config.UserInfoConfig;
import com.app.entites.RefreshToken;
import com.app.entites.User;
import com.app.payloads.*;
import com.app.repositories.UserRepository;
import com.app.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Override
    public AuthResponse register(
            UserRegistrationRequest request
    ) {

        UserResponse userResponse =
                userService.registerUser(request);

        User user =
                userRepository.findByEmail(
                                userResponse.getEmail()
                        )
                        .orElseThrow();

        UserDetails userDetails =
                new UserInfoConfig(user);

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }

    @Override
    public AuthResponse login(
            LoginRequest request
    ) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user =
                userRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow();

        UserDetails userDetails =
                new UserInfoConfig(user);

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }

    @Override
    public AuthResponse refreshToken(
            String refreshTokenValue
    ) {

        RefreshToken refreshToken =
                refreshTokenService
                        .verifyRefreshToken(
                                refreshTokenValue
                        );

        User user =
                refreshToken.getUser();

        UserDetails userDetails =
                new UserInfoConfig(user);

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }

    @Override
    public void logout(
            String refreshToken
    ) {

        refreshTokenService
                .revokeToken(refreshToken);
    }
}