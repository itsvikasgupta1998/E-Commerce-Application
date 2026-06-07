package com.app.services.Impl;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Role;
import com.app.enums.RoleType;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.UserMapper;
import com.app.payloads.AddressRequest;
import com.app.payloads.UserRegistrationRequest;
import com.app.payloads.UserResponse;
import com.app.payloads.UserUpdateRequest;
import com.app.repositories.AddressRepository;
import com.app.repositories.RoleRepository;
import com.app.repositories.UserRepository;
import com.app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		if (userRepository.existsByEmail(request.getEmail())) {

			log.warn(
					"Registration failed. Email already exists. email={}",
					request.getEmail()
			);

			throw new APIException(
					"Email already registered"
			);
		}

		User user = userMapper.toEntity(request);

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
				userRepository.findById(userId)
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
				userRepository.findById(userId)
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
	public void deleteUser(
			Long userId
	) {

		log.warn(
				"Delete user request received. userId={}",
				userId
		);

		User user =
				userRepository.findById(userId)
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

		userRepository.delete(user);

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
				userRepository.findAll(pageable);

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
}
