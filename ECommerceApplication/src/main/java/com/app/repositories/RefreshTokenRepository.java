package com.app.repositories;

import com.app.entites.RefreshToken;
import com.app.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    @Query("""
       SELECT rt
       FROM RefreshToken rt
       JOIN FETCH rt.user u
       LEFT JOIN FETCH u.roles
       WHERE rt.token = :token
       """)
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}