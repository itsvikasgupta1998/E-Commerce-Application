package com.app.repositories;

import com.app.entites.PasswordResetToken;
import com.app.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(
            String token
    );

    @Query("""
            SELECT p
            FROM PasswordResetToken p
            JOIN FETCH p.user
            WHERE p.token = :token
            """)
    Optional<PasswordResetToken> findByTokenWithUser(
            @Param("token") String token
    );


    Optional<PasswordResetToken> findByUser(User user);
    void deleteByUser(User user);
}
