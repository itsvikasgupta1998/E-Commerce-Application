package com.app.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.app.entites.*;
import com.app.mappers.OrderMapper;
import com.app.payloads.OrderPageResponse;
import com.app.payloads.PlaceOrderRequest;
import com.app.payloads.UpdateOrderStatusRequest;
import com.app.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.OrderResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {


	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final PaymentRepository paymentRepository;
	private final OrderMapper orderMapper;

	@Override
	public OrderResponse createOrder(
			String email,
			PlaceOrderRequest request
	) {

		Cart cart =
				cartRepository.findCartByUserEmailAndCartId(
								email,
								request.getCartId()
						)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Cart",
										"cartId",
										request.getCartId()
								)
						);


		if (cart.getCartItems() == null ||
				cart.getCartItems().isEmpty()) {

			throw new IllegalStateException(
					"Cart is empty"
			);
		}

		Order order = new Order();

		User user =
				userRepository.findByEmail(email)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"User",
										"email",
										email
								));

		order.setUser(user);
		order.setOrderDate(LocalDate.now());
		order.setTotalAmount(cart.getTotalPrice());
		order.setOrderStatus(OrderStatus.PLACED);

		Order savedOrder =
				orderRepository.save(order);

		List<OrderItem> orderItems =
				new ArrayList<>();

		for (CartItem cartItem : cart.getCartItems()) {

			Product product =
					cartItem.getProduct();

			if (product.getQuantity() <
					cartItem.getQuantity()) {

				throw new IllegalStateException(
						"Insufficient stock for product: "
								+ product.getProductName()
				);
			}

			product.setQuantity(
					product.getQuantity()
							- cartItem.getQuantity()
			);

			productRepository.save(product);

			OrderItem orderItem =
					new OrderItem();

			orderItem.setOrder(savedOrder);
			orderItem.setProduct(product);

			orderItem.setQuantity(
					cartItem.getQuantity()
			);

			orderItem.setDiscount(
					cartItem.getDiscount()
			);

			orderItem.setOrderedProductPrice(
					cartItem.getProductPrice()
			);

			orderItems.add(orderItem);
		}

		orderItems =
				orderItemRepository.saveAll(
						orderItems
				);

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
								UUID.randomUUID()
										.toString()
						)
						.gatewayName("INTERNAL")
						.gatewayResponse(
								"PAYMENT_INITIATED"
						)
						.build();

		payment =
				paymentRepository.save(payment);

		savedOrder.setPayment(payment);
		savedOrder.setOrderItems(orderItems);

		cart.getCartItems().clear();
		cart.setTotalPrice(BigDecimal.ZERO);

		cartRepository.save(cart);

		return orderMapper.toResponse(
				savedOrder
		);
	}

	@Override
	@Transactional(readOnly = true)
	public OrderResponse getOrderById(
			Long orderId
	) {

		Order order =
				orderRepository.findById(orderId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Order",
										"orderId",
										orderId
								));

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

		Sort sort =
				sortDir.equalsIgnoreCase("desc")
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
	public OrderPageResponse getOrdersByUser(
			String email,
			int page,
			int size
	) {

		Page<Order> orderPage =
				orderRepository.findByUser_Email(
						email,
						PageRequest.of(page, size)
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
	public OrderResponse updateOrderStatus(
			Long orderId,
			UpdateOrderStatusRequest request
	) {

		Order order =
				orderRepository.findById(orderId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Order",
										"orderId",
										orderId
								));

		order.setOrderStatus(request.getOrderStatus());

		Order updatedOrder = orderRepository.save(order);

		return orderMapper.toResponse(
				updatedOrder
		);
	}


}
