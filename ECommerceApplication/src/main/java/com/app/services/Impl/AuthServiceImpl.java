package com.app.services.Impl;

import com.app.config.UserInfoConfig;
import com.app.entites.EmailVerificationToken;
import com.app.entites.PasswordResetToken;
import com.app.entites.RefreshToken;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.EmailNotVerifiedException;
import com.app.exceptions.EmailVerificationException;
import com.app.exceptions.TokenExpiredException;
import com.app.payloads.*;
import com.app.repositories.EmailVerificationTokenRepository;
import com.app.repositories.PasswordResetTokenRepository;
import com.app.repositories.UserRepository;
import com.app.security.JwtService;
import com.app.services.AuthService;
import com.app.services.EmailService;
import com.app.services.RefreshTokenService;
import com.app.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    private UserSummary mapToUserSummary(User user) {

        return UserSummary.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getRoleType().name())
                                .collect(java.util.stream.Collectors.toSet())
                )
                .emailVerified(user.getEmailVerified())
                .build();
    }

    @Override
    public AuthResponse register(
            UserRegistrationRequest request
    ) {

        log.info(
                "User registration started for email={}",
                request.getEmail()
        );

        UserResponse userResponse = userService.registerUser(request);

        User user = userRepository.findByEmailWithRoles(userResponse.getEmail())
                        .orElseThrow(() -> {

                            log.error(
                                    "Registered user not found in database. email={}",
                                    userResponse.getEmail()
                            );

                            return new RuntimeException(
                                    "User not found"
                            );
                        });

        String verificationToken =
                generateVerificationToken(user);

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationToken
        );

        log.info(
                "User registered successfully. userId={}, email={}",
                user.getUserId(),
                user.getEmail());

        return AuthResponse.builder()
                .message("Registration successful. Please verify your email.")
                .user(mapToUserSummary(user))
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

        User user = userRepository.findByEmailWithRoles(
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

        UserDetails userDetails = new UserInfoConfig(user);
        // EMAIL VERIFICATION CHECK
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before login"
            );
        }
        String accessToken = jwtService.generateAccessToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

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

    public String generateVerificationToken(User user) {

        String tokenValue = UUID.randomUUID().toString();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(token);

        return tokenValue;
    }

    @Override
    public void verifyEmail(String tokenValue) {

        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new EmailVerificationException("Verification link is invalid or already used"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException(
                    "Verification token has expired"
            );
        }

        User user = token.getUser();

        user.setEmailVerified(true);
        user.setEnabled(true);

        userRepository.save(user);

        tokenRepository.delete(token);
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

    @Override
    public void forgotPassword(
            String email
    ) {

        userRepository.findByEmailWithRoles(email)
                .ifPresent(user -> {

                    // Remove old active token if exists
                    passwordResetTokenRepository
                            .findByUser(user)
                            .ifPresent(
                                    passwordResetTokenRepository::delete
                            );

                    String tokenValue =
                            UUID.randomUUID()
                                    .toString();

                    PasswordResetToken token =
                            PasswordResetToken.builder()
                                    .token(tokenValue)
                                    .user(user)
                                    .expiryDate(
                                            LocalDateTime.now()
                                                    .plusMinutes(15)
                                    )
                                    .build();

                    passwordResetTokenRepository.save(
                            token
                    );

                    emailService.sendPasswordResetEmail(
                            email,
                            tokenValue
                    );

                    log.info(
                            "Password reset token generated for userId={}",
                            user.getUserId()
                    );
                });
    }


    @Transactional
    @Override
    public void resetPassword(
            String tokenValue,
            String newPassword
    ) {

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findByTokenWithUser(tokenValue)
                        .orElseThrow(() ->
                                new APIException(
                                        "Invalid reset token"
                                ));

        if (token.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new TokenExpiredException(
                    "Reset token expired"
            );
        }

        User user = token.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(token);
    }

}
