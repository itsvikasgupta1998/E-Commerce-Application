package com.app.payloads;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

	@NotNull
	private Long productId;

	@Min(1)
	private Integer quantity;
}
