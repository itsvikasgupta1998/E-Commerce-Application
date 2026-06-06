package com.app.payloads;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

	private Long cartItemId;

	private Long productId;

	private String productName;

	private Integer quantity;

	private BigDecimal unitPrice;

	private BigDecimal discount;

	private BigDecimal lineTotal;
}