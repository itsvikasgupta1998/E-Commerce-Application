package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

	private Long orderItemId;
	private ProductResponse product;
	private Integer quantity;
	private BigDecimal discount;
	private BigDecimal orderedProductPrice;
}
