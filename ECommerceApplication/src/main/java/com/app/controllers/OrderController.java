package com.app.controllers;

import com.app.payloads.*;
import com.app.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/create")
	public ResponseEntity<OrderResponse> createOrder(
			@Valid @RequestBody PlaceOrderRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						orderService.createOrder(request)
				);
	}

	@GetMapping("/me")
	public ResponseEntity<OrderPageResponse> getMyOrders(

			@RequestParam(defaultValue = "0")
			int page,

			@RequestParam(defaultValue = "10")
			int size
	) {

		return ResponseEntity.ok(
				orderService.getMyOrders(
						page,
						size
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

	@PatchMapping("/{orderId}/cancel")
	public ResponseEntity<OrderResponse> cancelOrder(
			@PathVariable Long orderId
	) {

		return ResponseEntity.ok(
				orderService.cancelOrder(orderId)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<OrderPageResponse> getAllOrders(

			@RequestParam(defaultValue = "0")
			int page,

			@RequestParam(defaultValue = "10")
			int size,

			@RequestParam(defaultValue = "createdAt")
			String sortBy,

			@RequestParam(defaultValue = "desc")
			String sortDir
	) {

		return ResponseEntity.ok(
				orderService.getAllOrders(
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{orderId}/status")
	public ResponseEntity<OrderResponse> updateStatus(
			@PathVariable Long orderId,
			@Valid @RequestBody UpdateOrderStatusRequest request
	) {

		return ResponseEntity.ok(
				orderService.updateOrderStatus(
						orderId,
						request
				)
		);
	}
}