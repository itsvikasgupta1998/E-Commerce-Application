package com.app.services;

import com.app.payloads.ChangePasswordRequest;
import com.app.payloads.UserRegistrationRequest;
import com.app.payloads.UserUpdateRequest;
import com.app.payloads.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

	UserResponse registerUser(UserRegistrationRequest request);

	UserResponse getCurrentUser();

	UserResponse updateCurrentUser(
			UserUpdateRequest request
	);

	void changePassword(
			ChangePasswordRequest request
	);

	UserResponse getUserById(Long userId);

	UserResponse updateUser(
			Long userId,
			UserUpdateRequest request
	);

	void deleteUser(Long userId);

	Page<UserResponse> getAllUsers(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	void restoreUser(Long userId);

	Page<UserResponse> getDeletedUsers(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	Page<UserResponse> getAllUsersIncludingDeleted(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

}
