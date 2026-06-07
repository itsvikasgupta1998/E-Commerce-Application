package com.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn(
                "Unauthorized access attempt to {}",
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        Map<String, Object> errorResponse =
                new LinkedHashMap<>();

        errorResponse.put(
                "timestamp",
                LocalDateTime.now()
        );

        errorResponse.put(
                "status",
                HttpServletResponse.SC_UNAUTHORIZED
        );

        errorResponse.put(
                "error",
                "UNAUTHORIZED"
        );

        errorResponse.put(
                "message",
                "Authentication Required"
        );

        errorResponse.put(
                "path",
                request.getRequestURI()
        );

        new ObjectMapper()
                .writeValue(
                        response.getOutputStream(),
                        errorResponse
                );
    }
}