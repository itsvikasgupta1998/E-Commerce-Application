package com.app.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
		indexes = {
				@Index(name = "idx_email", columnList = "email")
		})
@Getter
@Setter
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	private String firstName;
	private String lastName;

	@Column(unique = true, nullable = false)
	private String email;

	private String password;

	private String mobileNumber;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "users_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Set<Role> roles = new HashSet<>();

	@ManyToMany(
			cascade = {
					CascadeType.PERSIST,
					CascadeType.MERGE
			}
	)
	@JoinTable(
			name = "user_addresses",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "address_id")
	)
	private Set<Address> addresses = new HashSet<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private Cart cart;

	@OneToOne(
			mappedBy = "user",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private RefreshToken refreshToken;

	@Column(nullable = false)
	private Boolean enabled = true;

	@Column(nullable = false)
	private Boolean accountLocked = false;

	@Column(nullable = false)
	private Integer failedLoginAttempts = 0;

	private LocalDateTime lockTime;

	@Column(nullable = false)
	private Boolean emailVerified = false;


}