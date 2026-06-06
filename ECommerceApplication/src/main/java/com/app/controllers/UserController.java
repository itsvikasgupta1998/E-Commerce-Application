package com.app.controllers;

import com.app.payloads.UserResponse;
import com.app.payloads.UserUpdateRequest;
import com.app.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "E-Commerce Application")
public class UserController {

	private final UserService userService;

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserById(
			@PathVariable Long userId
	) {

		return ResponseEntity.ok(
				userService.getUserById(userId)
		);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getAllUsers(

			@RequestParam(defaultValue = "0")
			int page,

			@RequestParam(defaultValue = "10")
			int size,

			@RequestParam(defaultValue = "userId")
			String sortBy,

			@RequestParam(defaultValue = "asc")
			String sortDir
	) {

		return ResponseEntity.ok(
				userService.getAllUsers(
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@PutMapping("/{userId}")
	public ResponseEntity<UserResponse> updateUser(

			@PathVariable Long userId,

			@Valid
			@RequestBody
			UserUpdateRequest request
	) {

		return ResponseEntity.ok(
				userService.updateUser(
						userId,
						request
				)
		);
	}

	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(
			@PathVariable Long userId
	) {

		userService.deleteUser(userId);

		return ResponseEntity.noContent()
				.build();
	}
}