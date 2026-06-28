package com.app.controllers;

import com.app.payloads.APIResponse;
import com.app.payloads.CartResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.app.services.CartService;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart APIs")
public class CartController {

	private final CartService cartService;

	@GetMapping("/me")
	public ResponseEntity<CartResponse> getMyCart() {

		return ResponseEntity.ok(cartService.getCurrentUserCart());
	}

	@PostMapping("/me/products/{productId}")
	public ResponseEntity<CartResponse> addProduct(
			@PathVariable Long productId,
			@RequestParam Integer quantity
	) {

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(
						cartService.addProductToCart(
								productId,
								quantity
						)
				);
	}

	@PutMapping("/me/products/{productId}")
	public ResponseEntity<CartResponse> updateQuantity(
			@PathVariable Long productId,
			@RequestParam Integer quantity
	) {

		return ResponseEntity.ok(
				cartService.updateCartItemQuantity(
						productId,
						quantity
				)
		);
	}

	@DeleteMapping("/me/products/{productId}")
	public ResponseEntity<APIResponse> removeProduct(
			@PathVariable Long productId
	) {

		cartService.removeProductFromCart(
				productId
		);

		return ResponseEntity.ok(
				APIResponse.builder()
						.message("Category deleted successfully")
						.success(true)
						.build()
		);
	}
}