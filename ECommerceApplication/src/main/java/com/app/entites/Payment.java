package com.app.entites;

import com.app.enums.PaymentMethod;
import com.app.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
		name = "payments",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_payment_transaction_id",
						columnNames = "transaction_id"
				),
				@UniqueConstraint(
						name = "uk_payment_gateway_transaction_id",
						columnNames = "gateway_transaction_id"
				)
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@Column(nullable = false, length = 100)
	private String transactionId;

	@Column(unique = true, length = 100)
	private String gatewayTransactionId;

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

	@Column(length = 1000)
	private String gatewayResponse;

	private LocalDateTime paidAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "order_id",
			nullable = false,
			unique = true
	)
	private Order order;


	@Column(
			name = "stripe_payment_intent_id",
			unique = true,
			length = 100
	)
	private String stripePaymentIntentId;

	@Column(
			name = "stripe_checkout_session_id",
			unique = true,
			length = 100
	)
	private String stripeCheckoutSessionId;

	@Column(length = 500)
	private String failureReason;

	@Column(length = 1000)
	private String receiptUrl;

	@Column( unique = true, length = 100)
	private String idempotencyKey;

	@Version
	private Long version;




}