package com.app.config;

import com.app.security.CustomAccessDeniedHandler;
import com.app.security.JwtAuthFilter;
import com.app.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http
	) throws Exception {

		return http

				.csrf(AbstractHttpConfigurer::disable)

				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)
				.exceptionHandling(ex -> ex

						.authenticationEntryPoint(
								authenticationEntryPoint
						)

						.accessDeniedHandler(
								accessDeniedHandler
						)
				)

				.authorizeHttpRequests(auth -> auth

						// ==========================
						// PUBLIC APIs
						// ==========================

						.requestMatchers(
								"/auth/**",
								"/images/**",
								"/api/files/**"
						).permitAll()

						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/v3/api-docs.yaml"
						).permitAll()

						// Product browsing public
						.requestMatchers(
								HttpMethod.GET,
								"/products/**",
								"/categories/**"
						).permitAll()

						// ==========================
						// ADMIN APIs
						// ==========================

						.requestMatchers("/admin/**")
						.hasRole("ADMIN")

						// ==========================
						// USER + ADMIN APIs
						// ==========================

						.requestMatchers(
								"/users/**",
								"/carts/**",
								"/orders/**",
								"/payments/**",
								"/addresses/**"
						)
						.hasAnyRole("USER", "ADMIN")

						// ==========================
						// EVERYTHING ELSE
						// ==========================

						.anyRequest()
						.authenticated()
				)

				.addFilterBefore(
						jwtAuthFilter,
						UsernamePasswordAuthenticationFilter.class
				)

				.httpBasic(Customizer.withDefaults())

				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration
	) throws Exception {

		return configuration.getAuthenticationManager();
	}
}