package com.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader(SecurityConstants.HEADER);

        log.info("Incoming request: {} {}", request.getMethod(), requestURI);

        // ---------------- NO TOKEN CASE ----------------
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            log.debug("No JWT token found in request: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
            log.debug("JWT token parsed successfully for request: {}", requestURI);
        }
        catch (Exception ex) {

            log.warn("Invalid or expired JWT token for request: {} | Error: {}",
                    requestURI, ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
            {
              "success": false,
              "message": "Invalid or Expired Token"
            }
            """);

            return;
        }

        // ---------------- AUTHENTICATION FLOW ----------------
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            log.debug("Loading user details for username: {}", username);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("User authenticated successfully: {}", username);
            }
            else {
                log.warn("Invalid JWT token for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}