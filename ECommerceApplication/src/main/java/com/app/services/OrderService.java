package com.app.services;

import com.app.payloads.*;

public interface OrderService {

	OrderResponse createOrder(
			String email,
			PlaceOrderRequest request
	);

	OrderResponse getOrderById(
			Long orderId
	);

	OrderPageResponse getAllOrders(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	OrderPageResponse getOrdersByUser(
			String email,
			int page,
			int size
	);

	OrderResponse updateOrderStatus(
			Long orderId,
			UpdateOrderStatusRequest request
	);
}