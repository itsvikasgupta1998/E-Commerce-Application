package com.app.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "carts")
@Data
public class Cart extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cartId;

	private BigDecimal totalPrice;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(
			mappedBy = "cart",
			cascade = CascadeType.ALL,
			orphanRemoval = true
	)
	private List<CartItem> cartItems = new ArrayList<>();
}