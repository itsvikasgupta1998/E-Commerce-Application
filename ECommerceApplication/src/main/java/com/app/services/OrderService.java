package com.app.services;

import com.app.payloads.*;

public interface OrderService {

	OrderResponse createOrder(
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

	OrderPageResponse getMyOrders(int page, int size);

	OrderResponse updateOrderStatus(
			Long orderId,
			UpdateOrderStatusRequest request
	);

	OrderResponse cancelOrder(Long orderId);


}