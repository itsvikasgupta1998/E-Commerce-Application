package com.app.services;

import com.app.entites.RefreshToken;
import com.app.entites.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeToken(String token);

    void deleteUserTokens(User user);
}
