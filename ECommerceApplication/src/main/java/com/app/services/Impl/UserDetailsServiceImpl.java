package com.app.services.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.app.config.UserInfoConfig;
import com.app.entites.User;
import com.app.repositories.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		log.info("Authentication request received for email: {}", email);

		User user = userRepository.findByEmailWithRoles(email)
				.orElseThrow(() -> {
					log.warn("Authentication failed - user not found: {}", email);
					return new UsernameNotFoundException("User not found: " + email);
				});

		log.debug("User found successfully for authentication. userId={}, email={}",
				user.getUserId(), email);

		return new UserInfoConfig(user);
	}
}