package com.app.repositories;

import com.app.entites.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository
		extends JpaRepository<Order, Long> {
	Page<Order> findByUser_Email(String email, Pageable pageable);
}