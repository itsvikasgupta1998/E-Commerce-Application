package com.app.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
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
		log.info(
				"Order creation started. email={}, cartId={}",
				email,
				request.getCartId()
		);

		Cart cart =
				cartRepository.findCartByUserEmailAndCartId(
								email,
								request.getCartId()
						)
						.orElseThrow(() -> {

							log.warn(
									"Cart not found while placing order. cartId={}, email={}",
									request.getCartId(),
									email
							);

							return new ResourceNotFoundException(
									"Cart",
									"cartId",
									request.getCartId()
							);
						});


		if (cart.getCartItems() == null ||
				cart.getCartItems().isEmpty()) {

			log.warn(
					"Order creation failed. Cart is empty. cartId={}",
					cart.getCartId()
			);

			throw new APIException(
					"Cart is empty"
			);
		}

		Order order = new Order();

		User user =
				userRepository.findByEmail(email)
						.orElseThrow(() -> {

							log.warn(
									"User not found while placing order. email={}",
									email
							);

							return new ResourceNotFoundException(
									"User",
									"email",
									email
							);
						});

		order.setUser(user);
		order.setOrderDate(LocalDate.now());
		order.setTotalAmount(cart.getTotalPrice());
		order.setOrderStatus(OrderStatus.PLACED);

		Order savedOrder = orderRepository.save(order);
		log.info(
				"Order entity created successfully. orderId={}, userId={}",
				savedOrder.getOrderId(),
				user.getUserId()
		);

		List<OrderItem> orderItems =
				new ArrayList<>();

		for (CartItem cartItem : cart.getCartItems()) {

			Product product =
					cartItem.getProduct();

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

		payment = paymentRepository.save(payment);
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
				"Order created successfully. orderId={}, email={}, amount={}",
				savedOrder.getOrderId(),
				email,
				savedOrder.getTotalAmount()
		);
		return orderMapper.toResponse(savedOrder);
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
				orderRepository.findById(orderId)
						.orElseThrow(() -> {

							log.warn(
									"Order not found. orderId={}",
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
	public OrderPageResponse getOrdersByUser(
			String email,
			int page,
			int size
	) {

		log.debug(
				"Fetching user orders. email={}, page={}, size={}",
				email,
				page,
				size
		);

		Page<Order> orderPage =
				orderRepository.findByUser_Email(
						email,
						PageRequest.of(page, size)
				);
		log.debug(
				"Orders fetched successfully for user. email={}, totalElements={}",
				email,
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
				orderRepository.findById(orderId)
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


}
