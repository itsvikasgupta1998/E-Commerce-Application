package com.app.services.Impl;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Role;
import com.app.enums.RoleType;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.UserMapper;
import com.app.payloads.*;
import com.app.repositories.AddressRepository;
import com.app.repositories.RoleRepository;
import com.app.repositories.UserRepository;
import com.app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final AddressRepository addressRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	@Override
	public UserResponse registerUser(
			UserRegistrationRequest request
	) {

		log.info(
				"User registration started. email={}",
				request.getEmail()
		);

		if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {

			log.warn(
					"Registration failed. Email already exists. email={}",
					request.getEmail()
			);

			throw new APIException(
					"Email already registered"
			);
		}

		User user = userMapper.toEntity(request);
		user.setEnabled(true);
		user.setDeleted(false);
		user.setDeletedAt(null);
		user.setEmailVerified(false);

		user.setPassword(
				passwordEncoder.encode(
						request.getPassword()
				)
		);

		Role userRole =
				roleRepository.findByRoleType(
								RoleType.ROLE_USER
						)
						.orElseThrow(() -> {

							log.error(
									"Default ROLE_USER not found in database"
							);

							return new ResourceNotFoundException(
									"Role",
									"roleType",
									"ROLE_USER"
							);
						});

		user.getRoles().add(userRole);

		if (request.getAddress() != null) {

			log.debug(
					"Address provided during registration. email={}",
					request.getEmail()
			);

			Address address =
					getOrCreateAddress(
							request.getAddress()
					);

			user.getAddresses().add(address);
		}

		Cart cart = new Cart();
		cart.setTotalPrice(java.math.BigDecimal.ZERO);

		cart.setUser(user);
		user.setCart(cart);

		User savedUser =
				userRepository.save(user);

		log.info(
				"User registered successfully. userId={}, email={}",
				savedUser.getUserId(),
				savedUser.getEmail()
		);

		return userMapper.toResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(
			Long userId
	) {

		log.debug(
				"Fetching user by id={}",
				userId
		);

		User user =
				userRepository.findByUserIdAndDeletedFalse(userId)
						.orElseThrow(() -> {

							log.warn(
									"User not found. userId={}",
									userId
							);

							return new ResourceNotFoundException(
									"User",
									"userId",
									userId
							);
						});

		return userMapper.toResponse(user);
	}

	@Override
	public UserResponse updateUser(
			Long userId,
			UserUpdateRequest request
	) {

		log.info(
				"Updating user. userId={}",
				userId
		);

		User user =
				userRepository.findByUserIdAndDeletedFalse(userId)
						.orElseThrow(() -> {

							log.warn(
									"User not found for update. userId={}",
									userId
							);

							return new ResourceNotFoundException(
									"User",
									"userId",
									userId
							);
						});

		userMapper.updateUserFromRequest(
				request,
				user
		);

		if (request.getAddress() != null) {

			Address address =
					getOrCreateAddress(
							request.getAddress()
					);

			user.getAddresses().add(address);
		}

		User updatedUser =
				userRepository.save(user);

		log.info(
				"User updated successfully. userId={}",
				updatedUser.getUserId()
		);

		return userMapper.toResponse(updatedUser);
	}

	@Override
	public void deleteUser(Long userId) {

		log.warn(
				"Delete user request received. userId={}",
				userId
		);

		User user =
				userRepository.findByUserId(userId)
						.orElseThrow(() -> {

							log.warn(
									"User not found for deletion. userId={}",
									userId
							);

							return new ResourceNotFoundException(
									"User",
									"userId",
									userId
							);
						});

		if (Boolean.TRUE.equals(user.getDeleted())) {

			log.warn(
					"Delete failed. User already deleted. userId={}",
					userId
			);

			throw new APIException(
					"User is already deleted"
			);
		}

		user.setDeleted(true);
		user.setEnabled(false);
		user.setDeletedAt(LocalDateTime.now());

		userRepository.save(user);

		log.warn(
				"User deleted successfully. userId={}",
				userId
		);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponse> getAllUsers(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		log.debug(
				"Fetching users. page={}, size={}, sortBy={}, sortDir={}",
				page,
				size,
				sortBy,
				sortDir
		);

		Sort sort =
				sortDir.equalsIgnoreCase("desc")
						? Sort.by(sortBy).descending()
						: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(
						page,
						size,
						sort
				);

		Page<User> users =
				userRepository.findAllByDeletedFalse(pageable);

		log.debug(
				"Users fetched successfully. totalElements={}",
				users.getTotalElements()
		);

		return users.map(
				userMapper::toResponse
		);
	}

	private Address getOrCreateAddress(
			AddressRequest request
	) {

		return addressRepository
				.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
						request.getCountry(),
						request.getState(),
						request.getCity(),
						request.getPincode(),
						request.getStreet(),
						request.getBuildingName()
				)
				.orElseGet(() -> {

					log.debug(
							"Creating new address. city={}, state={}",
							request.getCity(),
							request.getState()
					);

					Address address =
							new Address();

					address.setStreet(request.getStreet());
					address.setBuildingName(request.getBuildingName());
					address.setCity(request.getCity());
					address.setState(request.getState());
					address.setCountry(request.getCountry());
					address.setPincode(request.getPincode());

					return addressRepository.save(address);
				});
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser() {

		User user = getAuthenticatedUser();

		return userMapper.toResponse(user);
	}

	@Override
	public UserResponse updateCurrentUser(
			UserUpdateRequest request
	) {

		User user =
				getAuthenticatedUser();

		userMapper.updateUserFromRequest(
				request,
				user
		);

		if (request.getAddress() != null) {

			Address address =
					getOrCreateAddress(
							request.getAddress()
					);

			user.getAddresses().add(address);
		}

		User updatedUser =
				userRepository.save(user);

		return userMapper.toResponse(
				updatedUser
		);
	}

	@Override
	public void changePassword(
			ChangePasswordRequest request
	) {

		User user =
				getAuthenticatedUser();

		if (
				!passwordEncoder.matches(
						request.getOldPassword(),
						user.getPassword()
				)
		) {

			throw new APIException(
					"Old password is incorrect"
			);
		}

		user.setPassword(
				passwordEncoder.encode(
						request.getNewPassword()
				)
		);

		userRepository.save(user);

		log.info(
				"Password changed successfully. userId={}",
				user.getUserId()
		);
	}

	private User getAuthenticatedUser() {

		Authentication authentication =
				SecurityContextHolder
						.getContext()
						.getAuthentication();

		String email =
				authentication.getName();

		return userRepository
				.findByEmailWithRoles(email)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"User",
								"email",
								email
						)
				);
	}

	@Override
	public void restoreUser(Long userId) {

		log.info(
				"Restore user request received. userId={}",
				userId
		);

		User user =
				userRepository.findByUserId(userId)
						.orElseThrow(() -> {

							log.warn(
									"Restore failed. User not found. userId={}",
									userId
							);

							return new ResourceNotFoundException(
									"User",
									"userId",
									userId
							);
						});

		if (!Boolean.TRUE.equals(user.getDeleted())) {

			log.warn(
					"Restore failed. User already active. userId={}",
					userId
			);

			throw new APIException(
					"User is already active"
			);
		}

		user.setDeleted(false);
		user.setDeletedAt(null);
		user.setEnabled(true);

		userRepository.save(user);

		log.info(
				"User restored successfully. userId={}",
				userId
		);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponse> getDeletedUsers(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		log.debug(
				"Fetching deleted users. page={}, size={}",
				page,
				size
		);

		Sort sort =
				sortDir.equalsIgnoreCase("desc")
						? Sort.by(sortBy).descending()
						: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(
						page,
						size,
						sort
				);

		Page<User> users =
				userRepository.findAllByDeletedTrue(
						pageable
				);

		return users.map(
				userMapper::toResponse
		);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponse> getAllUsersIncludingDeleted(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		log.debug(
				"Fetching all users including deleted. page={}, size={}",
				page,
				size
		);

		Sort sort =
				sortDir.equalsIgnoreCase("desc")
						? Sort.by(sortBy).descending()
						: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(
						page,
						size,
						sort
				);

		Page<User> users =
				userRepository.findAll(
						pageable
				);

		return users.map(
				userMapper::toResponse
		);
	}

	@Override
	@Transactional(readOnly = true)
	public User getAuthenticatedUserEntity() {
		return getAuthenticatedUser();
	}
}
