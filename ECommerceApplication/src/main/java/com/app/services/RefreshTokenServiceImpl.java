package com.app.services;

import com.app.entites.RefreshToken;
import com.app.entites.User;
import com.app.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Override
    public RefreshToken createRefreshToken(User user) {

        refreshTokenRepo
                .findByUser(user)
                .ifPresent(refreshTokenRepo::delete);

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusSeconds(
                                                refreshTokenExpiration / 1000
                                        )
                        )
                        .revoked(false)
                        .build();

        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepo
                        .findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Refresh token not found"
                                ));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.isExpired()) {

            refreshTokenRepo.delete(refreshToken);

            throw new RuntimeException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {

        refreshTokenRepo
                .findByToken(token)
                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    refreshTokenRepo.save(refreshToken);
                });
    }

    @Override
    public void deleteUserTokens(User user) {

        refreshTokenRepo.deleteByUser(user);
    }
}