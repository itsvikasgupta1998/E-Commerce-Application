package com.app.entites;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(
		name = "addresses",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_address",
				columnNames = {
						"street",
						"building_name",
						"city",
						"state",
						"country",
						"pincode"
				}
		)
)
@Data
public class Address extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long addressId;

	@Column(nullable = false, length = 100)
	private String street;

	@Column(nullable = false)
	private String buildingName;


	@Column(nullable = false)
	private String city;

	@Column(nullable = false, length = 50)
	private String state;

	@Column(nullable = false, length = 50)
	private String country;

	@Column(nullable = false, length = 10)
	private String pincode;

	@ManyToMany(mappedBy = "addresses")
	private Set<User> users = new HashSet<>();
}