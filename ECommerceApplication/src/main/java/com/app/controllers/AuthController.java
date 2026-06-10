package com.app.controllers;

import com.app.payloads.*;
import com.app.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;


	// ---------------- REGISTER ----------------
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
			@Valid @RequestBody UserRegistrationRequest request
	) {
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}


	// ---------------- LOGIN ----------------
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request
	) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}


	// ---------------- REFRESH TOKEN ----------------
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(
			@RequestParam String refreshToken
	) {
		AuthResponse response = authService.refreshToken(refreshToken);
		return ResponseEntity.ok(response);
	}


	// ---------------- LOGOUT ----------------
	@PostMapping("/logout")
	public ResponseEntity<APIResponse> logout(
			@Valid @RequestBody LogoutRequest request
	) {

		authService.logout(
				request.getRefreshToken()
		);

		return ResponseEntity.ok(
				APIResponse.builder()
						.message("Logged out successfully")
						.success(true)
						.build()
		);
	}

	@GetMapping("/verify-email")
	public ResponseEntity<String> verifyEmail(@RequestParam String token) {
		authService.verifyEmail(token);
		return ResponseEntity.ok("Email verified successfully");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<APIResponse>
	forgotPassword(
			@Valid
			@RequestBody
			ForgotPasswordRequest request
	) {

		authService.forgotPassword(
				request.getEmail()
		);

		return ResponseEntity.ok(
				APIResponse.builder()
						.message("If the email exists, a reset link has been sent.")
						.success(true)
						.build()
		);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<APIResponse>
	resetPassword(
			@Valid
			@RequestBody
			ResetPasswordRequest request
	) {

		authService.resetPassword(
				request.getToken(),
				request.getNewPassword()
		);

		return ResponseEntity.ok(
				APIResponse.builder()
						.success(true)
						.message("Password reset successfully")
						.build()
		);
	}

}