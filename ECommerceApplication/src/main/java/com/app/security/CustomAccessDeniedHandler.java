package com.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        log.warn(
                "Forbidden access to {}",
                request.getRequestURI()
        );

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

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
                HttpServletResponse.SC_FORBIDDEN
        );

        errorResponse.put(
                "error",
                "FORBIDDEN"
        );

        errorResponse.put(
                "message",
                "You do not have permission to access this resource"
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