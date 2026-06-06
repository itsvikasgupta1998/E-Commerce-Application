package com.app.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    public String generateAccessToken(UserDetails userDetails) {

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuer("ECommerceApplication")
                .withIssuedAt(new Date())
                .withExpiresAt(
                        new Date(System.currentTimeMillis() + accessTokenExpiration)
                )
                .withClaim(
                        "roles",
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList())
                )
                .sign(Algorithm.HMAC256(secret));
    }

    public String extractUsername(String token) {

        return JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("ECommerceApplication")
                .build()
                .verify(token)
                .getSubject();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {

            String username =
                    extractUsername(token);

            return username.equals(
                    userDetails.getUsername()
            );

        } catch (Exception ex) {

            return false;
        }
    }

    public DecodedJWT decode(String token) {

        return JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("ECommerceApplication")
                .build()
                .verify(token);
    }
}