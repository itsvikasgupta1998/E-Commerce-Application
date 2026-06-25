package com.app.repositories;

import com.app.entites.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository
		extends JpaRepository<Order, Long> {
	Page<Order> findByUser_Email(String email, Pageable pageable);

	@EntityGraph(
			attributePaths = {
					"user",
					"payment",
					"orderItems",
					"orderItems.product"
			}
	)
	Optional<Order> findWithDetailsByOrderId(Long orderId);

	@EntityGraph(
			attributePaths = {
					"user",
					"orderItems",
					"orderItems.product"
			}
	)
	Optional<Order> findWithItemsByOrderId(Long orderId);

	@EntityGraph(
			attributePaths = {
					"user",
					"payment"
			}
	)
	Page<Order> findAll(Pageable pageable);
}