package com.app.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartId;

	@Column(nullable = false)
	private BigDecimal totalPrice;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(
			mappedBy = "cart",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<CartItem> cartItems = new ArrayList<>();

	@Version
	private Long version;
}