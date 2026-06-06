package com.app.services;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Role;
import com.app.entites.RoleType;
import com.app.entites.User;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.UserMapper;
import com.app.payloads.AddressRequest;
import com.app.payloads.UserRegistrationRequest;
import com.app.payloads.UserResponse;
import com.app.payloads.UserUpdateRequest;
import com.app.repositories.AddressRepository;
import com.app.repositories.RoleRepository;
import com.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException(
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
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Role",
										"roleId",
										2L
								));

		user.getRoles().add(userRole);

		if (request.getAddress() != null) {

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

		return userMapper.toResponse(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getUserById(
			Long userId
	) {

		User user =
				userRepository.findById(userId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"User",
										"userId",
										userId
								));

		return userMapper.toResponse(user);
	}

	@Override
	public UserResponse updateUser(
			Long userId,
			UserUpdateRequest request
	) {

		User user =
				userRepository.findById(userId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"User",
										"userId",
										userId
								));

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

		return userMapper.toResponse(updatedUser);
	}

	@Override
	public void deleteUser(Long userId) {

		User user =
				userRepository.findById(userId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"User",
										"userId",
										userId
								));

		userRepository.delete(user);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserResponse> getAllUsers(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

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

					Address address =
							new Address();

					address.setStreet(
							request.getStreet()
					);

					address.setBuildingName(
							request.getBuildingName()
					);

					address.setCity(
							request.getCity()
					);

					address.setState(
							request.getState()
					);

					address.setCountry(
							request.getCountry()
					);

					address.setPincode(
							request.getPincode()
					);

					return addressRepository.save(
							address
					);
				});
	}
}