package com.app.entites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.app.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;



@Entity
@Table(name = "orders")
@Data
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private LocalDate orderDate;
	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
	private Payment payment;

	@OneToMany(
			mappedBy = "order",
			cascade = CascadeType.ALL
	)
	private List<OrderItem> orderItems;

	@Version
	private Long version;
}