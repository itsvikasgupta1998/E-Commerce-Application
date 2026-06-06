package com.app.entites;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
		name = "roles",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_role_name",
						columnNames = "role_name"
				)
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roleId;

	@Enumerated(EnumType.STRING)
	@Column(
			name = "role_name",
			nullable = false,
			length = 30
	)
	private RoleType roleType;
}