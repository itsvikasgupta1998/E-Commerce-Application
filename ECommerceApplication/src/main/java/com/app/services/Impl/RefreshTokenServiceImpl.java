package com.app.services.Impl;

import com.app.entites.RefreshToken;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.repositories.RefreshTokenRepository;
import com.app.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Override
    public RefreshToken createRefreshToken(User user) {

        log.info(
                "Creating refresh token for userId={}, email={}",
                user.getUserId(),
                user.getEmail()
        );

        refreshTokenRepo
                .findByUser(user)
                .ifPresent(existingToken -> {

                    log.info(
                            "Existing refresh token found. Removing old token for userId={}",
                            user.getUserId()
                    );

                    refreshTokenRepo.delete(existingToken);
                });

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

        RefreshToken savedToken =
                refreshTokenRepo.save(refreshToken);

        log.info(
                "Refresh token created successfully for userId={}",
                user.getUserId()
        );

        return savedToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(
            String token
    ) {

        log.debug(
                "Refresh token verification started"
        );

        RefreshToken refreshToken =
                refreshTokenRepo
                        .findByToken(token)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Refresh token verification failed. Token not found"
                            );

                            return new APIException(
                                    "Refresh token not found"
                            );
                        });

        if (refreshToken.isRevoked()) {

            log.warn(
                    "Refresh token is revoked. userId={}",
                    refreshToken.getUser().getUserId()
            );

            throw new APIException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.isExpired()) {

            log.warn(
                    "Refresh token expired. userId={}",
                    refreshToken.getUser().getUserId()
            );

            refreshTokenRepo.delete(refreshToken);

            throw new APIException(
                    "Refresh token expired"
            );
        }

        log.debug(
                "Refresh token verified successfully. userId={}",
                refreshToken.getUser().getUserId()
        );

        return refreshToken;
    }

    @Override
    public void revokeToken(
            String token
    ) {

        log.info(
                "Refresh token revoke request received"
        );

        refreshTokenRepo
                .findByToken(token)
                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    refreshTokenRepo.save(refreshToken);

                    log.info(
                            "Refresh token revoked successfully. userId={}",
                            refreshToken.getUser().getUserId()
                    );
                });
    }

    @Override
    public void deleteUserTokens(
            User user
    ) {

        log.info(
                "Deleting all refresh tokens for userId={}, email={}",
                user.getUserId(),
                user.getEmail()
        );

        refreshTokenRepo.deleteByUser(user);

        log.info(
                "All refresh tokens deleted for userId={}",
                user.getUserId()
        );
    }
}
