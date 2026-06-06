package com.app.entites;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "addresses")
@Data
public class Address extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long addressId;

	@Column(nullable = false)
	private String street;

	@Column(nullable = false)
	private String buildingName;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(nullable = false)
	private String country;

	@Column(nullable = false)
	private String pincode;

	@ManyToMany(mappedBy = "addresses")
	private Set<User> users = new HashSet<>();
}