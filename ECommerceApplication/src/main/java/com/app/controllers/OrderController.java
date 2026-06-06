package com.app.controllers;

import com.app.payloads.*;
import com.app.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/{email}")
	public ResponseEntity<OrderResponse> createOrder(
			@PathVariable String email,
			@Valid @RequestBody PlaceOrderRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						orderService.createOrder(
								email,
								request
						)
				);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrder(
			@PathVariable Long orderId
	) {

		return ResponseEntity.ok(
				orderService.getOrderById(orderId)
		);
	}

	@PatchMapping("/{orderId}/status")
	public ResponseEntity<OrderResponse> updateStatus(
			@PathVariable Long orderId,
			@RequestBody UpdateOrderStatusRequest request
	) {

		return ResponseEntity.ok(
				orderService.updateOrderStatus(
						orderId,
						request
				)
		);
	}
}