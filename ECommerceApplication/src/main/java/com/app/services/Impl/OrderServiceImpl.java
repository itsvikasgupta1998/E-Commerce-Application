package com.app.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.app.entites.*;
import com.app.enums.OrderStatus;
import com.app.enums.PaymentStatus;
import com.app.mappers.OrderMapper;
import com.app.payloads.OrderPageResponse;
import com.app.payloads.PlaceOrderRequest;
import com.app.payloads.UpdateOrderStatusRequest;
import com.app.repositories.*;
import com.app.services.OrderService;
import com.app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.OrderResponse;
import com.app.exceptions.APIException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {


	private final OrderRepository orderRepository;
	private final UserService userService;
	private final OrderItemRepository orderItemRepository;
	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final PaymentRepository paymentRepository;
	private final OrderMapper orderMapper;

	@Override
	public OrderResponse createOrder(
			PlaceOrderRequest request
	) {

		User user =
				userService.getAuthenticatedUserEntity();

		log.info(
				"Order creation started. userId={}, cartId={}",
				user.getUserId(),
				request.getCartId()
		);

		Cart cart =
				cartRepository.findById(
								request.getCartId()
						)
						.orElseThrow(() -> {

							log.warn(
									"Cart not found. cartId={}",
									request.getCartId()
							);

							return new ResourceNotFoundException(
									"Cart",
									"cartId",
									request.getCartId()
							);
						});

		if (!cart.getUser().getUserId()
				.equals(user.getUserId())) {

			log.warn(
					"Unauthorized order creation attempt. cartId={}, ownerUserId={}, requesterUserId={}",
					cart.getCartId(),
					cart.getUser().getUserId(),
					user.getUserId()
			);

			throw new APIException(
					"You are not authorized to place this order"
			);
		}

		if (cart.getCartItems() == null
				|| cart.getCartItems().isEmpty()) {

			log.warn(
					"Order creation failed. Empty cart. cartId={}",
					cart.getCartId()
			);

			throw new APIException(
					"Cart is empty"
			);
		}

		Order order = new Order();

		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setTotalAmount(cart.getTotalPrice());
		order.setOrderStatus(OrderStatus.PLACED);

		Order savedOrder =
				orderRepository.save(order);

		log.info(
				"Order entity created. orderId={}, userId={}",
				savedOrder.getOrderId(),
				user.getUserId()
		);

		List<OrderItem> orderItems = new ArrayList<>();

		List<Product> productsToUpdate = new ArrayList<>();
		for (CartItem cartItem : cart.getCartItems()) {

			Product product = cartItem.getProduct();

			if (product.getQuantity() < cartItem.getQuantity()) {

				log.warn(
						"Insufficient stock. productId={}, available={}, requested={}",
						product.getProductId(),
						product.getQuantity(),
						cartItem.getQuantity()
				);

				throw new APIException(
						"Insufficient stock for product: "
								+ product.getProductName()
				);
			}

			product.setQuantity(
					product.getQuantity()
							- cartItem.getQuantity()
			);

			productsToUpdate.add(product);

			OrderItem orderItem =
					new OrderItem();

			orderItem.setOrder(savedOrder);
			orderItem.setProduct(product);
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setDiscount(cartItem.getDiscount());
			orderItem.setOrderedProductPrice(cartItem.getProductPrice());

			orderItems.add(orderItem);
		}

		productRepository.saveAll(productsToUpdate);

		orderItems = orderItemRepository.saveAll(orderItems);

		Payment payment =
				Payment.builder()
						.order(savedOrder)
						.amount(
								savedOrder.getTotalAmount()
						)
						.paymentMethod(
								request.getPaymentMethod()
						)
						.paymentStatus(
								PaymentStatus.PENDING
						)
						.transactionId(
								"TXN-" +
										System.currentTimeMillis() +
										"-" +
										UUID.randomUUID()
												.toString()
												.substring(0, 8))
						.gatewayName("INTERNAL")
						.gatewayResponse("PAYMENT_INITIATED")
						.build();



		payment =
				paymentRepository.save(
						payment
				);

		log.info(
				"Payment initialized. orderId={}, paymentId={}, amount={}",
				savedOrder.getOrderId(),
				payment.getPaymentId(),
				payment.getAmount()
		);

		savedOrder.setPayment(payment);
		savedOrder.setOrderItems(orderItems);

		cart.getCartItems().clear();
		cart.setTotalPrice(BigDecimal.ZERO);

		cartRepository.save(cart);

		log.info(
				"Order created successfully. orderId={}, userId={}, amount={}",
				savedOrder.getOrderId(),
				user.getUserId(),
				savedOrder.getTotalAmount()
		);

		Order finalOrder = orderRepository
						.findWithDetailsByOrderId(savedOrder.getOrderId())
						.orElseThrow();
		return orderMapper.toResponse(finalOrder);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse getOrderById(
			Long orderId
	) {
		log.debug(
				"Fetching order by id. orderId={}",
				orderId
		);

		Order order =
				orderRepository
						.findWithDetailsByOrderId(orderId)
						.orElseThrow(() -> {

							log.warn(
									"Order not found while fetching. orderId={}",
									orderId
							);

							return new ResourceNotFoundException(
									"Order",
									"orderId",
									orderId
							);
						});
		log.debug(
				"Order fetched successfully. orderId={}",
				orderId
		);

		User user = userService.getAuthenticatedUserEntity();
		validateOrderOwnership(order, user);
		return orderMapper.toResponse(order);
	}


	@Override
	@Transactional(readOnly = true)
	public OrderPageResponse getAllOrders(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {
		log.debug(
				"Fetching orders. page={}, size={}, sortBy={}, sortDir={}",
				page,
				size,
				sortBy,
				sortDir
		);

		Sort sort = sortDir.equalsIgnoreCase("desc")
						? Sort.by(sortBy).descending()
						: Sort.by(sortBy).ascending();

		Page<Order> orderPage =
				orderRepository.findAll(
						PageRequest.of(
								page,
								size,
								sort
						)
				);
		log.debug(
				"Orders fetched successfully. totalElements={}",
				orderPage.getTotalElements()
		);
		return OrderPageResponse.builder()
				.content(
						orderPage.getContent()
								.stream()
								.map(orderMapper::toResponse)
								.toList()
				)
				.pageNumber(
						orderPage.getNumber()
				)
				.pageSize(
						orderPage.getSize()
				)
				.totalElements(
						orderPage.getTotalElements()
				)
				.totalPages(
						orderPage.getTotalPages()
				)
				.lastPage(
						orderPage.isLast()
				)
				.build();
	}


	@Override
	@Transactional(readOnly = true)
	public OrderPageResponse getMyOrders(
			int page,
			int size

	) {

		User user =
				userService.getAuthenticatedUserEntity();

		log.debug(
				"Fetching orders for authenticated user. userId={}, page={}, size={}",
				user.getUserId(),
				page,
				size
		);

		Page<Order> orderPage = orderRepository.findByUser_Email(
						user.getEmail(),
						PageRequest.of(page, size,Sort.by("orderDate").descending())
				);

		return OrderPageResponse.builder()
				.content(
						orderPage.getContent()
								.stream()
								.map(orderMapper::toResponse)
								.toList()
				)
				.pageNumber(orderPage.getNumber())
				.pageSize(orderPage.getSize())
				.totalElements(orderPage.getTotalElements())
				.totalPages(orderPage.getTotalPages())
				.lastPage(orderPage.isLast())
				.build();
	}

	@Override
	public OrderResponse updateOrderStatus(
			Long orderId,
			UpdateOrderStatusRequest request
	) {
		log.info(
				"Updating order status. orderId={}, newStatus={}",
				orderId,
				request.getOrderStatus()
		);

		Order order =
				orderRepository.findWithDetailsByOrderId(orderId)
						.orElseThrow(() -> {

							log.warn(
									"Order not found while updating status. orderId={}",
									orderId
							);

							return new ResourceNotFoundException(
									"Order",
									"orderId",
									orderId
							);
						});

		validateStatusTransition(order.getOrderStatus(), request.getOrderStatus());

		order.setOrderStatus(request.getOrderStatus());
		
		Order updatedOrder = orderRepository.save(order);
		log.info(
				"Order status updated successfully. orderId={}",
				updatedOrder.getOrderId()
		);
		return orderMapper.toResponse(
				updatedOrder
		);
	}

	@Override
	public OrderResponse cancelOrder(
			Long orderId
	) {

		User user = userService.getAuthenticatedUserEntity();

		log.info(
				"Order cancellation requested. orderId={}, userId={}",
				orderId,
				user.getUserId()
		);

		Order order =
				orderRepository. findWithItemsByOrderId(orderId)
						.orElseThrow(() -> {

							log.warn(
									"Order not found while cancelling. orderId={}",
									orderId
							);

							return new ResourceNotFoundException(
									"Order",
									"orderId",
									orderId
							);
						});

		validateOrderOwnership(order, user);

		if (order.getOrderStatus() != OrderStatus.PLACED) {

			log.warn(
					"Order cancellation rejected. orderId={}, status={}",
					orderId,
					order.getOrderStatus()
			);

			throw new APIException(
					"Only PLACED orders can be cancelled"
			);
		}

		order.setOrderStatus(OrderStatus.CANCELLED);
		if(order.getPayment() != null) {

			order.getPayment().setPaymentStatus(
					PaymentStatus.CANCELLED
			);
		}

		List<Product> productsToRestore =
				new ArrayList<>();

		for (OrderItem orderItem :
				order.getOrderItems()) {

			Product product =
					orderItem.getProduct();

			product.setQuantity(
					product.getQuantity()
							+ orderItem.getQuantity()
			);

			productsToRestore.add(product);
		}

		productRepository.saveAll(
				productsToRestore
		);

		Order updatedOrder =
				orderRepository.save(order);

		log.info(
				"Order cancelled successfully. orderId={}",
				orderId
		);

		return orderMapper.toResponse(
				updatedOrder
		);
	}

	private void validateStatusTransition(
			OrderStatus currentStatus,
			OrderStatus newStatus
	) {

		switch (currentStatus) {

			case PLACED -> {

				if (newStatus != OrderStatus.SHIPPED
						&& newStatus != OrderStatus.CANCELLED) {

					throwInvalidStatusTransition(
							currentStatus,
							newStatus
					);
				}
			}

			case SHIPPED -> {

				if (newStatus != OrderStatus.DELIVERED) {

					throwInvalidStatusTransition(
							currentStatus,
							newStatus
					);
				}
			}

			case DELIVERED,
			     CANCELLED -> {

				log.warn(
						"Status update rejected. Order already finalized. currentStatus={}",
						currentStatus
				);

				throw new APIException(
						"Order status cannot be changed"
				);
			}
		}
	}

	private void validateOrderOwnership(
			Order order,
			User authenticatedUser
	) {

		if (!order.getUser()
				.getUserId()
				.equals(authenticatedUser.getUserId())) {

			log.warn(
					"Unauthorized order access. orderId={}, ownerUserId={}, requesterUserId={}",
					order.getOrderId(),
					order.getUser().getUserId(),
					authenticatedUser.getUserId()
			);

			throw new APIException(
					"You are not authorized to access this order"
			);
		}
	}

	private void throwInvalidStatusTransition(
			OrderStatus currentStatus,
			OrderStatus newStatus
	) {

		log.warn(
				"Invalid order status transition. currentStatus={}, requestedStatus={}",
				currentStatus,
				newStatus
		);

		throw new APIException(
				"Invalid order status transition from "
						+ currentStatus
						+ " to "
						+ newStatus
		);
	}
}
