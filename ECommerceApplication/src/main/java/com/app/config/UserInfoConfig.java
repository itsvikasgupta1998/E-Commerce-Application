package com.app.config;

import com.app.entites.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
public class UserInfoConfig implements UserDetails {

	private final Long userId;

	private final String email;

	private final String password;

	private final boolean enabled;

	private final boolean emailVerified;

	private final List<GrantedAuthority> authorities;

	public UserInfoConfig(User user) {

		this.userId = user.getUserId();

		this.email = user.getEmail();

		this.password = user.getPassword();

		this.enabled = user.getEnabled();

		this.emailVerified = user.getEmailVerified();

		this.authorities =
				user.getRoles()
						.stream()
						.map(role -> (GrantedAuthority)
								new SimpleGrantedAuthority(
										role.getRoleType().name()
								))
						.toList();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isEnabled() {

		return enabled;
	}
}