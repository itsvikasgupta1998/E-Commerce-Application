package com.app.services.Impl;

import com.app.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(
            String email,
            String token
    ) {

        String link =
                "http://localhost:8080/auth/verify-email?token="
                        + token;

        log.info("Send email to: {}", email);
        log.info("Verification link: {}", link);
    }
}