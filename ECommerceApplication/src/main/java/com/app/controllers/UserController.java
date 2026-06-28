package com.app.controllers;

import com.app.payloads.APIResponse;
import com.app.payloads.ChangePasswordRequest;
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

	// ================= CURRENT USER =================

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser() {
		return ResponseEntity.ok(userService.getCurrentUser());
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request)
	{
		return ResponseEntity.ok(userService.updateCurrentUser(request));
	}

	@PutMapping("/change-password")
	public ResponseEntity<APIResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		userService.changePassword(request);
		return ResponseEntity.ok(APIResponse
				.builder()
				.success(true)
				.message("Password changed successfully")
				.build());
	}

	// ================= ADMIN APIs =================

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getAllUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "userId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir)
	{
		return ResponseEntity.ok(userService.getAllUsers(page, size, sortBy, sortDir));
	}


	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<APIResponse> deleteUser(@PathVariable Long userId)
	{
		userService.deleteUser(userId);
		return ResponseEntity.ok(APIResponse
				.builder()
				.success(true)
				.message("User deleted successfully")
				.build());
	}

	@GetMapping("/deleted")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getDeletedUsers(
			@RequestParam(defaultValue = "0")
			int page,
			@RequestParam(defaultValue = "10")
			int size,
			@RequestParam(defaultValue = "userId")
			String sortBy,
			@RequestParam(defaultValue = "desc")
			String sortDir)
	{
		return ResponseEntity.ok(userService.getDeletedUsers(page, size, sortBy, sortDir));
	}


	@PostMapping("/{userId}/restore")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<APIResponse> restoreUser(@PathVariable Long userId) {

		userService.restoreUser(userId);
		return ResponseEntity.ok(APIResponse
				.builder()
				.success(true)
				.message("User restored successfully")
				.build());
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getAllUsersIncludingDeleted(

			@RequestParam(defaultValue = "0")
			int page,
			@RequestParam(defaultValue = "15")
			int size,
			@RequestParam(defaultValue = "userId")
			String sortBy,
			@RequestParam(defaultValue = "asc")
			String sortDir)
	{
		return ResponseEntity.ok(userService.getAllUsersIncludingDeleted(page, size, sortBy, sortDir));
	}
}