package com.app.services.Impl;

import com.app.config.UserInfoConfig;
import com.app.entites.RefreshToken;
import com.app.entites.User;
import com.app.payloads.*;
import com.app.repositories.UserRepository;
import com.app.security.JwtService;
import com.app.services.AuthService;
import com.app.services.RefreshTokenService;
import com.app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
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

        log.info(
                "User registration started for email={}",
                request.getEmail()
        );

        UserResponse userResponse =
                userService.registerUser(request);

        User user =
                userRepository.findByEmail(
                                userResponse.getEmail()
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Registered user not found in database. email={}",
                                    userResponse.getEmail()
                            );

                            return new RuntimeException(
                                    "User retrieval failed after registration"
                            );
                        });

        UserDetails userDetails =
                new UserInfoConfig(user);

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(user);

        log.info(
                "User registered successfully. userId={}, email={}",
                user.getUserId(),
                user.getEmail()
        );

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

        log.info(
                "Login attempt for email={}",
                request.getEmail()
        );

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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Login failed. User not found. email={}",
                                    request.getEmail()
                            );

                            return new RuntimeException(
                                    "User not found"
                            );
                        });

        UserDetails userDetails =
                new UserInfoConfig(user);

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(user);

        log.info(
                "Login successful. userId={}, email={}",
                user.getUserId(),
                user.getEmail()
        );

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

        log.info(
                "Refresh token request received"
        );

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

        log.info(
                "Access token refreshed successfully. userId={}, email={}",
                user.getUserId(),
                user.getEmail()
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

        log.info(
                "Logout request received"
        );

        refreshTokenService
                .revokeToken(refreshToken);

        log.info(
                "Logout successful"
        );
    }
}
