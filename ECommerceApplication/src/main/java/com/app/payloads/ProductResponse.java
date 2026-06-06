package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

	private Long productId;

	private String productName;

	private String description;

	private Integer quantity;

	private BigDecimal price;

	private BigDecimal discount;

	private BigDecimal specialPrice;

	private String imageUrl;

	private CategoryResponse category;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
}