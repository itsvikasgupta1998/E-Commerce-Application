package com.app.entites;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;



@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@Column(nullable = false, unique = true, length = 100)
	private String transactionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PaymentStatus paymentStatus;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(length = 100)
	private String gatewayName;

	@Column(length = 255)
	private String gatewayResponse;

	@OneToOne
	@JoinColumn(name = "order_id")
	private Order order;
}