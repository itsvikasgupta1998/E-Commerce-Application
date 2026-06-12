package com.app.payloads;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long userId;

	private String firstName;

	private String lastName;

	private String mobileNumber;

	private String email;

	private Set<RoleResponse> roles;

	private List<AddressResponse> addresses;

	private CartResponse cart;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private Boolean enabled;

	private Boolean deleted;

	private Boolean emailVerified;

	private LocalDateTime deletedAt;


}