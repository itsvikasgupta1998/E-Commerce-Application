package com.app.payloads;

import com.app.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	private Long orderId;
	private String email;
	private LocalDate orderDate;
	private BigDecimal totalAmount;
	private OrderStatus orderStatus;
	private PaymentResponse payment;
	private List<OrderItemResponse> orderItems;
}