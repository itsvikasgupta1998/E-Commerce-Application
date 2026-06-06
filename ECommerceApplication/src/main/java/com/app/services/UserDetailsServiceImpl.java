package com.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.app.config.UserInfoConfig;
import com.app.entites.User;
import com.app.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl
		implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(
			String email
	) throws UsernameNotFoundException {

		User user =
				userRepository.findByEmail(email)
						.orElseThrow(() ->
								new UsernameNotFoundException(
										"User not found: " + email
								));

		return new UserInfoConfig(user);
	}
}