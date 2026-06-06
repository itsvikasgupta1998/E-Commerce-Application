package com.app.controllers;


import com.app.payloads.AuthResponse;
import com.app.payloads.LoginRequest;
import com.app.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.app.payloads.UserRegistrationRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
			@Valid @RequestBody UserRegistrationRequest request
	) {

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request
	) {

		return ResponseEntity.ok(
				authService.login(request)
		);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(
			@RequestParam String refreshToken
	) {

		return ResponseEntity.ok(
				authService.refreshToken(refreshToken)
		);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@RequestParam String refreshToken
	) {

		authService.logout(refreshToken);

		return ResponseEntity.noContent().build();
	}
}