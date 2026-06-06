package com.app.controllers;

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

	@PostMapping("/{cartId}/products/{productId}")
	public ResponseEntity<CartResponse> addProduct(
			@PathVariable Long cartId,
			@PathVariable Long productId,
			@RequestParam Integer quantity
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(
						cartService.addProductToCart(
								cartId,
								productId,
								quantity
						)
				);
	}

	@GetMapping("/{email}/{cartId}")
	public ResponseEntity<CartResponse> getCart(
			@PathVariable String email,
			@PathVariable Long cartId
	) {
		return ResponseEntity.ok(
				cartService.getCart(
						email,
						cartId
				)
		);
	}

	@PutMapping("/{cartId}/products/{productId}")
	public ResponseEntity<CartResponse> updateQuantity(
			@PathVariable Long cartId,
			@PathVariable Long productId,
			@RequestParam Integer quantity
	) {
		return ResponseEntity.ok(
				cartService.updateCartItemQuantity(
						cartId,
						productId,
						quantity
				)
		);
	}

	@DeleteMapping("/{cartId}/products/{productId}")
	public ResponseEntity<Void> removeProduct(
			@PathVariable Long cartId,
			@PathVariable Long productId
	) {

		cartService.removeProductFromCart(
				cartId,
				productId
		);

		return ResponseEntity.noContent().build();
	}


}
